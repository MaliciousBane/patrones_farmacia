package patrones_farmacia.observer.controller;

import patrones_farmacia.observer.model.*;
import java.util.List;

public class InventoryController {

    private InventorySubject subject;

    public InventoryController() {
        this.subject = new InventorySubject(5);
    }

    public void registerEmailAlert(String email) {
        subject.addObserver(new EmailAlert(email));
    }

    public void registerSMSAlert(String phone) {
        subject.addObserver(new SMSAlert(phone));
    }

    public void removeObserver(FarmaObserver obs) {
        subject.removeObserver(obs);
    }

    public void addProduct(String name, int stock) {
        subject.addProduct(name, stock);
    }

    public void reduceStock(String name, int quantity) {
        subject.reduceStock(name, quantity);
    }

    public void restock(String name, int quantity) {
        subject.increaseStock(name, quantity);
    }

    public int getStock(String name) {
        return subject.getStockLevel(name);
    }

    public List<String> getLowStockProducts() {
        return subject.getLowStockProducts();
    }

    public List<String> getAllProducts() {
        return subject.getAllProductNames();
    }

    public void showInventory() {
        System.out.println("\n=== INVENTARIO ACTUAL ===");
        for (String name : subject.getAllProductNames()) {
            int stock = subject.getStockLevel(name);
            System.out.println("- " + name + " : " + stock + " unidades");
        }
    }

    public void setThreshold(int threshold) {
        subject.setMinThreshold(threshold);
    }

    public int getThreshold() {
        return subject.getMinThreshold();
    }
}