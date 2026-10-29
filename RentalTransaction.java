import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * RentalTransaction - the rental request of one customer (optionally linked to a Booking).
 * OOP concepts: COMPOSITION (it owns its TransactionLine objects - lines have no life outside it),
 * ASSOCIATION (Customer, Booking), ENCAPSULATION (lines can only change through its methods).
 */
public class RentalTransaction {
    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_COMPLETED = "COMPLETED";

    private final String transactionId;
    private final Customer customer;
    private final Booking booking;            // null for a walk-in customer
    private final List<TransactionLine> lines = new ArrayList<>();
    private String status = STATUS_OPEN;

    public RentalTransaction(String transactionId, Customer customer, Booking booking) {
        this.transactionId = transactionId;
        this.customer = customer;
        this.booking = booking;
    }

    public String getTransactionId() { return transactionId; }

    public Customer getCustomer() { return customer; }

    public Booking getBooking() { return booking; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public boolean isOpen() { return STATUS_OPEN.equals(status); }

    public boolean isEmpty() { return lines.isEmpty(); }

    public List<TransactionLine> getLines() { return Collections.unmodifiableList(lines); }

    public TransactionLine findLine(String itemCode) {
        for (TransactionLine line : lines) {
            if (line.getItem().getItemCode().equalsIgnoreCase(itemCode)) return line;
        }
        return null;
    }

    /** Adds an item; if the item is already in the transaction its quantity is increased. */
    public void addItem(RentalItem item, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1.");
        TransactionLine existing = findLine(item.getItemCode());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            lines.add(new TransactionLine(item, quantity));
        }
    }

    /** Sets a new quantity for an item already in the transaction; 0 removes the line. */
    public boolean updateQuantity(String itemCode, int newQuantity) {
        TransactionLine line = findLine(itemCode);
        if (line == null || newQuantity < 0) return false;
        if (newQuantity == 0) {
            lines.remove(line);
        } else {
            line.setQuantity(newQuantity);
        }
        return true;
    }

    /** Sum of facility / court package lines (the base for Promotions A and B). */
    public double getFacilityCharge() {
        double total = 0;
        for (TransactionLine line : lines) {
            if (line.getItem().isFacility()) total += line.getLineTotal();
        }
        return total;
    }

    /** Total quantity of Equipment items only (accessories and facilities are not counted). */
    public int getEquipmentUnits() {
        int units = 0;
        for (TransactionLine line : lines) {
            if (line.getItem().isEquipment()) units += line.getQuantity();
        }
        return units;
    }

    /** True when the transaction contains at least one facility / court booking line. */
    public boolean hasFacilityBooking() {
        for (TransactionLine line : lines) {
            if (line.getItem().isFacility() && line.getQuantity() > 0) return true;
        }
        return false;
    }

    public double getSubtotal() {
        double total = 0;
        for (TransactionLine line : lines) total += line.getLineTotal();
        return total;
    }

    public void displayLines() {
        System.out.printf("%-6s %-34s %5s %10s %11s%n", "Code", "Item", "Qty", "Price", "Total");
        System.out.println("-".repeat(70));
        for (TransactionLine line : lines) {
            System.out.printf(Locale.US, "%-6s %-34s %5d %10.2f %11.2f%n", line.getItem().getItemCode(),
                    line.getItem().getItemName(), line.getQuantity(), line.getItem().getRentalPrice(),
                    line.getLineTotal());
        }
        System.out.println("-".repeat(70));
        System.out.printf(Locale.US, "%-47s %22.2f%n", "Subtotal (RM)", getSubtotal());
    }
}
