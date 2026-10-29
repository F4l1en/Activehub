/** Credit / debit card payment: only the card type and last 4 digits are kept (never the full number). */
public class CardPayment extends Payment {
    private final String cardType;
    private final String last4Digits;

    public CardPayment(String paymentId, double amount, String cardType, String last4Digits) {
        super(paymentId, amount);
        this.cardType = cardType;
        this.last4Digits = last4Digits;
    }

    @Override
    public boolean processPayment() {
        if (last4Digits == null || !last4Digits.matches("\\d{4}")) return false;
        if (!"Credit".equals(cardType) && !"Debit".equals(cardType)) return false;
        markPaid();
        return true;
    }

    @Override
    public String getMethodName() { return "Card"; }

    @Override
    public String getDetails() { return cardType + " card ending " + last4Digits; }

    @Override
    public String toFileDetail() { return cardType + "," + last4Digits; }
}
