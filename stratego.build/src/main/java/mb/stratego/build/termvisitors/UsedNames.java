package mb.stratego.build.termvisitors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.stratego.build.strincr.StaticChecks;
import mb.stratego.build.util.Relation;
import mb.stratego.build.util.StringSetWithPositions;
import org.spoofax.terms.util.TermUtils;

public class UsedNames extends UsedConstrs {

    private final Deque<Set<String>> scopes = new ArrayDeque<>();
    private final Set<String> inScope = new HashSet<>();

    private final StringSetWithPositions usedStrats;
    private final Map<String, Set<String>> usedAmbStrats;
    private final StringSetWithPositions ambStratPositions;

    private @Nullable String currentTopLevelStrategyName = null;
    public int ambiguousStrategyNamesFound = 0;

    public UsedNames(StringSetWithPositions usedConstrs, StringSetWithPositions usedStrats,
        Map<String, Set<String>> usedAmbStrats, StringSetWithPositions ambStratPositions) {
        super(usedConstrs);

        this.usedStrats = usedStrats;
        this.usedAmbStrats = usedAmbStrats;
        this.ambStratPositions = ambStratPositions;
    }

    @Override public void preVisit(IStrategoTerm term) {
        enterTopLevelStrategy(term);
        enterScope(term);
        registerConsUse(term);
        registerAmbStratUse(term);
        registerStratUse(term);
    }

    private void enterTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName == null && TermUtils.isAppl(term) && TermUtils
            .isAppl(term, "SDefT", 4)) {
            currentTopLevelStrategyName = TermUtils.toJavaStringAt(term, 0);
        }
    }

    private void enterScope(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            boolean count = false;
            switch(TermUtils.tryGetName(term).orElse("")) {
                case "Let":
                    if(term.getSubtermCount() == 2) {
                        final IStrategoList defList = TermUtils.toListAt(term, 0);
                        final Set<String> defs = new HashSet<>(defList.size() * 2);
                        for(IStrategoTerm sdeft : defList) {
                            if(TermUtils.isAppl((IStrategoAppl) sdeft, "AnnoDef", 2)) {
                                sdeft = sdeft.getSubterm(1);
                            }
                            final String def = TermUtils.toJavaStringAt(sdeft, 0);
                            defs.add(def);
                            inScope.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
                case "ExtSDef":
                    count = term.getSubtermCount() == 3;
                case "SDefT":
                    count |= term.getSubtermCount() == 4;
                case "ExtSDefInl":
                    count |= term.getSubtermCount() == 4;
                    if(count) {
                        final IStrategoList svars = TermUtils.toListAt(term, 1);
                        final Set<String> defs = new HashSet<>(svars.size() * 2);
                        for(IStrategoTerm tvar : svars) {
                            final String def = TermUtils.toJavaStringAt(tvar, 0);
                            defs.add(def);
                            inScope.add(def);
                        }
                        scopes.push(defs);
                    }
                    break;
            }
        }
    }

    private void registerAmbStratUse(IStrategoTerm term) {
        if(TermUtils.isAppl(term) && TermUtils.isAppl((IStrategoAppl) term, "CallT", 3)) {
            final IStrategoList sargs = TermUtils.toListAt(term, 1);
            for(IStrategoTerm sarg : sargs) {
                if(TermUtils.isAppl(sarg) && TermUtils.isAppl((IStrategoAppl) sarg, "CallT", 3)) {
                    if(sarg.getSubterm(1).getSubtermCount() == 0 && sarg.getSubterm(2).getSubtermCount() == 0) {
                        // Mark svar so it is not counted as a normal strategy use
                        sarg.getSubterm(0).putAttachment(AmbUseAttachment.INSTANCE);

                        final IStrategoString ambNameAST = TermUtils.toStringAt(TermUtils.toApplAt(sarg, 0), 0);
                        final String ambName = ambNameAST.stringValue();
                        if(!ambName.endsWith("_0_0")) {
                            // Inner strategies that were lifted don't have any arity info in their name and aren't ambiguous uses
                            if(!StaticChecks.stripArityPattern.matcher(ambName).matches()) {
                                continue;
                            }
                        }
                        Relation.getOrInitialize(usedAmbStrats, ambName, HashSet::new).add(currentTopLevelStrategyName);
                        ambStratPositions.add(ambNameAST);
                        ambiguousStrategyNamesFound++;
                    }
                }
            }
        }
    }

    private void registerStratUse(IStrategoTerm term) {
        if(TermUtils.isAppl(term) && TermUtils.isAppl((IStrategoAppl) term, "SVar", 1)
            && term.getAttachment(AmbUseAttachment.TYPE) == null) {
            IStrategoString strategyName = TermUtils.toStringAt(term, 0);
            if(!inScope.contains(strategyName.stringValue())) {
                usedStrats.add(strategyName);
            }
        }
    }

    @Override public void postVisit(IStrategoTerm term) {
        leaveScope(term);
        leaveTopLevelStrategy(term);
    }

    private void leaveScope(IStrategoTerm term) {
        if(TermUtils.isAppl(term)) {
            boolean count = false;
            switch(TermUtils.tryGetName(term).orElse("")) {
                case "Let":
                    count = term.getSubtermCount() == 2;
                case "ExtSDef":
                    count |= term.getSubtermCount() == 3;
                case "SDefT":
                    count |= term.getSubtermCount() == 4;
                case "ExtSDefInl":
                    count |= term.getSubtermCount() == 4;
                    if(count) {
                        inScope.removeAll(scopes.pop());
                    }
                    break;
            }
        }
    }

    private void leaveTopLevelStrategy(IStrategoTerm term) {
        if(currentTopLevelStrategyName != null && TermUtils.isAppl(term) && TermUtils
            .isAppl(term, "SDefT", 4) && currentTopLevelStrategyName
            .equals(TermUtils.toJavaStringAt(term, 0))) {
            currentTopLevelStrategyName = null;
        }
    }

}