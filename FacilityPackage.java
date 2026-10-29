/**
 * FacilityPackage - a court / facility booking charge. Promotions A and B discount only these charges.
 * OOP concept: INHERITANCE (extends RentalItem) and POLYMORPHISM (overrides getItemType).
 */
public class FacilityPackage extends RentalItem {
    public FacilityPackage(String itemCode, String itemName, String category, double rentalPrice) {
        super(itemCode, itemName, category, rentalPrice);
    }

    @Override
    public String getItemType() { return TYPE_FACILITY; }
}
