package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.metaborg.util.functions.CheckedFunction1;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.AbstractTermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.util.B;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.Logger;
import mb.pie.api.STask;
import mb.resource.ResourceService;
import mb.resource.fs.FSPath;
import mb.resource.fs.FSResource;
import mb.stratego.build.strincr.SplitResult.ConstructorSignature;
import mb.stratego.build.strincr.SplitResult.StrategySignature;
import mb.stratego.build.strincr.StaticChecks.Data;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StringSetWithPositions;

public class Frontends {
    public static class Input implements Serializable {
        protected final File inputFile;
        protected final Collection<File> includeDirs;
        protected final Collection<String> builtinLibs;
        protected final Collection<STask> originTasks;
        protected final File projectLocation;

        public Input(File inputFile, Collection<File> includeDirs, Collection<String> builtinLibs,
            Collection<STask> originTasks, File projectLocation) {
            this.inputFile = inputFile;
            this.includeDirs = includeDirs;
            this.builtinLibs = builtinLibs;
            this.originTasks = originTasks;
            this.projectLocation = projectLocation;

        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Input))
                return false;
            Input input = (Input) o;
            return inputFile.equals(input.inputFile) && includeDirs.equals(input.includeDirs) && builtinLibs
                .equals(input.builtinLibs) && originTasks.equals(input.originTasks) && projectLocation
                .equals(input.projectLocation);
        }

        @Override
        public int hashCode() {
            return Objects.hash(inputFile, includeDirs, builtinLibs, originTasks, projectLocation);
        }

        @Override public String toString() {
            return "FrontEnds$Input(inputFile=" + inputFile + ", projectLocation=" + projectLocation + ")";
        }
    }

    public static class Output implements Serializable {
        public final StaticChecks.Data staticData;
        public final BackendData backendData;
        public final Map<String, SplitResult> splitModules;
        public final List<Message<?>> messages;
        public StaticChecks.Output staticCheckOutput;

        public Output(Data staticData, BackendData backendData, Map<String, SplitResult> splitModules,
            List<Message<?>> messages) {
            this.staticData = staticData;
            this.backendData = backendData;
            this.splitModules = splitModules;
            this.messages = messages;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(!(o instanceof Output))
                return false;
            Output output = (Output) o;
            return staticData.equals(output.staticData) && backendData.equals(output.backendData) && splitModules
                .equals(output.splitModules) && messages.equals(output.messages) && staticCheckOutput
                .equals(output.staticCheckOutput);
        }

        @Override
        public int hashCode() {
            return Objects.hash(staticData, backendData, splitModules, messages, staticCheckOutput);
        }
    }

    protected final LibFrontend strIncrFrontLib;
    private final StaticChecks staticChecks;
    private final SubFrontend strIncrSubFront;
    private final ParseStratego parseStratego;
    private final ResourceService resourceService;

    @Inject public Frontends(ResourceService resourceService, LibFrontend strIncrFrontLib, StaticChecks staticChecks, StrIncrContext strContext,
        ParseStratego parseStratego, SubFrontend strIncrSubFront) {
        this.resourceService = resourceService;
        this.strIncrFrontLib = strIncrFrontLib;
        this.staticChecks = staticChecks;
        this.strIncrSubFront = strIncrSubFront;
        this.parseStratego = parseStratego;
    }

    protected Output collectInformation(ExecContext execContext, Input input, Path projectLocationPath)
        throws Exception {
        final Module inputModule = Module.source(Paths.get(input.inputFile.toURI()));

        final Output output = frontends(execContext, input, projectLocationPath, inputModule);

        long preCheckTime = System.nanoTime();

        // CHECK: constructor/strategy uses have definition which is imported
        output.staticCheckOutput = staticChecks.insertCasts(execContext, inputModule.path, output, output.messages,
            projectLocationPath);

        BuildStats.checkTime = System.nanoTime() - preCheckTime;
        return output;
    }

    public Output frontends(ExecContext execContext, Input input, Path projectLocationPath, Module inputModule)
        throws Exception {
        // FRONTEND
        final java.util.Set<Module> seen = new HashSet<>();
        final Deque<Module> workList = new ArrayDeque<>();
        workList.add(inputModule);
        seen.add(inputModule);

        final List<Import> defaultImports = new ArrayList<>(input.builtinLibs.size());
        for(String builtinLib : input.builtinLibs) {
            defaultImports.add(Import.library(B.string(builtinLib)));
        }
        // depend on the include directories in which we search for str and rtree files
        for(File includeDir : input.includeDirs) {
            execContext.require(includeDir);
        }

        final StaticChecks.Data staticData = new StaticChecks.Data();

        final BackendData backendData = new BackendData();
        final List<Message<?>> messages = new ArrayList<>();
        final Map<String, SplitResult> splitModules = new HashMap<>();

        long shuffleStartTime;
        do {
            final Module module = workList.remove();

            if(module.type == Module.Type.library) {
                final LibFrontend.Input frontLibInput =
                    new LibFrontend.Input(Library.fromString(resourceService, module.path));
                final LibFrontend.Output frontLibOutput = execContext.require(strIncrFrontLib, frontLibInput);

                shuffleStartTime = System.nanoTime();

                final StringSetWithPositions strategies = new StringSetWithPositions();
                for(StrategySignature strategy : frontLibOutput.strategies) {
                    strategies.add(new StrategoString(strategy.cifiedName(), AbstractTermFactory.EMPTY_LIST));
                }
                staticData.definedStrategies.put(module.path, strategies);
                staticData.strategySignatures.put(module.path, frontLibOutput.strategies);
                staticData.registerConstructorDefinitions(module.path, frontLibOutput.constrs);

                reportOverlappingStrategies(staticData.libraryExternalStrategies, strategies,
                    execContext.logger());

                staticData.libraryExternalStrategies.addAll(strategies);
                final StringSetWithPositions constrs = new StringSetWithPositions();
                for(ConstructorSignature constr : frontLibOutput.constrs) {
                    constrs.add(new StrategoString(constr.cifiedName(), AbstractTermFactory.EMPTY_LIST));
                }
                staticData.externalConstructors.addAll(constrs);

                BuildStats.shuffleLibTime += System.nanoTime() - shuffleStartTime;

                continue;
            }

            final String inputFileString = new FSPath(projectLocationPath).appendOrReplaceWithPath(module.path).getNormalized().toString();
            final FSResource inputFile = new FSResource(inputFileString);
            // File existence:
            if(!inputFile.exists()) {
                execContext.logger().trace("File deletion detected: " + inputFileString);
                continue;
            }
            // Parse file:
            execContext.require(inputFile);
            final IStrategoTerm ast;
            try(final InputStream inputStream = new BufferedInputStream(inputFile.openRead())) {
                if("rtree".equals(inputFile.getLeafExtension())) {
                    ast = parseStratego.parseRtree(inputStream);
                } else {
                    ast = parseStratego.parse(inputStream, StandardCharsets.UTF_8, inputFileString);
                }
            }
            execContext.logger().trace("File parsed: " + inputFileString);
            staticData.sugarASTs.put(module.path, ast);
            // Split file up with PIE task:
            final SubFrontend.Input splitInput =
                SubFrontend.Input.split(input.originTasks, inputFileString, inputFileString, ast);
            final IStrategoTerm splitTerm = execContext.require(strIncrSubFront, splitInput).result;
            final SplitResult splitResult = SplitResult.fromTerm(splitTerm, inputFileString);
            // Save results for use in different order during type checking
            splitModules.put(module.path, splitResult);
            // Resolve imports
            final Set<Module> expandedImports =
                resolveImports(input, defaultImports, messages, module, splitResult,
                    execContext::require);
            for(Module m : expandedImports) {
                Relation.getOrInitialize(staticData.imports, module.path, HashSet::new).add(m.path);
            }
            expandedImports.removeAll(seen);
            workList.addAll(expandedImports);
            seen.addAll(expandedImports);
        } while(!workList.isEmpty());
        return new Output(staticData, backendData, splitModules, messages);
    }

    public static Set<Module> resolveImports(Input input, List<Import> defaultImports,
        List<Message<?>> messages, Module module, SplitResult splitResult,
        CheckedFunction1<Path, FSResource, IOException> require) throws IOException, ExecException {
        final List<Import> theImports = new ArrayList<>(splitResult.imports.size());
        for(IStrategoTerm importTerm : splitResult.imports) {
            theImports.add(Import.fromTerm(importTerm));
        }
        theImports.addAll(defaultImports);

        return Module
            .resolveWildcards(module.path, theImports, input.includeDirs, messages, require);
    }

    public static void reportOverlappingStrategies(StringSetWithPositions externalStrategies,
        StringSetWithPositions newExternalStrategies, Logger logger) {
        final java.util.Set<String> overlappingStrategies = Sets.difference(
            Sets.intersection(externalStrategies.readSet(), newExternalStrategies.readSet()),
            StaticChecks.ALWAYS_DEFINED);
        if(!overlappingStrategies.isEmpty()) {
            logger.warn("Overlapping external strategy definitions: " + overlappingStrategies, null);
        }
    }
}