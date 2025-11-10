package patrones_farmacia.Singleton.view;

import patrones_farmacia.Singleton.controller.InventoryController;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.*;
import java.util.Scanner;

public class InventoryConsole {

    private InventoryController controller = new InventoryController();
    private FCreator creator = new FCreator();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        boolean active = true;
        while (active) {
            System.out.println("\n=== SINGLETON - INVENTARIO GLOBAL ===");
            System.out.println("1. Agregar medicamento genérico");
            System.out.println("2. Agregar medicamento de marca");
            System.out.println("3. Ver inventario");
            System.out.println("4. Eliminar medicamento");
            System.out.println("5. Salir");
            System.out.print("Opción: ");
            int op = scanner.nextInt(); scanner.nextLine();

            switch (op) {
                case 1 -> addGeneric();
                case 2 -> addBrand();
                case 3 -> controller.showInventory();
                case 4 -> remove();
                case 5 -> active = false;
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private void addGeneric() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Laboratorio: ");
        String lab = scanner.nextLine();
        System.out.print("Precio: ");
        double price = scanner.nextDouble(); scanner.nextLine();

        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, name, lab, price);
        controller.registerMedicine(m);
    }

    private void addBrand() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Marca: ");
        String brand = scanner.nextLine();
        System.out.print("Precio: ");
        double price = scanner.nextDouble(); scanner.nextLine();

        Medicine m = creator.createMedicine(FCreator.Type.BRAND, name, brand, price);
        controller.registerMedicine(m);
    }

    private void remove() {
        System.out.print("Nombre del medicamento a eliminar: ");
        String name = scanner.nextLine();
        controller.removeMedicine(name);
    }

    public static void main(String[] args) {
        new InventoryConsole().run();
    }
}