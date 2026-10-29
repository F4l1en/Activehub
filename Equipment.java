/**
 * Equipment - physical sports equipment (racquets, balls, ...). Counted as equipment units by Promotion C.
 * OOP concept: INHERITANCE (extends RentalItem) and POLYMORPHISM (overrides getItemType).
 */
public class Equipment extends RentalItem {
    public Equipment(String itemCode, String itemName, String category, double rentalPrice) {
        super(itemCode, itemName, category, rentalPrice);
    }

    @Override
    public String getItemType() { return TYPE_EQUIPMENT; }
}
