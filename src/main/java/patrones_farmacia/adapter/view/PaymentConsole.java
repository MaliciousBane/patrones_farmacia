package patrones_farmacia.adapter.view;

import patrones_farmacia.adapter.model.*;
import patrones_farmacia.adapter.controller.PaymentController;
import java.util.Scanner;

public class PaymentConsole {

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            CashMethod cash = new CashMethod(50000);
            CreditCardMethod credit = new CreditCardMethod("1234-5678", "Andrés Sánchez", "999", 300000);
            EWalletMethod wallet = new EWalletMethod("ACC-1001", 100000);
            AdapterPayMethod adapter = new AdapterPayMethod(cash, credit, wallet);
            PaymentController controller = new PaymentController(adapter);

            boolean active = true;
            while (active) {
                System.out.println("\n=== ADAPTER - MÉTODOS DE PAGO ===");
                System.out.println("1. Pagar en efectivo");
                System.out.println("2. Pagar con tarjeta");
                System.out.println("3. Pagar con billetera digital");
                System.out.println("4. Salir");
                System.out.print("Opción: ");
                int op = sc.nextInt(); sc.nextLine();
                System.out.print("Monto a pagar: ");
                double amount = sc.nextDouble(); sc.nextLine();

                switch (op) {
                    case 1 -> controller.processPayment("CASH", amount);
                    case 2 -> controller.processPayment("CREDIT", amount);
                    case 3 -> controller.processPayment("EWALLET", amount);
                    case 4 -> active = false;
                    default -> System.out.println("Opción inválida");
                }
            }
        }
    }

    public static void main(String[] args) {
        new PaymentConsole().run();
    }
}
