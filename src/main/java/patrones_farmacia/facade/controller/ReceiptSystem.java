package patrones_farmacia.facade.controller;

import patrones_farmacia.facade.model.Sale;

public class ReceiptSystem {

    public void generateReceipt(Sale sale) {
        System.out.println("\n=== RECIBO DE VENTA ===");
        System.out.println("Cliente: " + sale.getClient());
        System.out.println("Medicamentos comprados:");
        for (var m : sale.getItems()) {
            System.out.println("- " + m.getName() + " ($" + m.getPrice() + ")");
        }
        System.out.println("TOTAL: $" + sale.getTotal());
        System.out.println("=========================");
    }
}