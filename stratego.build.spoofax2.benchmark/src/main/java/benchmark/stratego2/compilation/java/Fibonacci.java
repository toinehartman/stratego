package benchmark.stratego2.compilation.java;

import benchmark.stratego2.problem.FibonacciProblem;
import benchmark.stratego2.template.benchmark.compilation.JavaCompilationBenchmark;
import org.openjdk.jmh.annotations.Param;

public class Fibonacci extends JavaCompilationBenchmark implements FibonacciProblem {

    @Param({"18", "19", "20", "21"})
    int problemSize;

}
