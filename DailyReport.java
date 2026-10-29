import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * DailyReport - end-of-day summary for the manager.
 * Bookings are counted by booking date; transactions and revenue by the date the payment was made.
 */
public class DailyReport {
    private final LocalDate reportDate;
    private final List<Booking> bookings;
    private final List<Bill> bills;

    public DailyReport(LocalDate reportDate, List<Booking> bookings, List<Bill> bills) {
        this.reportDate = reportDate;
        this.bookings = bookings;
        this.bills = bills;
    }

    public int totalBookings() {
        int count = 0;
        for (Booking b : bookings) {
            if (b.isActive() && b.getBookingDate().equals(reportDate)) count++;
        }
        return count;
    }

    /** Active bookings on file for any date (helps when the demo books future dates). */
    public int totalBookingsAllDates() {
        int count = 0;
        for (Booking b : bookings) {
            if (b.isActive()) count++;
        }
        return count;
    }

    public int cancelledBookings() {
        int count = 0;
        for (Booking b : bookings) {
            if (!b.isActive() && b.getBookingDate().equals(reportDate)) count++;
        }
        return count;
    }

    private boolean paidOnReportDate(Bill bill) {
        return bill.isPaid() && bill.getPayment().getPaidAt() != null
                && bill.getPayment().getPaidAt().toLocalDate().equals(reportDate);
    }

    public int completedTransactions() {
        int count = 0;
        for (Bill bill : bills) {
            if (paidOnReportDate(bill)) count++;
        }
        return count;
    }

    public double totalRevenue() {
        double total = 0;
        for (Bill bill : bills) {
            if (paidOnReportDate(bill)) total += bill.getPayment().getAmount();
        }
        return Promotion.round2(total);
    }

    /** Item (or facility) with the highest total quantity rented; ties go to the alphabetically first name. */
    public String mostRentedItem() {
        Map<String, Integer> quantities = new TreeMap<>();
        for (Bill bill : bills) {
            if (!paidOnReportDate(bill)) continue;
            for (TransactionLine line : bill.getTransaction().getLines()) {
                quantities.merge(line.getItem().getItemName(), line.getQuantity(), Integer::sum);
            }
        }
        String best = null;
        int bestQty = 0;
        for (Map.Entry<String, Integer> e : quantities.entrySet()) {
            if (e.getValue() > bestQty) {
                best = e.getKey();
                bestQty = e.getValue();
            }
        }
        return best == null ? "No rentals recorded" : best + " (" + bestQty + " rented)";
    }

    /** Number of payments per payment method. */
    public Map<String, Integer> paymentSummary() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("Cash", 0);
        summary.put("Card", 0);
        summary.put("E-Wallet", 0);
        for (Bill bill : bills) {
            if (paidOnReportDate(bill)) summary.merge(bill.getPayment().getMethodName(), 1, Integer::sum);
        }
        return summary;
    }

    private double amountFor(String method) {
        double total = 0;
        for (Bill bill : bills) {
            if (paidOnReportDate(bill) && bill.getPayment().getMethodName().equals(method)) {
                total += bill.getPayment().getAmount();
            }
        }
        return total;
    }

    public void display() {
        System.out.println();
        System.out.println("============ DAILY REPORT - " + reportDate + " (" + reportDate.getDayOfWeek() + ") ============");
        System.out.println("Total facility bookings (this date): " + totalBookings());
        System.out.println("Cancelled bookings (this date)     : " + cancelledBookings());
        System.out.println("Active bookings on file (all dates): " + totalBookingsAllDates());
        System.out.println("Completed rental transactions     : " + completedTransactions());
        System.out.println("Total daily revenue (RM)          : " + Bill.money(totalRevenue()));
        System.out.println("Most frequently rented item       : " + mostRentedItem());
        System.out.println("Payment methods used:");
        for (Map.Entry<String, Integer> e : paymentSummary().entrySet()) {
            System.out.printf(Locale.US, "  %-10s %3d payment(s)   RM %9.2f%n", e.getKey(), e.getValue(), amountFor(e.getKey()));
        }
        System.out.println("=".repeat(60));
    }
}
