package patrones_farmacia.observer.model;

import java.util.ArrayList;
import java.util.List;


public class InventorySubject {

    private List<FarmaObserver> observers;
    private List<String> productNames;
    private List<Integer> stockLevels;
    private int minThreshold;

    public InventorySubject(int i) {
        this.observers = new ArrayList<>();
        this.productNames = new ArrayList<>();
        this.stockLevels = new ArrayList<>();
        this.minThreshold = 5;
    }

    public void addObserver(FarmaObserver observer) {
        if (!observers.contains(observer)) observers.add(observer);
    }

    public void removeObserver(FarmaObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(String name, int stock) {
        for (FarmaObserver obs : observers) {
            obs.update(name, stock);
        }
    }

    public void addProduct(String name, int stock) {
        productNames.add(name);
        stockLevels.add(stock);
    }

    public void reduceStock(String name, int quantity) {
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i).equalsIgnoreCase(name)) {
                int newStock = stockLevels.get(i) - quantity;
                if (newStock < 0) newStock = 0;
                stockLevels.set(i, newStock);
                System.out.println("Stock actualizado de " + name + ": " + newStock);
                if (newStock <= minThreshold) {
                    notifyObservers(name, newStock);
                }
                return;
            }
        }
        System.out.println("Producto no encontrado: " + name);
    }

    public void increaseStock(String name, int quantity) {
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i).equalsIgnoreCase(name)) {
                int newStock = stockLevels.get(i) + quantity;
                stockLevels.set(i, newStock);
                System.out.println("Reposición de " + name + " a " + newStock + " unidades.");
                return;
            }
        }
        System.out.println("Producto no encontrado: " + name);
    }

    public int getStockLevel(String name) {
        for (int i = 0; i < productNames.size(); i++) {
            if (productNames.get(i).equalsIgnoreCase(name)) {
                return stockLevels.get(i);
            }
        }
        return -1;
    }

    public List<String> getAllProductNames() {
        return new ArrayList<>(productNames);
    }

    public List<String> getLowStockProducts() {
        List<String> low = new ArrayList<>();
        for (int i = 0; i < productNames.size(); i++) {
            if (stockLevels.get(i) <= minThreshold) {
                low.add(productNames.get(i));
            }
        }
        return low;
    }

    public void setMinThreshold(int threshold) {
        this.minThreshold = threshold;
    }

    public int getMinThreshold() {
        return minThreshold;
    }
}