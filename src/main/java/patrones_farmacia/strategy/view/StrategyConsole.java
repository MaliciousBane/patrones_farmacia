package patrones_farmacia.strategy.view;

import patrones_farmacia.strategy.model.*;
import patrones_farmacia.strategy.controller.*;
import java.util.Scanner;

public class StrategyConsole {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in)) {
            DiscountContext context = new DiscountContext();

            System.out.print("Ingrese total de la compra: ");
            double total = sc.nextDouble(); sc.nextLine();

            System.out.println("\n=== STRATEGY - DESCUENTOS ===");
            System.out.println("1. Sin descuento");
            System.out.println("2. Descuento porcentual");
            System.out.println("3. Descuento VIP");
            System.out.print("Opción: ");
            int op = sc.nextInt(); sc.nextLine();

            switch (op) {
                case 1 -> context.setStrategy(new NoDiscount());
                case 2 -> {
                    System.out.print("Porcentaje: ");
                    double p = sc.nextDouble();
                    context.setStrategy(new PercentageDiscount(p));
                }
                case 3 -> {
                    System.out.print("Monto mínimo VIP: ");
                    double min = sc.nextDouble();
                    System.out.print("Porcentaje VIP: ");
                    double rate = sc.nextDouble();
                    context.setStrategy(new VIPClientDiscount(min, rate));
                }
                default -> context.setStrategy(new NoDiscount());
            }

            double finalTotal = context.apply(total);
            System.out.println("\nEstrategia aplicada: " + context.describe());
            System.out.println("Total final: $" + finalTotal);
        }
    }
}
