/**
 * Customer - a person who makes a booking or a walk-in rental request.
 * OOP concept: ENCAPSULATION (private final fields, access only through getters).
 */
public class Customer {
    private final String name;
    private final String contactNumber;

    public Customer(String name, String contactNumber) {
        this.name = name;
        this.contactNumber = contactNumber;
    }

    public String getName() { return name; }

    public String getContactNumber() { return contactNumber; }

    @Override
    public String toString() { return name + " (" + contactNumber + ")"; }
}
