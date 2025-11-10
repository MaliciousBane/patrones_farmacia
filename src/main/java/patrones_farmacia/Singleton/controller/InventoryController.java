package patrones_farmacia.Singleton.controller;

import patrones_farmacia.Singleton.model.GlobalInvent;
import patrones_farmacia.factoryMethod.model.Medicine;

public class InventoryController {

    private GlobalInvent inventory = GlobalInvent.getInstance();

    public void registerMedicine(Medicine m) {
        inventory.addMedicine(m);
        System.out.println("Medicamento agregado al inventario: " + m.getName());
    }

    public void removeMedicine(String name) {
        if (inventory.removeMedicine(name)) {
            System.out.println("Medicamento eliminado: " + name);
        } else {
            System.out.println("No se encontró el medicamento: " + name);
        }
    }

    public void showInventory() {
        inventory.showInventory();
    }
}