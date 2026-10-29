import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * FileHandler - reads and writes the simple line-format text files (fields separated by '|').
 *   catalogue.txt     type|code|name|category|price
 *   bookings.txt      id|name|contact|date|time|participants|facilityId|status
 *   transactions.txt  id|bookingId or -|name|contact|status|code:qty;code:qty
 *   bills.txt         txId|promoCode|subtotal|discount|service|final|method|detail|paymentId|paidAt
 * Malformed lines are skipped with a warning so one bad line never stops the program.
 */
public class FileHandler {
    private final String dataFolder;

    public FileHandler(String dataFolder) {
        this.dataFolder = dataFolder;
        new File(dataFolder).mkdirs();
    }

    private Path path(String fileName) { return Paths.get(dataFolder, fileName); }

    private List<String> readLines(String fileName) {
        List<String> lines = new ArrayList<>();
        Path p = path(fileName);
        if (!Files.exists(p)) return lines;
        try (BufferedReader reader = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) lines.add(line);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + fileName + " (" + e.getMessage() + ")");
        }
        return lines;
    }

    private void writeLines(String fileName, List<String> lines) {
        try (BufferedWriter writer = Files.newBufferedWriter(path(fileName), StandardCharsets.UTF_8)) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save " + fileName + " (" + e.getMessage() + ")");
        }
    }

    private static String[] split(String line) { return line.split("\\|", -1); }

    private static void skipped(String fileName, String line) {
        System.out.println("Warning: skipped invalid line in " + fileName + ": " + line);
    }

    // ---------------------------------------------------------------- catalogue
    public void saveCatalogue(List<RentalItem> items) {
        List<String> lines = new ArrayList<>();
        for (RentalItem i : items) {
            lines.add(i.getItemType() + "|" + i.getItemCode() + "|" + i.getItemName() + "|" + i.getCategory()
                    + "|" + String.format(Locale.US, "%.2f", i.getRentalPrice()));
        }
        writeLines("catalogue.txt", lines);
    }

    public List<RentalItem> loadCatalogue() {
        List<RentalItem> items = new ArrayList<>();
        for (String line : readLines("catalogue.txt")) {
            try {
                String[] f = split(line);
                double price = Double.parseDouble(f[4]);
                switch (f[0]) {
                    case RentalItem.TYPE_EQUIPMENT: items.add(new Equipment(f[1], f[2], f[3], price)); break;
                    case RentalItem.TYPE_FACILITY: items.add(new FacilityPackage(f[1], f[2], f[3], price)); break;
                    case RentalItem.TYPE_ACCESSORY: items.add(new Accessory(f[1], f[2], f[3], price)); break;
                    default: skipped("catalogue.txt", line);
                }
            } catch (RuntimeException e) {
                skipped("catalogue.txt", line);
            }
        }
        return items;
    }

    // ---------------------------------------------------------------- bookings
    public void saveBookings(List<Booking> bookings) {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) {
            lines.add(b.getBookingId() + "|" + b.getCustomer().getName() + "|" + b.getCustomer().getContactNumber()
                    + "|" + b.getBookingDate() + "|" + b.getStartTime() + "|" + b.getParticipants()
                    + "|" + b.getFacility().getFacilityId() + "|" + b.getStatus());
        }
        writeLines("bookings.txt", lines);
    }

    public List<Booking> loadBookings(List<Facility> facilities) {
        List<Booking> bookings = new ArrayList<>();
        for (String line : readLines("bookings.txt")) {
            try {
                String[] f = split(line);
                Facility facility = null;
                for (Facility fac : facilities) {
                    if (fac.getFacilityId().equals(f[6])) facility = fac;
                }
                if (facility == null) { skipped("bookings.txt", line); continue; }
                bookings.add(new Booking(f[0], new Customer(f[1], f[2]), facility, LocalDate.parse(f[3]),
                        LocalTime.parse(f[4]), Integer.parseInt(f[5]), f[7]));
            } catch (RuntimeException e) {
                skipped("bookings.txt", line);
            }
        }
        return bookings;
    }

    // ---------------------------------------------------------------- transactions
    public void saveTransactions(List<RentalTransaction> transactions) {
        List<String> lines = new ArrayList<>();
        for (RentalTransaction t : transactions) {
            StringBuilder items = new StringBuilder();
            for (TransactionLine l : t.getLines()) {
                if (items.length() > 0) items.append(';');
                items.append(l.getItem().getItemCode()).append(':').append(l.getQuantity());
            }
            lines.add(t.getTransactionId() + "|" + (t.getBooking() == null ? "-" : t.getBooking().getBookingId())
                    + "|" + t.getCustomer().getName() + "|" + t.getCustomer().getContactNumber()
                    + "|" + t.getStatus() + "|" + (items.length() == 0 ? "-" : items));
        }
        writeLines("transactions.txt", lines);
    }

    public List<RentalTransaction> loadTransactions(RentalCatalogue catalogue, BookingManager bookingManager) {
        List<RentalTransaction> list = new ArrayList<>();
        for (String line : readLines("transactions.txt")) {
            try {
                String[] f = split(line);
                Booking booking = "-".equals(f[1]) ? null : bookingManager.findById(f[1]);
                RentalTransaction t = new RentalTransaction(f[0], new Customer(f[2], f[3]), booking);
                t.setStatus(f[4]);
                if (!"-".equals(f[5])) {
                    for (String part : f[5].split(";")) {
                        String[] cq = part.split(":");
                        RentalItem item = catalogue.findByCode(cq[0]);
                        if (item != null) t.addItem(item, Integer.parseInt(cq[1]));
                    }
                }
                list.add(t);
            } catch (RuntimeException e) {
                skipped("transactions.txt", line);
            }
        }
        return list;
    }

    // ---------------------------------------------------------------- bills (paid)
    public void saveBills(List<Bill> bills) {
        List<String> lines = new ArrayList<>();
        for (Bill b : bills) {
            if (!b.isPaid()) continue;
            Payment p = b.getPayment();
            lines.add(b.getTransaction().getTransactionId() + "|" + b.getSelectedPromoCode()
                    + "|" + Bill.money(b.getSubtotal()) + "|" + Bill.money(b.getDiscount())
                    + "|" + Bill.money(b.getServiceCharge()) + "|" + Bill.money(b.getFinalAmount())
                    + "|" + p.getMethodName() + "|" + p.toFileDetail() + "|" + p.getPaymentId() + "|" + p.getPaidAt());
        }
        writeLines("bills.txt", lines);
    }

    public List<Bill> loadBills(List<RentalTransaction> transactions) {
        List<Bill> bills = new ArrayList<>();
        for (String line : readLines("bills.txt")) {
            try {
                String[] f = split(line);
                RentalTransaction tx = null;
                for (RentalTransaction t : transactions) {
                    if (t.getTransactionId().equals(f[0])) tx = t;
                }
                if (tx == null) { skipped("bills.txt", line); continue; }
                double finalAmount = Double.parseDouble(f[5]);
                String[] d = f[7].split(",");
                Payment payment;
                switch (f[6]) {
                    case "Cash": payment = new CashPayment(f[8], finalAmount, Double.parseDouble(d[0])); break;
                    case "Card": payment = new CardPayment(f[8], finalAmount, d[0], d[1]); break;
                    case "E-Wallet": payment = new EWalletPayment(f[8], finalAmount, d[0], d[1]); break;
                    default: skipped("bills.txt", line); continue;
                }
                payment.setPaidAt(LocalDateTime.parse(f[9]));
                Bill bill = Bill.restore(tx, f[1], Double.parseDouble(f[2]), Double.parseDouble(f[3]),
                        Double.parseDouble(f[4]), finalAmount);
                bill.setPayment(payment);
                bills.add(bill);
            } catch (RuntimeException e) {
                skipped("bills.txt", line);
            }
        }
        return bills;
    }
}
