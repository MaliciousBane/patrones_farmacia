package patrones_farmacia.decorator.view;

import patrones_farmacia.decorator.model.*;
import java.util.Scanner;

public class DecoratorConsole {

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("\n=== DECORATOR - APLICAR DESCUENTOS E IMPUESTOS ===");
            System.out.print("Nombre del producto: ");
            String name = sc.nextLine();
            System.out.print("Precio base: ");
            double price = sc.nextDouble(); sc.nextLine();

            Product product = new BaseProduct(name, price);
            System.out.print("¿Aplicar descuento (%): ");
            double desc = sc.nextDouble(); sc.nextLine();
            if (desc > 0) product = new DiscountDecorator(product, desc);

            System.out.print("¿Aplicar IVA (%): ");
            double iva = sc.nextDouble(); sc.nextLine();
            if (iva > 0) product = new TaxDecorator(product, iva);

            System.out.println("\nProducto final: " + product.getDescription());
            System.out.println("Precio final: $" + product.getPrice());
        }
    }

    public static void main(String[] args) {
        new DecoratorConsole().run();
    }
}