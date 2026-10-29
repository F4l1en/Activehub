/**
 * Promotion B - Team Booking Reward: for 8 or more participants deduct RM5 per participant,
 * capped at RM60 per transaction and never more than the facility charge (cannot go below RM0).
 */
public class TeamBookingReward implements Promotion {
    private static final int MIN_PARTICIPANTS = 8;
    private static final double PER_HEAD = 5.0;
    private static final double CAP = 60.0;

    @Override
    public String getCode() { return "B"; }

    @Override
    public String getName() { return "Team Booking Reward"; }

    @Override
    public boolean isEligible(RentalTransaction tx) {
        Booking booking = tx.getBooking();
        return booking != null && booking.getParticipants() >= MIN_PARTICIPANTS
                && tx.getFacilityCharge() > 0;
    }

    @Override
    public double calculateSaving(RentalTransaction tx) {
        double reward = Math.min(tx.getBooking().getParticipants() * PER_HEAD, CAP);
        return Promotion.round2(Math.min(reward, tx.getFacilityCharge()));
    }
}
