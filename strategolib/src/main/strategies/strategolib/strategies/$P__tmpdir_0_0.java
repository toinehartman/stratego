package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class $P__tmpdir_0_0 extends Strategy {
    public static $P__tmpdir_0_0 instance = new $P__tmpdir_0_0();

    /**
     * Stratego 2 type: {@code P_tmpdir :: (|) ? -> string}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        return context.getFactory().makeString(context.getIOAgent().getTempDir());
    }
}
