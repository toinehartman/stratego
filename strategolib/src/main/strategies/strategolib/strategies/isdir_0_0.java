package strategolib.strategies;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.util.TermUtils;
import org.strategoxt.lang.Context;
import org.strategoxt.lang.Strategy;

public class isdir_0_0 extends Strategy {
    public static isdir_0_0 instance = new isdir_0_0();

    /**
     * Stratego 2 type: {@code isdir :: (|) FileMode -> FileMode}
     */
    @Override public IStrategoTerm invoke(Context context, IStrategoTerm current) {
        if((TermUtils.toJavaInt(current) & filemode_0_0.S_IFDIR) != 0) {
            return current;
        }
        return null;
    }
}
