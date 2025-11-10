package patrones_farmacia.facade.model;

import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class Sale {

    private String id;
    private String client;
    private List<Medicine> items;
    private double total;

    public Sale(String id, String client) {
        this.id = id;
        this.client = client;
        this.items = new ArrayList<>();
        this.total = 0;
    }

    public void addMedicine(Medicine med) {
        items.add(med);
        total += med.getPrice();
    }

    public double getTotal() { return total; }
    public String getClient() { return client; }
    public List<Medicine> getItems() { return items; }
    public String getId() { return id; }
}