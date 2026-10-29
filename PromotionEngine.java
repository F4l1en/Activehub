import java.util.ArrayList;
import java.util.List;

/**
 * PromotionEngine - the rule-based engine. It evaluates ALL promotions and selects the single best one.
 * OOP concepts: POLYMORPHISM (the loop treats every rule as a Promotion and calls the same methods;
 * each rule runs its own logic), AGGREGATION (promotions are created outside and handed to the engine).
 */
public class PromotionEngine {
    private final List<Promotion> promotions = new ArrayList<>();

    public PromotionEngine(List<Promotion> promotions) {
        this.promotions.addAll(promotions);
    }

    /** Evaluates every promotion for the transaction (eligible or not). */
    public List<PromotionResult> evaluateAll(RentalTransaction tx) {
        List<PromotionResult> results = new ArrayList<>();
        for (Promotion p : promotions) {
            boolean eligible = p.isEligible(tx);                       // polymorphic call
            double saving = eligible ? p.calculateSaving(tx) : 0.0;    // polymorphic call
            results.add(new PromotionResult(p, eligible, saving));
        }
        return results;
    }

    /**
     * Picks the eligible promotion with the greatest saving. Promotions cannot be combined.
     * If savings are equal, the lower alphabetical code wins (A before B before C).
     * Returns null when nothing is eligible.
     */
    public PromotionResult selectBest(List<PromotionResult> results) {
        PromotionResult best = null;
        for (PromotionResult r : results) {
            if (!r.isEligible() || r.getSaving() <= 0) continue;
            if (best == null) {
                best = r;
                continue;
            }
            long rCents = Math.round(r.getSaving() * 100);
            long bestCents = Math.round(best.getSaving() * 100);
            boolean greater = rCents > bestCents;
            boolean tieWithLowerCode = rCents == bestCents
                    && r.getPromotion().getCode().compareTo(best.getPromotion().getCode()) < 0;
            if (greater || tieWithLowerCode) best = r;
        }
        return best;
    }
}
