/**
 * Promotion - INTERFACE implemented by every promotion rule.
 * OOP concepts: ABSTRACTION (what a promotion must do, not how) and POLYMORPHISM
 * (PromotionEngine calls isEligible / calculateSaving on the interface type;
 * each class answers with its own rule).
 */
public interface Promotion {
    String getCode();

    String getName();

    boolean isEligible(RentalTransaction tx);

    double calculateSaving(RentalTransaction tx);

    /** Rounds money to 2 decimal places so savings can be compared reliably. */
    static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
