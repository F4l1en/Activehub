/**
 * Facility - a bookable court / activity space (e.g. Badminton Court 1).
 * OOP concept: ENCAPSULATION. Booking holds an ASSOCIATION to one Facility.
 */
public class Facility {
    private final String facilityId;
    private final String facilityType;
    private final int courtNumber;

    public Facility(String facilityId, String facilityType, int courtNumber) {
        this.facilityId = facilityId;
        this.facilityType = facilityType;
        this.courtNumber = courtNumber;
    }

    public String getFacilityId() { return facilityId; }

    public String getFacilityType() { return facilityType; }

    public int getCourtNumber() { return courtNumber; }

    @Override
    public String toString() { return facilityType + " Court " + courtNumber; }
}
