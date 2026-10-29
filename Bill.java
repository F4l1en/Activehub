import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Bill - the final bill of one RentalTransaction.
 * Calculation order: subtotal - discount (best promotion) = amount after discount;
 * 10% service charge is applied on the amount after discount; final = after discount + service charge.
 * OOP concepts: COMPOSITION (owns its PromotionResult objects), ASSOCIATION (RentalTransaction, Payment).
 */
public final class Bill {
    public static final double SERVICE_CHARGE_RATE = 0.10;

    private final RentalTransaction transaction;
    private final List<PromotionResult> results = new ArrayList<>();
    private PromotionResult selected;           // null when no promotion applies
    private String restoredPromoCode = "-";     // only used for bills loaded from file
    private double subtotal;
    private double discount;
    private double serviceCharge;
    private double finalAmount;
    private Payment payment;                    // null until the customer pays

    public Bill(RentalTransaction transaction, List<PromotionResult> results, PromotionResult selected) {
        this.transaction = transaction;
        this.results.addAll(results);
        this.selected = selected;
        calculate();
    }

    private Bill(RentalTransaction transaction, String promoCode, double subtotal, double discount,
                 double serviceCharge, double finalAmount) {
        this.transaction = transaction;
        this.restoredPromoCode = promoCode;
        this.subtotal = subtotal;
        this.discount = discount;
        this.serviceCharge = serviceCharge;
        this.finalAmount = finalAmount;
    }

    /** Rebuilds a paid bill from the text file with its saved (frozen) amounts. */
    public static Bill restore(RentalTransaction transaction, String promoCode, double subtotal,
                               double discount, double serviceCharge, double finalAmount) {
        return new Bill(transaction, promoCode, subtotal, discount, serviceCharge, finalAmount);
    }

    public void calculate() {
        subtotal = Promotion.round2(transaction.getSubtotal());
        discount = (selected == null) ? 0.0 : Promotion.round2(selected.getSaving());
        double afterDiscount = Promotion.round2(subtotal - discount);
        serviceCharge = Promotion.round2(afterDiscount * SERVICE_CHARGE_RATE);
        finalAmount = Promotion.round2(afterDiscount + serviceCharge);
    }

    public static String money(double value) { return String.format(Locale.US, "%.2f", value); }

    public RentalTransaction getTransaction() { return transaction; }

    public double getSubtotal() { return subtotal; }

    public double getDiscount() { return discount; }

    public double getServiceCharge() { return serviceCharge; }

    public double getFinalAmount() { return finalAmount; }

    public Payment getPayment() { return payment; }

    public void setPayment(Payment payment) { this.payment = payment; }

    public boolean isPaid() { return payment != null; }

    public String getSelectedPromoCode() {
        return selected != null ? selected.getPromotion().getCode() : restoredPromoCode;
    }

    public void display() {
        Customer c = transaction.getCustomer();
        Booking b = transaction.getBooking();
        System.out.println();
        System.out.println("==================== BILL - " + transaction.getTransactionId() + " ====================");
        System.out.println("Customer : " + c);
        System.out.println("Booking  : " + (b == null ? "Walk-in (no booking)" : b.toString()));
        System.out.println();
        transaction.displayLines();

        if (!results.isEmpty()) {
            System.out.println();
            System.out.println("Promotions evaluated:");
            for (PromotionResult r : results) {
                Promotion p = r.getPromotion();
                String info = r.isEligible() ? "ELIGIBLE      saving RM " + money(r.getSaving()) : "Not eligible";
                System.out.printf("  Promotion %s - %-27s %s%n", p.getCode(), p.getName(), info);
            }
            if (selected != null) {
                System.out.println("Selected promotion : " + selected.getPromotion().getCode() + " - "
                        + selected.getPromotion().getName() + " (greatest saving, promotions are not combined)");
            } else {
                System.out.println("Selected promotion : None (no eligible promotion)");
            }
        } else if (!"-".equals(restoredPromoCode)) {
            System.out.println("Selected promotion : " + restoredPromoCode);
        }

        System.out.println();
        System.out.printf("%-47s %22s%n", "Subtotal (RM)", money(subtotal));
        System.out.printf("%-47s %22s%n", "Discount (RM)", discount > 0 ? "-" + money(discount) : money(discount));
        System.out.printf("%-47s %22s%n", "Amount after discount (RM)", money(subtotal - discount));
        System.out.printf("%-47s %22s%n", "Service charge 10% (RM)", money(serviceCharge));
        System.out.println("-".repeat(70));
        System.out.printf("%-47s %22s%n", "FINAL PAYABLE (RM)", money(finalAmount));
        System.out.println("=".repeat(70));
        if (payment != null) {
            System.out.println("PAID by " + payment.getMethodName() + " | " + payment.getDetails()
                    + " | " + payment.getPaidAt().toString().replace('T', ' '));
        }
    }
}
