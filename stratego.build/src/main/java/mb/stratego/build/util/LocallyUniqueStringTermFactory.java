package mb.stratego.build.util;

import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.StrategoString;
import org.spoofax.terms.attachments.AbstractWrappedTermFactory;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * This TermFactory is similar to {@link org.spoofax.terms.TermFactory}, but has a local usedStrings set. Therefore the
 * usedStrings will not be shared by different instances of the factory, which is used by the separate compiler for
 * Stratego to make newname only locally unique to each file that is processed. The base factory is still called to
 * build a string so it can record its usage. But the locally built IStrategoString is returned.
 */
public class LocallyUniqueStringTermFactory extends AbstractWrappedTermFactory {
    private static final int MAX_POOLED_STRING_LENGTH = 200;
    private final Set<String> usedStrings = new HashSet<>();
    private final ITermFactory baseFactory;

    public LocallyUniqueStringTermFactory(ITermFactory baseFactory) {
        super(baseFactory.getDefaultStorageType(), baseFactory);
        this.baseFactory = baseFactory;
    }

    @Override public ITermFactory getFactoryWithStorageType(int storageType) {
        assert getDefaultStorageType() <= storageType;
        return this;
    }

    @Override public IStrategoString makeString(String s) {
        final IStrategoString string = new StrategoString(s, null, defaultStorageType);
        if(s.length() <= MAX_POOLED_STRING_LENGTH) {
            synchronized(usedStrings) {
                usedStrings.add(s);
            }
        }
        // Run the base factory too in case it wants to register strings
        baseFactory.makeString(s);
        return string;
    }

    @Override public @Nullable IStrategoString tryMakeUniqueString(String name) {
        synchronized(usedStrings) {
            if(usedStrings.contains(name)) {
                return null;
            } else if(name.length() > MAX_POOLED_STRING_LENGTH) {
                throw new UnsupportedOperationException("String too long to be pooled (newname not allowed): " + name);
            } else {
                return makeString(name);
            }
        }
    }
}