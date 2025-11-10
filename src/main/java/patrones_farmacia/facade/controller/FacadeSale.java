package patrones_farmacia.facade.controller;

import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.List;

/**
 * Fachada: orquesta el proceso completo de venta.
 */
public class FacadeSale {

    private InventSystem invent;
    private PaySystem pay;
    private ReceiptSystem receipt;

    public FacadeSale(InventSystem invent, PaySystem pay, ReceiptSystem receipt) {
        this.invent = invent;
        this.pay = pay;
        this.receipt = receipt;
    }

    public boolean doSale(Sale sale, String paymentMode) {
        System.out.println("\nProcesando venta #" + sale.getId());

        List<Medicine> items = sale.getItems();
        for (Medicine med : items) {
            if (!invent.verifyStock(med)) {
                System.out.println("El producto " + med.getName() + " no está disponible.");
                return false;
            }
        }

        if (pay.processPayment(sale.getTotal(), paymentMode)) {
            for (Medicine med : items) {
                invent.removeFromStock(med);
            }
            receipt.generateReceipt(sale);
            System.out.println("Venta completada con éxito.");
            return true;
        } else {
            System.out.println("Error en el pago, venta cancelada.");
            return false;
        }
    }
}