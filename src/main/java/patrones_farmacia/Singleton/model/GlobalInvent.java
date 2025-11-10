package patrones_farmacia.Singleton.model;

import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class GlobalInvent {

    private static GlobalInvent instance;
    private List<Medicine> medicines;

    private GlobalInvent() {
        medicines = new ArrayList<>();
    }

    public static synchronized GlobalInvent getInstance() {
        if (instance == null) {
            instance = new GlobalInvent();
        }
        return instance;
    }

    public void addMedicine(Medicine m) {
        medicines.add(m);
    }

    public boolean removeMedicine(String name) {
        for (int i = 0; i < medicines.size(); i++) {
            if (medicines.get(i).getName().equalsIgnoreCase(name)) {
                medicines.remove(i);
                return true;
            }
        }
        return false;
    }

    public Medicine findMedicine(String name) {
        for (Medicine m : medicines) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }

    public List<Medicine> getAllMedicines() {
        return medicines;
    }

    public void showInventory() {
        System.out.println("\n=== INVENTARIO GLOBAL ===");
        if (medicines.isEmpty()) {
            System.out.println("Inventario vacío.");
        } else {
            for (Medicine m : medicines) {
                System.out.println("- " + m);
            }
        }
    }
}