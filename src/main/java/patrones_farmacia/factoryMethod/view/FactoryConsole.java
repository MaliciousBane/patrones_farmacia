package patrones_farmacia.factoryMethod.view;

import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.*;
import java.util.Scanner;

public class FactoryConsole {

    private FCreator creator = new FCreator();
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("=== FACTORY METHOD - CREACIÓN DE MEDICAMENTOS ===");
        boolean active = true;

        while (active) {
            System.out.println("\n1. Crear Genérico");
            System.out.println("2. Crear de Marca");
            System.out.println("3. Crear Controlado");
            System.out.println("4. Salir");
            System.out.print("Seleccione una opción: ");
            int op = scanner.nextInt(); scanner.nextLine();

            switch (op) {
                case 1 -> createGeneric();
                case 2 -> createBrand();
                case 3 -> createControlled();
                case 4 -> active = false;
                default -> System.out.println("Opción inválida");
            }
        }
    }

    private void createGeneric() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Laboratorio: ");
        String lab = scanner.nextLine();
        System.out.print("Precio: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        Medicine m = creator.createMedicine(FCreator.Type.GENERIC, name, lab, price);
        System.out.println("Creado: " + m);
    }

    private void createBrand() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Marca: ");
        String brand = scanner.nextLine();
        System.out.print("Precio: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        Medicine m = creator.createMedicine(FCreator.Type.BRAND, name, brand, price);
        System.out.println("Creado: " + m);
    }

    private void createControlled() {
        System.out.print("Nombre: ");
        String name = scanner.nextLine();
        System.out.print("Código INVIMA: ");
        String code = scanner.nextLine();
        System.out.print("Precio: ");
        double price = scanner.nextDouble();
        scanner.nextLine();

        Medicine m = creator.createMedicine(FCreator.Type.CONTROLLED, name, code, price);
        System.out.println("Creado: " + m);
    }

    public static void main(String[] args) {
        new FactoryConsole().run();
    }
}