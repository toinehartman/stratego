package mb.stratego.build.strincr.message.java;

import org.spoofax.interpreter.terms.IStrategoString;

import mb.stratego.build.strincr.MessageSeverity;
import mb.stratego.build.strincr.message.JavaMessage;

public class ExternalStrategyOverlap extends JavaMessage<IStrategoString> {
    public ExternalStrategyOverlap(String module, IStrategoString name) {
        super(module, name, MessageSeverity.ERROR);
    }

    @Override public String getMessage() {
        return "Strategy '" + locationTerm.stringValue() + "' overlaps with an externally defined strategy";
    }
}