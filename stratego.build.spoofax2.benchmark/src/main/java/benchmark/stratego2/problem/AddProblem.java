package benchmark.stratego2.problem;

import benchmark.stratego2.template.problem.Problem;

public interface AddProblem extends Problem {

    @Override
    default String problemFileNamePattern() {
        return "add%d.str2";
    }
}