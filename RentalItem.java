/**
 * RentalItem - ABSTRACT parent of everything that can be rented or sold from the catalogue.
 * OOP concepts: ABSTRACTION (abstract getItemType), INHERITANCE (Equipment, FacilityPackage,
 * Accessory extend it), ENCAPSULATION, POLYMORPHISM (callers use RentalItem references).
 */
public abstract class RentalItem {
    public static final String TYPE_EQUIPMENT = "Equipment";
    public static final String TYPE_FACILITY = "Facility";
    public static final String TYPE_ACCESSORY = "Accessory";

    private final String itemCode;
    private final String itemName;
    private final String category;
    private final double rentalPrice;

    protected RentalItem(String itemCode, String itemName, String category, double rentalPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.category = category;
        this.rentalPrice = rentalPrice;
    }

    public String getItemCode() { return itemCode; }

    public String getItemName() { return itemName; }

    public String getCategory() { return category; }

    public double getRentalPrice() { return rentalPrice; }

    /** Each subclass states what kind of item it is. */
    public abstract String getItemType();

    public boolean isEquipment() { return TYPE_EQUIPMENT.equals(getItemType()); }

    public boolean isFacility() { return TYPE_FACILITY.equals(getItemType()); }
}
