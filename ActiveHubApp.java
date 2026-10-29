import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * ActiveHubApp - the console menu (controller). It creates the manager objects and coordinates the modules.
 * Run from the folder that contains the .java files:  javac *.java   then   java ActiveHubApp
 * Data is stored in the "data" folder as simple text files.
 */
public class ActiveHubApp {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");
    private static final String[] WALLETS = {"TouchNGo", "GrabPay", "Boost", "ShopeePay"};

    private final Scanner scanner = new Scanner(System.in);
    private final FileHandler fileHandler = new FileHandler("data");
    private final BookingManager bookingManager = new BookingManager();
    private final RentalCatalogue catalogue = new RentalCatalogue();
    private final PromotionEngine promotionEngine = new PromotionEngine(
            Arrays.asList(new OffPeakSaver(), new TeamBookingReward(), new EquipmentBundleDiscount()));
    private final List<RentalTransaction> transactions = new ArrayList<>();
    private final List<Bill> bills = new ArrayList<>();      // paid bills

    public static void main(String[] args) {
        new ActiveHubApp().run();
    }

    // ================================================================ start-up / shutdown
    private void run() {
        loadData();
        try {
            boolean running = true;
            while (running) {
                System.out.println();
                System.out.println("===== ACTIVEHUB SYSTEM =====");
                System.out.println("1. Facility Booking Module");
                System.out.println("2. View Rental Catalogue");
                System.out.println("3. Create Rental Transaction");
                System.out.println("4. Apply Promotion");
                System.out.println("5. Make Payment");
                System.out.println("6. Daily Report");
                System.out.println("7. Exit");
                switch (readInt("Enter choice (1-7): ", 1, 7)) {
                    case 1: bookingMenu(); break;
                    case 2: catalogue.displayAll(); break;
                    case 3: transactionMenu(); break;
                    case 4: applyPromotion(); break;
                    case 5: makePayment(); break;
                    case 6: dailyReport(); break;
                    default: running = false;
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Saving data and exiting.");
        }
        saveAll();
        System.out.println("Data saved. Thank you for using ActiveHub!");
    }

    private void loadData() {
        for (RentalItem item : fileHandler.loadCatalogue()) catalogue.addItem(item);
        if (catalogue.size() == 0) {
            seedDefaultCatalogue();
            fileHandler.saveCatalogue(catalogue.getItems());
        }
        bookingManager.setBookings(fileHandler.loadBookings(bookingManager.getFacilities()));
        transactions.addAll(fileHandler.loadTransactions(catalogue, bookingManager));
        bills.addAll(fileHandler.loadBills(transactions));
    }

    /** The 8 catalogue items: 4 equipment, 2 facility packages, 2 accessories / add-on services. */
    private void seedDefaultCatalogue() {
        catalogue.addItem(new Equipment("E001", "Badminton Racquet", "Racquet Sports", 5.00));
        catalogue.addItem(new Equipment("E002", "Shuttlecock Set (12 pcs)", "Racquet Sports", 8.00));
        catalogue.addItem(new Equipment("E003", "Futsal Ball", "Ball Sports", 6.00));
        catalogue.addItem(new Equipment("E004", "Basketball", "Ball Sports", 6.00));
        catalogue.addItem(new FacilityPackage("F001", "Badminton Court (1 hour)", "Court Booking", 30.00));
        catalogue.addItem(new FacilityPackage("F002", "Futsal / Basketball Hall (1 hour)", "Court Booking", 80.00));
        catalogue.addItem(new Accessory("A001", "Locker Rental", "Add-on", 4.00));
        catalogue.addItem(new Accessory("A002", "Referee Service", "Add-on Service", 25.00));
    }

    private void saveAll() {
        fileHandler.saveCatalogue(catalogue.getItems());
        fileHandler.saveBookings(bookingManager.getBookings());
        fileHandler.saveTransactions(transactions);
        fileHandler.saveBills(bills);
    }

    // ================================================================ input helpers
    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            String s = ask(prompt);
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            System.out.println("  Invalid input. Please enter a whole number from " + min + " to " + max + ".");
        }
    }

    private String readText(String prompt) {
        while (true) {
            String s = ask(prompt);
            if (s.isEmpty()) System.out.println("  This field cannot be empty.");
            else if (s.contains("|")) System.out.println("  The '|' character is not allowed.");
            else if (s.length() > 40) System.out.println("  Maximum 40 characters.");
            else return s;
        }
    }

    private String readContact(String prompt) {
        while (true) {
            String s = ask(prompt);
            if (s.matches("[0-9+\\-]{8,15}")) return s;
            System.out.println("  Invalid contact number. Use 8-15 digits (e.g. 0123456789).");
        }
    }

    private LocalDate readBookingDate(String prompt) {
        while (true) {
            String s = ask(prompt);
            try {
                LocalDate d = LocalDate.parse(s);
                if (d.isBefore(LocalDate.now())) {
                    System.out.println("  The booking date cannot be in the past (today is " + LocalDate.now() + ").");
                } else {
                    return d;
                }
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid date. Use the format yyyy-MM-dd (e.g. 2026-11-16).");
            }
        }
    }

    private LocalTime readTime(String prompt) {
        while (true) {
            String s = ask(prompt);
            try {
                LocalTime t = LocalTime.parse(s, TIME_FORMAT);
                if (bookingManager.isWithinOpeningHours(t)) return t;
                System.out.println("  The centre is open " + BookingManager.OPENING_TIME + " - "
                        + BookingManager.CLOSING_TIME + ". Start time must be within these hours.");
            } catch (DateTimeParseException e) {
                System.out.println("  Invalid time. Use 24-hour format HH:mm (e.g. 15:30).");
            }
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String s = ask(prompt).toUpperCase();
            if (s.equals("Y") || s.equals("YES")) return true;
            if (s.equals("N") || s.equals("NO")) return false;
            System.out.println("  Please enter Y or N.");
        }
    }

    // ================================================================ 1. facility booking
    private void bookingMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Facility Booking Module ---");
            System.out.println("1. Record new booking");
            System.out.println("2. View all bookings");
            System.out.println("3. Cancel a booking");
            System.out.println("0. Back to main menu");
            int choice = readInt("Enter choice: ", 0, 3);
            if (choice == 0) return;
            if (choice == 1) recordBooking();
            else if (choice == 2) bookingManager.viewAll();
            else cancelBooking();
        }
    }

    private void recordBooking() {
        System.out.println("\n-- Record new booking --");
        String name = readText("Customer name: ");
        String contact = readContact("Contact number: ");
        LocalDate date = readBookingDate("Booking date (yyyy-MM-dd): ");
        LocalTime time = readTime("Start time (HH:mm, 24-hour): ");
        int pax = readInt("Number of participants (1-100): ", 1, 100);

        List<Facility> facilities = bookingManager.getFacilities();
        System.out.println("Available facilities:");
        for (int i = 0; i < facilities.size(); i++) {
            System.out.printf("  %d. [%s] %s%n", i + 1, facilities.get(i).getFacilityId(), facilities.get(i));
        }
        Facility facility = facilities.get(readInt("Select facility (1-" + facilities.size() + "): ", 1, facilities.size()) - 1);

        Booking booking = new Booking(bookingManager.nextBookingId(), new Customer(name, contact),
                facility, date, time, pax);
        if (bookingManager.addBooking(booking)) {
            saveAll();
            System.out.println("Booking saved: " + booking);
        } else {
            System.out.println("DOUBLE BOOKING PREVENTED: " + facility + " is already booked on " + date
                    + " (" + date.getDayOfWeek() + ") at " + time + ". Please choose another time or court.");
        }
    }

    private void cancelBooking() {
        System.out.println("\n-- Cancel booking --");
        bookingManager.viewActive();
        String id = ask("Enter booking ID to cancel (Enter to go back): ");
        if (id.isEmpty()) return;
        Booking booking = bookingManager.findById(id);
        if (booking == null || !booking.isActive()) {
            System.out.println("No active booking found with ID " + id + ".");
            return;
        }
        for (RentalTransaction t : transactions) {
            if (t.getBooking() != null && t.getBooking().getBookingId().equals(booking.getBookingId())) {
                System.out.println("Cannot cancel: booking " + booking.getBookingId()
                        + " already has rental transaction " + t.getTransactionId() + ".");
                return;
            }
        }
        if (readYesNo("Cancel " + booking + " ? (Y/N): ")) {
            bookingManager.cancelBooking(booking.getBookingId());
            saveAll();
            System.out.println("Booking " + booking.getBookingId() + " cancelled. The slot is available again.");
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    // ================================================================ 3. rental transactions
    private void transactionMenu() {
        while (true) {
            System.out.println();
            System.out.println("--- Rental Transaction Module ---");
            System.out.println("1. Create new transaction");
            System.out.println("2. Add items to an existing (open) transaction");
            System.out.println("3. Change quantity / remove item");
            System.out.println("4. View transactions");
            System.out.println("0. Back to main menu");
            int choice = readInt("Enter choice: ", 0, 4);
            if (choice == 0) return;
            if (choice == 1) newTransaction();
            else if (choice == 2) addToExisting();
            else if (choice == 3) changeQuantity();
            else viewTransactions();
        }
    }

    private String nextTransactionId() {
        int max = 0;
        for (RentalTransaction t : transactions) {
            try {
                max = Math.max(max, Integer.parseInt(t.getTransactionId().substring(2)));
            } catch (NumberFormatException e) {
                // ignore malformed ids
            }
        }
        return String.format("TX%03d", max + 1);
    }

    private void newTransaction() {
        System.out.println("\n-- Create new rental transaction --");
        Booking booking = null;
        Customer customer;
        if (readYesNo("Is this transaction for an existing booking? (Y = booking, N = walk-in): ")) {
            bookingManager.viewActive();
            String id = ask("Enter booking ID (Enter to go back): ");
            if (id.isEmpty()) return;
            booking = bookingManager.findById(id);
            if (booking == null || !booking.isActive()) {
                System.out.println("No active booking found with ID " + id + ".");
                return;
            }
            customer = booking.getCustomer();
            System.out.println("Customer: " + customer);
        } else {
            customer = new Customer(readText("Customer name: "), readContact("Contact number: "));
        }
        RentalTransaction tx = new RentalTransaction(nextTransactionId(), customer, booking);
        transactions.add(tx);
        System.out.println("Transaction " + tx.getTransactionId() + " created.");
        addItemsLoop(tx);
        saveAll();
    }

    private void addItemsLoop(RentalTransaction tx) {
        catalogue.displayAll();
        while (true) {
            String code = ask("Enter item code to add (Enter when finished): ");
            if (code.isEmpty()) break;
            RentalItem item = catalogue.findByCode(code);
            if (item == null) {
                System.out.println("  Item code not found in the catalogue.");
                continue;
            }
            int qty = readInt("  Quantity for " + item.getItemName() + " (1-99): ", 1, 99);
            tx.addItem(item, qty);
            System.out.println("  Added. Running subtotal: RM " + Bill.money(tx.getSubtotal()));
        }
        if (tx.isEmpty()) {
            System.out.println("Note: " + tx.getTransactionId() + " has no items yet.");
        } else {
            System.out.println();
            tx.displayLines();
        }
    }

    private RentalTransaction chooseTransaction(boolean needItems) {
        List<RentalTransaction> open = new ArrayList<>();
        for (RentalTransaction t : transactions) {
            if (t.isOpen() && (!needItems || !t.isEmpty())) open.add(t);
        }
        if (open.isEmpty()) {
            System.out.println("There are no open transactions" + (needItems ? " with items" : "")
                    + ". Create one from menu option 3 first.");
            return null;
        }
        System.out.println();
        System.out.printf("%-7s %-22s %-10s %6s %12s%n", "ID", "Customer", "Booking", "Items", "Subtotal RM");
        System.out.println("-".repeat(62));
        for (RentalTransaction t : open) {
            System.out.printf(Locale.US, "%-7s %-22s %-10s %6d %12.2f%n", t.getTransactionId(),
                    t.getCustomer().getName(), t.getBooking() == null ? "Walk-in" : t.getBooking().getBookingId(),
                    t.getLines().size(), t.getSubtotal());
        }
        System.out.println("-".repeat(62));
        String id = ask("Enter transaction ID (Enter to go back): ");
        if (id.isEmpty()) return null;
        for (RentalTransaction t : open) {
            if (t.getTransactionId().equalsIgnoreCase(id)) return t;
        }
        System.out.println("Transaction " + id + " is not in the open list.");
        return null;
    }

    private void addToExisting() {
        System.out.println("\n-- Add items to an existing transaction --");
        RentalTransaction tx = chooseTransaction(false);
        if (tx == null) return;
        addItemsLoop(tx);
        saveAll();
    }

    private void changeQuantity() {
        System.out.println("\n-- Change quantity / remove item --");
        RentalTransaction tx = chooseTransaction(true);
        if (tx == null) return;
        System.out.println();
        tx.displayLines();
        String code = ask("Item code to change: ");
        TransactionLine line = tx.findLine(code);
        if (line == null) {
            System.out.println("That item is not in " + tx.getTransactionId() + ".");
            return;
        }
        int qty = readInt("New quantity for " + line.getItem().getItemName() + " (0 = remove, max 99): ", 0, 99);
        tx.updateQuantity(code, qty);
        saveAll();
        System.out.println(qty == 0 ? "Item removed." : "Quantity updated.");
        if (!tx.isEmpty()) {
            System.out.println();
            tx.displayLines();
        }
    }

    private void viewTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions recorded yet.");
            return;
        }
        System.out.println();
        System.out.printf("%-7s %-22s %-10s %-10s %6s %12s%n", "ID", "Customer", "Booking", "Status", "Items", "Subtotal RM");
        System.out.println("-".repeat(72));
        for (RentalTransaction t : transactions) {
            System.out.printf(Locale.US, "%-7s %-22s %-10s %-10s %6d %12.2f%n", t.getTransactionId(),
                    t.getCustomer().getName(), t.getBooking() == null ? "Walk-in" : t.getBooking().getBookingId(),
                    t.getStatus(), t.getLines().size(), t.getSubtotal());
        }
        System.out.println("-".repeat(72));
        String id = ask("Enter a transaction ID to see its items (Enter to go back): ");
        if (id.isEmpty()) return;
        for (RentalTransaction t : transactions) {
            if (t.getTransactionId().equalsIgnoreCase(id)) {
                System.out.println();
                if (t.isEmpty()) System.out.println("No items in this transaction.");
                else t.displayLines();
                return;
            }
        }
        System.out.println("Transaction not found.");
    }

    // ================================================================ 4. promotion  /  5. payment
    private Bill buildBill(RentalTransaction tx) {
        List<PromotionResult> results = promotionEngine.evaluateAll(tx);
        PromotionResult best = promotionEngine.selectBest(results);
        return new Bill(tx, results, best);
    }

    private void applyPromotion() {
        System.out.println("\n-- Apply promotion --");
        RentalTransaction tx = chooseTransaction(true);
        if (tx == null) return;
        Bill bill = buildBill(tx);
        bill.display();
        if (readYesNo("Proceed to payment now? (Y/N): ")) payBill(bill);
    }

    private void makePayment() {
        System.out.println("\n-- Make payment --");
        RentalTransaction tx = chooseTransaction(true);
        if (tx == null) return;
        Bill bill = buildBill(tx);
        bill.display();
        payBill(bill);
    }

    private void payBill(Bill bill) {
        RentalTransaction tx = bill.getTransaction();
        String paymentId = "PAY" + tx.getTransactionId().substring(2);
        System.out.println();
        System.out.println("Payment method:");
        System.out.println("1. Cash");
        System.out.println("2. Credit / Debit card");
        System.out.println("3. E-wallet");
        System.out.println("0. Cancel (pay later)");
        int choice = readInt("Enter choice: ", 0, 3);
        if (choice == 0) {
            System.out.println("Payment postponed. Transaction stays open.");
            return;
        }
        Payment payment = collectPayment(choice, paymentId, bill.getFinalAmount());
        if (payment == null) {
            System.out.println("Payment cancelled. Transaction stays open.");
            return;
        }
        bill.setPayment(payment);
        tx.setStatus(RentalTransaction.STATUS_COMPLETED);
        bills.add(bill);
        saveAll();
        System.out.println("\nPAYMENT SUCCESSFUL.");
        bill.display();
    }

    /** Builds the right Payment subclass; processPayment() is then called polymorphically. */
    private Payment collectPayment(int choice, String paymentId, double amount) {
        System.out.println("Amount to pay: RM " + Bill.money(amount));
        while (true) {
            Payment payment;
            if (choice == 1) {
                double tendered = readMoney("Cash received (RM, 0 to cancel): ");
                if (tendered == 0) return null;
                payment = new CashPayment(paymentId, amount, tendered);
            } else if (choice == 2) {
                int type = readInt("Card type (1 = Credit, 2 = Debit, 0 = cancel): ", 0, 2);
                if (type == 0) return null;
                String last4 = ask("Last 4 digits of the card (Enter to cancel): ");
                if (last4.isEmpty()) return null;
                payment = new CardPayment(paymentId, amount, type == 1 ? "Credit" : "Debit", last4);
            } else {
                for (int i = 0; i < WALLETS.length; i++) System.out.println("  " + (i + 1) + ". " + WALLETS[i]);
                int w = readInt("Select e-wallet (0 = cancel): ", 0, WALLETS.length);
                if (w == 0) return null;
                String ref = ask("Transaction reference number (Enter to cancel): ");
                if (ref.isEmpty()) return null;
                payment = new EWalletPayment(paymentId, amount, WALLETS[w - 1], ref);
            }
            if (payment.processPayment()) return payment;     // polymorphic call
            System.out.println("Payment failed: details are invalid or the amount is not enough. Please try again.");
        }
    }

    private double readMoney(String prompt) {
        while (true) {
            String s = ask(prompt);
            try {
                double v = Double.parseDouble(s);
                if (v >= 0 && v < 1_000_000) return v;
            } catch (NumberFormatException e) {
                // fall through
            }
            System.out.println("  Invalid amount.");
        }
    }

    // ================================================================ 6. daily report
    private void dailyReport() {
        System.out.println("\n-- Daily report --");
        LocalDate date = null;
        while (date == null) {
            String s = ask("Report date (yyyy-MM-dd, Enter = today): ");
            if (s.isEmpty()) {
                date = LocalDate.now();
            } else {
                try {
                    date = LocalDate.parse(s);
                } catch (DateTimeParseException e) {
                    System.out.println("  Invalid date. Use the format yyyy-MM-dd.");
                }
            }
        }
        new DailyReport(date, bookingManager.getBookings(), bills).display();
    }
}
