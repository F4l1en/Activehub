/**
 * PromotionResult - the outcome of evaluating ONE promotion on ONE transaction.
 * Bill owns these results (COMPOSITION) so it can show every promotion that was evaluated.
 */
public class PromotionResult {
    private final Promotion promotion;
    private final boolean eligible;
    private final double saving;

    public PromotionResult(Promotion promotion, boolean eligible, double saving) {
        this.promotion = promotion;
        this.eligible = eligible;
        this.saving = saving;
    }

    public Promotion getPromotion() { return promotion; }

    public boolean isEligible() { return eligible; }

    public double getSaving() { return saving; }
}
