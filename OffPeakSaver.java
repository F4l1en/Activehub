import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Promotion A - Off-Peak Saver: 15% off FACILITY charges for bookings that start Monday-Friday
 * at or after 9:00 AM and before 4:00 PM (3:59 PM qualifies, 4:00 PM does not).
 * Eligibility uses DayOfWeek and LocalTime from java.time (no String comparison).
 */
public class OffPeakSaver implements Promotion {
    private static final double RATE = 0.15;
    private static final LocalTime WINDOW_START = LocalTime.of(9, 0);
    private static final LocalTime WINDOW_END = LocalTime.of(16, 0);

    @Override
    public String getCode() { return "A"; }

    @Override
    public String getName() { return "Off-Peak Saver"; }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        Booking booking = tx.getBooking();
        if (booking == null || tx.getFacilityCharge() <= 0) return false;
        DayOfWeek day = booking.getDayOfWeek();
        boolean weekday = day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
        LocalTime start = booking.getStartTime();
        boolean inWindow = !start.isBefore(WINDOW_START) && start.isBefore(WINDOW_END);
        return weekday && inWindow;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        return Promotion.round2(tx.getFacilityCharge() * RATE);
    }
}
