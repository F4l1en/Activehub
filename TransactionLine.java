/**
 * TransactionLine - one catalogue item and its quantity inside a RentalTransaction.
 * OOP concept: ASSOCIATION to RentalItem (the line refers to the catalogue item, it does not copy it).
 */
public class TransactionLine {
    private final RentalItem item;
    private int quantity;

    public TransactionLine(RentalItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public RentalItem getItem() { return item; }

    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getLineTotal() { return item.getRentalPrice() * quantity; }
}
