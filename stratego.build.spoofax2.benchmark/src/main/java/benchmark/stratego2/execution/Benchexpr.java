package benchmark.stratego2.execution;

import benchmark.stratego2.problem.BenchexprProblem;
import benchmark.stratego2.template.benchmark.execution.ExecutionBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Benchexpr extends ExecutionBenchmark implements BenchexprProblem {

    @Param({"10", "15", "20", "22"})
    int problemSize;

}
