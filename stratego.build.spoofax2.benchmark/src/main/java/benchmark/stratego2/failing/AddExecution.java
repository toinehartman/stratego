package benchmark.stratego2.failing;

import benchmark.stratego2.problem.AddProblem;
import benchmark.stratego2.template.benchmark.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class AddExecution extends ExecutionBenchmark implements AddProblem {

    @Param({"8", "16", "32"})
    int problemSize;

}
