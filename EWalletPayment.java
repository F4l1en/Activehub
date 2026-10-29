/** E-wallet payment: needs the wallet provider and a transaction reference number. */
public class EWalletPayment extends Payment {
    private final String provider;
    private final String referenceNo;

    public EWalletPayment(String paymentId, double amount, String provider, String referenceNo) {
        super(paymentId, amount);
        this.provider = provider;
        this.referenceNo = referenceNo;
    }

    @Override
    public boolean processPayment() {
        if (provider == null || provider.isEmpty()) return false;
        if (referenceNo == null || !referenceNo.matches("[A-Za-z0-9]{4,20}")) return false;
        markPaid();
        return true;
    }

    @Override
    public String getMethodName() { return "E-Wallet"; }

    @Override
    public String getDetails() { return provider + " ref " + referenceNo; }

    @Override
    public String toFileDetail() { return provider + "," + referenceNo; }
}
