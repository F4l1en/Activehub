import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Booking - one reservation of a Facility on a date and start time.
 * Dates and times are stored with java.time classes (LocalDate / LocalTime),
 * never as Strings, so scheduling decisions use real date-time comparison.
 * OOP concepts: ENCAPSULATION, ASSOCIATION (Customer, Facility).
 */
public class Booking {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private final String bookingId;
    private final Customer customer;
    private final Facility facility;
    private final LocalDate bookingDate;
    private final LocalTime startTime;
    private final int participants;
    private String status;

    public Booking(String bookingId, Customer customer, Facility facility,
                   LocalDate bookingDate, LocalTime startTime, int participants) {
        this(bookingId, customer, facility, bookingDate, startTime, participants, STATUS_ACTIVE);
    }

    public Booking(String bookingId, Customer customer, Facility facility,
                   LocalDate bookingDate, LocalTime startTime, int participants, String status) {
        this.bookingId = bookingId;
        this.customer = customer;
        this.facility = facility;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.participants = participants;
        this.status = status;
    }

    public String getBookingId() { return bookingId; }

    public Customer getCustomer() { return customer; }

    public Facility getFacility() { return facility; }

    public LocalDate getBookingDate() { return bookingDate; }

    public LocalTime getStartTime() { return startTime; }

    public int getParticipants() { return participants; }

    public String getStatus() { return status; }

    public boolean isActive() { return STATUS_ACTIVE.equals(status); }

    /** Day of week comes from the LocalDate (used by Promotion A). */
    public DayOfWeek getDayOfWeek() { return bookingDate.getDayOfWeek(); }

    /** Two ACTIVE bookings clash when they use the same facility on the same date and start time. */
    public boolean conflictsWith(Booking other) {
        return this.isActive() && other.isActive()
                && this.facility.getFacilityId().equals(other.facility.getFacilityId())
                && this.bookingDate.equals(other.bookingDate)
                && this.startTime.equals(other.startTime);
    }

    public void cancel() { this.status = STATUS_CANCELLED; }

    @Override
    public String toString() {
        return bookingId + " | " + facility + " | " + bookingDate + " (" + getDayOfWeek() + ") "
                + startTime + " | " + participants + " pax";
    }
}
