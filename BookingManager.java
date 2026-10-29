import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * BookingManager - manages all Booking records and prevents double booking.
 * OOP concepts: AGGREGATION (it holds Booking and Facility objects),
 * ENCAPSULATION (lists are private; callers only get read-only views).
 */
public class BookingManager {
    public static final LocalTime OPENING_TIME = LocalTime.of(8, 0);
    public static final LocalTime CLOSING_TIME = LocalTime.of(22, 0);

    private final List<Facility> facilities = new ArrayList<>();
    private List<Booking> bookings = new ArrayList<>();

    public BookingManager() {
        for (int i = 1; i <= 4; i++) {
            facilities.add(new Facility("BD" + i, "Badminton", i));
        }
        facilities.add(new Facility("FS1", "Futsal", 1));
        facilities.add(new Facility("BK1", "Basketball", 1));
    }

    public List<Facility> getFacilities() { return Collections.unmodifiableList(facilities); }

    public Facility findFacility(String facilityId) {
        for (Facility f : facilities) {
            if (f.getFacilityId().equalsIgnoreCase(facilityId)) return f;
        }
        return null;
    }

    public List<Booking> getBookings() { return Collections.unmodifiableList(bookings); }

    /** Used when records are loaded from the text file at start-up. */
    public void setBookings(List<Booking> loaded) { this.bookings = new ArrayList<>(loaded); }

    public Booking findById(String id) {
        for (Booking b : bookings) {
            if (b.getBookingId().equalsIgnoreCase(id)) return b;
        }
        return null;
    }

    public boolean isWithinOpeningHours(LocalTime time) {
        return !time.isBefore(OPENING_TIME) && time.isBefore(CLOSING_TIME);
    }

    /** True when no ACTIVE booking uses this facility on the same date and start time. */
    public boolean isSlotAvailable(Facility facility, LocalDate date, LocalTime time) {
        for (Booking b : bookings) {
            if (b.isActive() && b.getFacility().getFacilityId().equals(facility.getFacilityId())
                    && b.getBookingDate().equals(date) && b.getStartTime().equals(time)) {
                return false;
            }
        }
        return true;
    }

    /** Adds the booking unless it would double-book the facility. */
    public boolean addBooking(Booking booking) {
        if (!isSlotAvailable(booking.getFacility(), booking.getBookingDate(), booking.getStartTime())) {
            return false;
        }
        bookings.add(booking);
        return true;
    }

    public boolean cancelBooking(String id) {
        Booking b = findById(id);
        if (b == null || !b.isActive()) return false;
        b.cancel();
        return true;
    }

    public String nextBookingId() {
        int max = 0;
        for (Booking b : bookings) {
            try {
                max = Math.max(max, Integer.parseInt(b.getBookingId().substring(2)));
            } catch (NumberFormatException e) {
                // ignore malformed ids
            }
        }
        return String.format("BK%03d", max + 1);
    }

    /** Prints all records (active and cancelled), sorted by date then time. */
    public void viewAll() { printTable(false); }

    /** Prints only ACTIVE bookings (used when linking a booking to a transaction). */
    public void viewActive() { printTable(true); }

    private void printTable(boolean activeOnly) {
        List<Booking> list = new ArrayList<>();
        for (Booking b : bookings) {
            if (!activeOnly || b.isActive()) list.add(b);
        }
        if (list.isEmpty()) {
            System.out.println("No booking records found.");
            return;
        }
        list.sort(Comparator.comparing(Booking::getBookingDate).thenComparing(Booking::getStartTime));
        System.out.println();
        System.out.printf("%-6s %-16s %-14s %-11s %-4s %-5s %-4s %-18s %-9s%n",
                "ID", "Customer", "Contact", "Date", "Day", "Time", "Pax", "Facility", "Status");
        System.out.println("-".repeat(98));
        for (Booking b : list) {
            System.out.printf("%-6s %-16s %-14s %-11s %-4s %-5s %-4d %-18s %-9s%n",
                    b.getBookingId(), cut(b.getCustomer().getName(), 16), b.getCustomer().getContactNumber(),
                    b.getBookingDate(), b.getDayOfWeek().toString().substring(0, 3), b.getStartTime(),
                    b.getParticipants(), b.getFacility(), b.getStatus());
        }
        System.out.println("-".repeat(98));
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}
