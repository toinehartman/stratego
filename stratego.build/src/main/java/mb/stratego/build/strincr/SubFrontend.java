package mb.stratego.build.strincr;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import mb.stratego.build.util.IOAgentTrackerFactory;
import org.apache.commons.io.output.NullOutputStream;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Strategy;
import org.strategoxt.strc.compile_top_level_def_0_0;
import org.strategoxt.strc.insert_casts_0_0;
import org.strategoxt.strc.split_module_0_0;

import javax.inject.Inject;

import mb.pie.api.ExecContext;
import mb.pie.api.STask;
import mb.pie.api.TaskDef;
import mb.pie.api.stamp.output.InconsequentialOutputStamper;
import mb.stratego.build.termvisitors.TermSize;
import mb.stratego.build.util.IOAgentTracker;
import mb.stratego.build.util.StrIncrContext;
import mb.stratego.build.util.StrategoExecutor;

public class SubFrontend implements TaskDef<SubFrontend.Input, SubFrontend.Output> {
    public static final String id = SubFrontend.class.getCanonicalName();

    public static final class Input implements Serializable {
        private final Collection<STask> originTasks;
        public final String moduleName;
        public final String cifiedName;
        public final InputType inputType;
        public final IStrategoTerm ast;

        protected Input(String moduleName, String cifiedName, InputType inputType, IStrategoTerm ast) {
            this(Collections.emptyList(), moduleName, cifiedName, inputType, ast);
        }

        protected Input(Collection<STask> originTasks, String moduleName, String cifiedName, InputType inputType, IStrategoTerm ast) {
            this.originTasks = originTasks;
            this.moduleName = moduleName;
            this.cifiedName = cifiedName;
            this.inputType = inputType;
            this.ast = ast;
        }

        @Override public String toString() {
            return "SubFront$Input(" + inputType.name() + ", " + cifiedName + ')';
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((ast == null) ? 0 : ast.hashCode());
            result = prime * result + ((cifiedName == null) ? 0 : cifiedName.hashCode());
            result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
            result = prime * result + ((inputType == null) ? 0 : inputType.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Input other = (Input) obj;
            if(ast == null) {
                if(other.ast != null)
                    return false;
            } else if(!ast.equals(other.ast))
                return false;
            if(cifiedName == null) {
                if(other.cifiedName != null)
                    return false;
            } else if(!cifiedName.equals(other.cifiedName))
                return false;
            if(moduleName == null) {
                if(other.moduleName != null)
                    return false;
            } else if(!moduleName.equals(other.moduleName))
                return false;
            if(inputType != other.inputType)
                return false;
            return true;
        }

        public static Input topLevelDefinition(String inputFileString, String cifiedName, IStrategoTerm ast) {
            return new Input(inputFileString, cifiedName, InputType.TopLevelDefinition, ast);
        }

        public static Input split(Collection<STask> originTasks, String inputFileString, String cifiedName, IStrategoTerm ast) {
            return new Input(originTasks, inputFileString, cifiedName, InputType.Split, ast);
        }

        public static Input insertCasts(String moduleName, String cifiedName, IStrategoTerm ast) {
            return new Input(moduleName, cifiedName, InputType.InsertCasts, ast);
        }
    }

    public static final class Output implements Serializable {
        final IStrategoTerm result;

        public Output(IStrategoTerm result) {
            this.result = result;
        }

        @Override public String toString() {
            return "StrIncrSubFront$Output";
        }

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((this.result == null) ? 0 : this.result.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Output other = (Output) obj;
            if(result == null) {
                if(other.result != null)
                    return false;
            } else if(!result.equals(other.result))
                return false;
            return true;
        }
    }

    private enum InputType {
        TopLevelDefinition(compile_top_level_def_0_0.instance),
        InsertCasts(insert_casts_0_0.instance),
        Split(split_module_0_0.instance); // Split is for convenience, not because it *must* be cached
        public final Strategy strategy;

        InputType(Strategy strategy) {
            this.strategy = strategy;
        }
    }

    private final IOAgentTrackerFactory ioAgentTrackerFactory;
    private final StrIncrContext strContext;

    @Inject public SubFrontend(IOAgentTrackerFactory ioAgentTrackerFactory, StrIncrContext strContext) {
        this.ioAgentTrackerFactory = ioAgentTrackerFactory;
        this.strContext = strContext;
    }


    @Override public String getId() {
        return id;
    }

    @Override public Serializable key(SubFrontend.Input input) {
        return input.inputType.name() + ":" + input.moduleName + ":" + input.cifiedName;
    }


    @Override public SubFrontend.Output exec(ExecContext context, SubFrontend.Input input) throws Exception {
        /*
         * Note that we require the sdf tasks here because we may be reading a Stratego file that was generated by one
         * of those tasks and that dependency is not allowed to be hidden from the build system. To make sure that
         * front-end tasks only run when their input _files_ change, we need the front-end to depend on the sdf tasks
         * with a simple stamper that allows the execution of the sdf task to be ignored. The execution of the sdf task
         * is forced in the main task StrIncr before it starts frontend tasks and searches for Stratego files through
         * imports.
         */
        for(STask t : input.originTasks) {
            context.require(t, InconsequentialOutputStamper.instance);
        }
        if(input.inputType == InputType.TopLevelDefinition) {
            BuildStats.tldSubFrontendCTreeSize.put(input.toString(), TermSize.computeTermSize(input.ast));
        }
        final IOAgentTracker tracker = newResourceTracker(new File(System.getProperty("user.dir")), true);
        final StrategoExecutor.ExecutionResult result = Backend.runLocallyUniqueStringStrategy(context.logger(), true,
            tracker, input.inputType.strategy, input.ast,
            strContext);

        if(!result.success) {
            // TODO: temporary, remove once this stuff works
            context.logger().error("Call to strc frontend failed on " + input.toString() + ": \n" + result.strategoTrace + "\n" + tracker.stderr() + "\n" + tracker.stdout(), result.exception);
            return new Output(null);
//            throw new ExecException("Call to strc frontend failed on " + input.toString() + ": \n" + result.strategoTrace, result.exception);
        }
        return new Output(result.result);
    }

    private IOAgentTracker newResourceTracker(File baseFile, boolean silent, String... excludePatterns) {
        final IOAgentTracker tracker;
        if(silent) {
            tracker = ioAgentTrackerFactory.create(baseFile, new NullOutputStream(), new NullOutputStream());
        } else {
            tracker = ioAgentTrackerFactory.create(baseFile, excludePatterns);
        }
        return tracker;
    }

}
