package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface BenchNullaryProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "benchnullary%d.str2";
    }
}
