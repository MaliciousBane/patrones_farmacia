package patrones_farmacia.command.view;

import patrones_farmacia.command.controller.*;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import java.util.Scanner;

public class CommandConsole {

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            SaleReceiver receiver = new SaleReceiver();
            CashierInvoker invoker = new CashierInvoker();
            FCreator creator = new FCreator();

            System.out.println("=== COMMAND - OPERACIONES DE VENTA ===");

            Medicine med1 = creator.createMedicine(FCreator.Type.BRAND, "Dolex", "GSK", 5000);
            Sale sale = new Sale("V001", "Cliente1");
            sale.addMedicine(med1);

            boolean active = true;
            while (active) {
                System.out.println("\n1. Registrar venta\n2. Cancelar venta\n3. Devolver producto\n4. Deshacer última\n5. Salir");
                System.out.print("Opción: ");
                int op = sc.nextInt(); sc.nextLine();

                switch (op) {
                    case 1 -> invoker.executeCommand(new RegisterSaleCommand(receiver, sale));
                    case 2 -> invoker.executeCommand(new CancelSaleCommand(receiver, "V001"));
                    case 3 -> invoker.executeCommand(new ReturnProductCommand(receiver, "V001", med1));
                    case 4 -> invoker.undoLast();
                    case 5 -> active = false;
                }
            }
        }
    }

    public static void main(String[] args) {
        new CommandConsole().run();
    }
}