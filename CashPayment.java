/** Cash payment: succeeds only when the cash handed over covers the amount; change is calculated. */
public class CashPayment extends Payment {
    private final double amountTendered;

    public CashPayment(String paymentId, double amount, double amountTendered) {
        super(paymentId, amount);
        this.amountTendered = amountTendered;
    }

    public double getAmountTendered() { return amountTendered; }

    public double calculateChange() { return Promotion.round2(amountTendered - getAmount()); }

    @Override
    public boolean processPayment() {
        if (amountTendered + 0.001 < getAmount()) return false;
        markPaid();
        return true;
    }

    @Override
    public String getMethodName() { return "Cash"; }

    @Override
    public String getDetails() {
        return "Tendered RM " + Bill.money(amountTendered) + ", Change RM " + Bill.money(calculateChange());
    }

    @Override
    public String toFileDetail() { return Bill.money(amountTendered); }
}
