package patrones_farmacia.facade.controller;

import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.ArrayList;
import java.util.List;


public class InventSystem {

    private List<Medicine> stock = new ArrayList<>();

    public void addToStock(Medicine med) {
        stock.add(med);
    }

    public boolean verifyStock(Medicine med) {
        for (Medicine m : stock) {
            if (m.getName().equalsIgnoreCase(med.getName())) {
                return true;
            }
        }
        return false;
    }

    public void removeFromStock(Medicine med) {
        for (int i = 0; i < stock.size(); i++) {
            if (stock.get(i).getName().equalsIgnoreCase(med.getName())) {
                stock.remove(i);
                break;
            }
        }
    }
}