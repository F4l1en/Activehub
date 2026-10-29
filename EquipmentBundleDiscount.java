/**
 * Promotion C - Equipment Bundle Discount: fixed RM20 off the bill when the transaction has at least
 * one facility / court booking AND 3 or more equipment units (same or different items).
 * Accessories, facility packages and add-on services are not counted as equipment units.
 */
public class EquipmentBundleDiscount implements Promotion {
    private static final int MIN_EQUIPMENT_UNITS = 3;
    private static final double DISCOUNT = 20.0;

    @Override
    public String getCode() { return "C"; }

    @Override
    public String getName() { return "Equipment Bundle Discount"; }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        return tx.hasFacilityBooking() && tx.getEquipmentUnits() >= MIN_EQUIPMENT_UNITS;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        // The discount can never be larger than the bill itself.
        return Promotion.round2(Math.min(DISCOUNT, tx.getSubtotal()));
    }
}
