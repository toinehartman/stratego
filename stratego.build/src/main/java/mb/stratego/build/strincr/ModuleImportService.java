package mb.stratego.build.strincr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;

import mb.pie.api.ExecContext;
import mb.pie.api.ExecException;
import mb.pie.api.STask;
import mb.pie.api.Supplier;
import mb.pie.api.stamp.output.OutputStampers;
import mb.pie.api.stamp.resource.ResourceStampers;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.ResourcePath;
import mb.resource.hierarchical.match.PathResourceMatcher;
import mb.resource.hierarchical.match.path.ExtensionsPathMatcher;
import mb.stratego.build.util.ExistsAndRTreeStamper;
import mb.stratego.build.util.LastModified;
import mb.stratego.build.util.Relation;

public class ModuleImportService implements IModuleImportService {
    private final ResourcePathConverter resourcePathConverter;
    private final StrategoLanguage strategoLanguage;

    @Inject public ModuleImportService(ResourcePathConverter resourcePathConverter,
        StrategoLanguage strategoLanguage) {
        this.resourcePathConverter = resourcePathConverter;
        this.strategoLanguage = strategoLanguage;
    }

    @Override public ImportResolution resolveImport(ExecContext context, IStrategoTerm anImport,
        ImportResolutionInfo importResolutionInfo)
        throws ExecException, IOException {
        /*
         * Note that we require the sdf task here to force it to generated needed str files. We
         *     then discover those in this method with a directory search.
         */
        for(final STask<?> t : importResolutionInfo.strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        List<ResourcePath> includeDirs = new ArrayList<>(importResolutionInfo.includeDirs);
        for(Supplier<Stratego2LibInfo> str2lib : importResolutionInfo.str2libraries) {
            includeDirs.add(context.require(str2lib).str2libFile.getParent());
        }
        if(!TermUtils.isAppl(anImport)) {
            throw new ExecException("Import term was not a constructor: " + anImport);
        }
        final IStrategoAppl appl = (IStrategoAppl) anImport;
        switch(appl.getName()) {
            case "Import": {
                final String moduleString = TermUtils.toJavaStringAt(appl, 0);
                final @Nullable BuiltinLibraryIdentifier builtinLibraryIdentifier =
                    BuiltinLibraryIdentifier.fromString(moduleString);
                if(builtinLibraryIdentifier != null) {
                    if(!importResolutionInfo.linkedLibraries.contains(builtinLibraryIdentifier)) {
                        return UnresolvedImport.INSTANCE;
                    }
                    return new ResolvedImport(Collections.singleton(builtinLibraryIdentifier));
                }

                final Set<mb.stratego.build.strincr.ModuleIdentifier> result = new HashSet<>();
                boolean foundSomethingToImport = false;
                for(ResourcePath dir : includeDirs) {
                    final ResourcePath str2libPath =
                        dir.appendOrReplaceWithPath(moduleString + ".str2lib");
                    final HierarchicalResource str2libResource =
                        context.require(str2libPath, ResourceStampers.<HierarchicalResource>exists());
                    if(str2libResource.exists()) {
                        foundSomethingToImport = true;
                        result.add(new mb.stratego.build.strincr.ModuleIdentifier(false, true,
                            moduleString, str2libPath));
                    } else {
                        final ResourcePath rtreePath =
                            dir.appendOrReplaceWithPath(moduleString + ".rtree");
                        final HierarchicalResource rtreeResource = context
                            .require(rtreePath, new ExistsAndRTreeStamper<HierarchicalResource>());
                        if(rtreeResource.exists()) {
                            foundSomethingToImport = true;
                            final boolean isLibrary =
                                ExistsAndRTreeStamper.isLibraryRTree(rtreeResource);
                            result.add(
                                new mb.stratego.build.strincr.ModuleIdentifier(true, isLibrary,
                                    moduleString, rtreePath));
                        } else {
                            final ResourcePath str2Path =
                                dir.appendOrReplaceWithPath(moduleString + ".str2");
                            final HierarchicalResource str2Resource = context
                                .require(str2Path, ResourceStampers.<HierarchicalResource>exists());
                            if(str2Resource.exists()) {
                                foundSomethingToImport = true;
                                result.add(
                                    new mb.stratego.build.strincr.ModuleIdentifier(false, false,
                                        moduleString, str2Path));
                            } else {
                                final ResourcePath strPath =
                                    dir.appendOrReplaceWithPath(moduleString + ".str");
                                final HierarchicalResource strResource = context.require(strPath,
                                    ResourceStampers.<HierarchicalResource>exists());
                                if(strResource.exists()) {
                                    foundSomethingToImport = true;
                                    result.add(
                                        new mb.stratego.build.strincr.ModuleIdentifier(true, false,
                                            moduleString, strPath));
                                }
                            }
                        }
                    }
                }
                if(!foundSomethingToImport) {
                    return UnresolvedImport.INSTANCE;
                }
                return new ResolvedImport(result);
            }
            case "ImportWildcard": {
                final String directory = TermUtils.toJavaStringAt(appl, 0);
                final Map<String, Set<mb.stratego.build.strincr.ModuleIdentifier>> foundModules = new HashMap<>();
                boolean foundSomethingToImport = false;
                for(ResourcePath includeDir : importResolutionInfo.includeDirs) {
                    final ResourcePath searchDirectory =
                        includeDir.appendOrReplaceWithPath(directory);
                    context.require(searchDirectory);
                    final HierarchicalResource searchDir =
                        context.getResourceService().getHierarchicalResource(searchDirectory);
                    if(searchDir.exists()) {
                        // N.B. deliberate choice not to resolve to str2lib files here, those should be imported by name.
                        final List<HierarchicalResource> moduleFiles = searchDir.list(
                            new PathResourceMatcher(new ExtensionsPathMatcher("rtree", "str2", "str")))
                            .collect(Collectors.toList());
                        for(HierarchicalResource moduleFile : moduleFiles) {
                            foundSomethingToImport = true;
                            @Nullable final String filename = moduleFile.getLeaf();
                            @Nullable final String ext = moduleFile.getLeafExtension();
                            assert filename != null : "HierarchicalResource::list returned some resources without a path leaf?!";
                            assert ext != null : "HierarchicalResource::list returned some resources without an extension?!";
                            boolean legacyStratego;
                            boolean isLibrary = false;
                            String moduleString;
                            switch(ext) {
                                case "rtree":
                                    isLibrary = ExistsAndRTreeStamper.isLibraryRTree(moduleFile);
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".rtree".length());
                                    legacyStratego = true;
                                    break;
                                case "str2":
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".str2".length());
                                    legacyStratego = false;
                                    break;
                                case "str":
                                    moduleString = directory + "/" + filename
                                        .substring(0, filename.length() - ".str".length());
                                    legacyStratego = true;
                                    break;
                                default:
                                    assert false : "HierarchicalResource::list returned some resources an extension that it shouldn't search for?!";
                                    continue;
                            }
                            Relation.getOrInitialize(foundModules, moduleString, HashSet::new)
                                .add(new mb.stratego.build.strincr.ModuleIdentifier(legacyStratego, isLibrary,
                                    moduleString, moduleFile.getPath()));
                        }
                    }
                }
                if(!foundSomethingToImport) {
                    return UnresolvedImport.INSTANCE;
                }
                // Have rtree files for a module shadow str2/str files, have str2 files shadow str files
                final Set<mb.stratego.build.strincr.ModuleIdentifier> result = new HashSet<>();
                for(Set<mb.stratego.build.strincr.ModuleIdentifier> modules : foundModules.values()) {
                    if(modules.size() == 1) {
                        result.addAll(modules);
                    } else {
                        boolean foundSomething = false;
                        for(mb.stratego.build.strincr.ModuleIdentifier module : modules) {
                            if(module.path.getLeafFileExtension().equals("rtree")) {
                                foundSomething = true;
                                result.add(module);
                            }
                        }
                        if(foundSomething) {
                            continue;
                        }
                        for(mb.stratego.build.strincr.ModuleIdentifier module : modules) {
                            if(module.path.getLeafFileExtension().equals("str2")) {
                                foundSomething = true;
                                result.add(module);
                            }
                        }
                        if(foundSomething) {
                            continue;
                        }
                        // they're all .str files
                        result.addAll(modules);
                    }
                }
                return new ResolvedImport(result);
            }
            default:
                throw new ExecException(
                    "Import term was not the expected Import or ImportWildcard: " + appl);
        }
    }

    @Override public LastModified<IStrategoTerm> getModuleAst(ExecContext context,
        ModuleIdentifier moduleIdentifier,
        ImportResolutionInfo importResolutionInfo) throws Exception {
        /*
         * Every getModuleAst call depends on the sdf task so there is no hidden dep. To make
         *     sure that getModuleAst only runs when their input _files_ change, we need
         *     getModuleAst to depend on the sdf task with a simple stamper that allows the
         *     execution of the sdf task to be ignored.
         */
        for(final STask<?> t : importResolutionInfo.strFileGeneratingTasks) {
            context.require(t, OutputStampers.inconsequential());
        }
        if(moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier) {
            final mb.stratego.build.strincr.ModuleIdentifier identifier =
                (mb.stratego.build.strincr.ModuleIdentifier) moduleIdentifier;
            HierarchicalResource resource = context.require(identifier.path);
            try(final InputStream inputStream = new BufferedInputStream(
                resource.openRead())) {
                final long lastModified =
                    resource.getLastModifiedTime().getEpochSecond();
                if(moduleIdentifier.isLibrary() && moduleIdentifier.legacyStratego()) {
                    return new LastModified<>(strategoLanguage.parseRtree(inputStream), lastModified);
                } else if(moduleIdentifier.isLibrary() && !moduleIdentifier.legacyStratego()) {
                    for(Supplier<Stratego2LibInfo> str2library : importResolutionInfo.str2libraries) {
                        if(str2library instanceof STask) {
                            context.require((STask<Stratego2LibInfo>) str2library, OutputStampers.inconsequential());
                        } else {
                            context.require(str2library);
                        }
                    }
                    return new LastModified<>(strategoLanguage.parseStr2Lib(inputStream), lastModified);
                } else {
                    return new LastModified<>(strategoLanguage
                        .parse(inputStream, StandardCharsets.UTF_8,
                            resourcePathConverter.toString(identifier.path)), lastModified);
                }
            }
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            final BuiltinLibraryIdentifier builtinLibraryIdentifier =
                (BuiltinLibraryIdentifier) moduleIdentifier;
            return new LastModified<>(builtinLibraryIdentifier.readLibraryFile(), 0L);
        }
    }

    @Override public @Nullable String fileName(ModuleIdentifier moduleIdentifier) {
        if(moduleIdentifier instanceof mb.stratego.build.strincr.ModuleIdentifier) {
            final mb.stratego.build.strincr.ModuleIdentifier identifier =
                (mb.stratego.build.strincr.ModuleIdentifier) moduleIdentifier;
            return resourcePathConverter.toString(identifier.path);
        } else {// if(moduleIdentifier instanceof BuiltinLibraryIdentifier) {
            return null;
        }
    }
}
