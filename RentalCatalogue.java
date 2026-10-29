import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * RentalCatalogue - the list of rental items staff can choose from.
 * OOP concept: AGGREGATION (the catalogue lists RentalItem objects that exist independently).
 */
public class RentalCatalogue {
    private final List<RentalItem> items = new ArrayList<>();

    public void addItem(RentalItem item) { items.add(item); }

    public List<RentalItem> getItems() { return Collections.unmodifiableList(items); }

    public RentalItem findByCode(String code) {
        for (RentalItem item : items) {
            if (item.getItemCode().equalsIgnoreCase(code)) return item;
        }
        return null;
    }

    public int size() { return items.size(); }

    public void displayAll() {
        System.out.println();
        System.out.println("=============================== RENTAL CATALOGUE ===============================");
        System.out.printf("%-6s %-34s %-10s %-18s %10s%n", "Code", "Item Name", "Type", "Category", "Price (RM)");
        System.out.println("-".repeat(82));
        for (RentalItem item : items) {
            System.out.printf(Locale.US, "%-6s %-34s %-10s %-18s %10.2f%n", item.getItemCode(), item.getItemName(),
                    item.getItemType(), item.getCategory(), item.getRentalPrice());
        }
        System.out.println("-".repeat(82));
    }
}
