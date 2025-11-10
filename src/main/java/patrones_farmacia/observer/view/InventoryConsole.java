package patrones_farmacia.observer.view;

import patrones_farmacia.observer.controller.InventoryController;
import java.util.List;
import java.util.Scanner;

/**
 * Vista de consola para probar el patrón Observer.
 */
public class InventoryConsole {

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            InventoryController controller = new InventoryController();

            // Registramos observadores
            controller.registerEmailAlert("alertas@farmacia.com");
            controller.registerSMSAlert("+573001112233");

            boolean active = true;
            while (active) {
                System.out.println("\n=== OBSERVER - GESTIÓN DE INVENTARIO ===");
                System.out.println("1. Agregar producto");
                System.out.println("2. Reducir stock");
                System.out.println("3. Reponer stock");
                System.out.println("4. Ver inventario");
                System.out.println("5. Ver productos con bajo stock");
                System.out.println("6. Cambiar umbral de alerta");
                System.out.println("7. Salir");
                System.out.print("Opción: ");
                int op = sc.nextInt(); sc.nextLine();

                switch (op) {
                    case 1 -> {
                        System.out.print("Nombre del producto: ");
                        String name = sc.nextLine();
                        System.out.print("Stock inicial: ");
                        int stock = sc.nextInt(); sc.nextLine();
                        controller.addProduct(name, stock);
                    }
                    case 2 -> {
                        System.out.print("Producto a reducir: ");
                        String name = sc.nextLine();
                        System.out.print("Cantidad: ");
                        int q = sc.nextInt(); sc.nextLine();
                        controller.reduceStock(name, q);
                    }
                    case 3 -> {
                        System.out.print("Producto a reponer: ");
                        String name = sc.nextLine();
                        System.out.print("Cantidad: ");
                        int q = sc.nextInt(); sc.nextLine();
                        controller.restock(name, q);
                    }
                    case 4 -> controller.showInventory();
                    case 5 -> {
                        List<String> low = controller.getLowStockProducts();
                        if (low.isEmpty()) System.out.println("Todos los productos tienen stock suficiente.");
                        else {
                            System.out.println("\n⚠ Productos con bajo stock:");
                            low.forEach(p -> System.out.println("- " + p));
                        }
                    }
                    case 6 -> {
                        System.out.print("Nuevo umbral mínimo: ");
                        int t = sc.nextInt(); sc.nextLine();
                        controller.setThreshold(t);
                        System.out.println("Umbral de alerta actualizado a " + t);
                    }
                    case 7 -> active = false;
                    default -> System.out.println("Opción inválida.");
                }
            }
        }
        System.out.println("Saliendo del módulo Observer...");
    }

    public static void main(String[] args) {
        new InventoryConsole().run();
    }
}