/**
 * Accessory - an accessory or add-on service (locker, referee, ...). Never counted as equipment.
 * OOP concept: INHERITANCE (extends RentalItem) and POLYMORPHISM (overrides getItemType).
 */
public class Accessory extends RentalItem {
    public Accessory(String itemCode, String itemName, String category, double rentalPrice) {
        super(itemCode, itemName, category, rentalPrice);
    }

    @Override
    public String getItemType() { return TYPE_ACCESSORY; }
}
