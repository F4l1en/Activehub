import java.time.LocalDateTime;

/**
 * Payment - ABSTRACT payment method.
 * OOP concepts: ABSTRACTION, INHERITANCE (CashPayment, CardPayment, EWalletPayment) and
 * POLYMORPHISM: the program calls processPayment() on a Payment reference and the correct
 * method-specific validation runs at runtime.
 */
public abstract class Payment {
    private final String paymentId;
    private final double amount;
    private LocalDateTime paidAt;

    protected Payment(String paymentId, double amount) {
        this.paymentId = paymentId;
        this.amount = amount;
    }

    public String getPaymentId() { return paymentId; }

    public double getAmount() { return amount; }

    public LocalDateTime getPaidAt() { return paidAt; }

    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    protected void markPaid() { this.paidAt = LocalDateTime.now().withNano(0); }

    /** Validates the payment details and records the payment. Returns false when it fails. */
    public abstract boolean processPayment();

    public abstract String getMethodName();

    /** Human-readable details shown on the receipt. */
    public abstract String getDetails();

    /** Compact details stored in the text file (comma separated). */
    public abstract String toFileDetail();
}
