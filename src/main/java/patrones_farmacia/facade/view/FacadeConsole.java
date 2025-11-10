package patrones_farmacia.facade.view;

import patrones_farmacia.factoryMethod.model.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.facade.controller.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.*;
import java.util.Scanner;

public class FacadeConsole {

    public void run() {
        try (Scanner sc = new Scanner(System.in)) {
            System.out.println("=== FACADE - PROCESO DE VENTA COMPLETO ===");

            InventSystem invent = new InventSystem();
            CashMethod cash = new CashMethod(200000);
            CreditCardMethod card = new CreditCardMethod("9999-8888", "Farmacia", "777", 500000);
            EWalletMethod wallet = new EWalletMethod("WAL-123", 300000);
            AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
            PaySystem paySystem = new PaySystem(adapter);
            ReceiptSystem receiptSystem = new ReceiptSystem();

            FacadeSale facade = new FacadeSale(invent, paySystem, receiptSystem);

            FCreator creator = new FCreator();
            Medicine m1 = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);
            Medicine m2 = creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6200);
            invent.addToStock(m1);
            invent.addToStock(m2);

            System.out.print("Nombre del cliente: ");
            String client = sc.nextLine();
            Sale sale = new Sale("001", client);

            sale.addMedicine(m1);
            sale.addMedicine(m2);

            System.out.println("\nSeleccione método de pago (CASH / CREDIT / EWALLET): ");
            String mode = sc.nextLine();

            facade.doSale(sale, mode);
        }
    }

    public static void main(String[] args) {
        new FacadeConsole().run();
    }
}