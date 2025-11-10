package patrones_farmacia.command.model;

import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class SaleReceiver {

    private List<Sale> sales;

    public SaleReceiver() {
        this.sales = new ArrayList<>();
    }

    public void registerSale(Sale sale) {
        sales.add(sale);
        System.out.println("Venta registrada: " + sale.getId());
    }

    public boolean cancelSale(String saleId) {
        for (int i = 0; i < sales.size(); i++) {
            if (sales.get(i).getId().equalsIgnoreCase(saleId)) {
                sales.remove(i);
                System.out.println("Venta " + saleId + " cancelada.");
                return true;
            }
        }
        return false;
    }

    public boolean returnProduct(String saleId, Medicine med) {
        for (Sale s : sales) {
            if (s.getId().equalsIgnoreCase(saleId)) {
                List<Medicine> items = s.getItems();
                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getName().equalsIgnoreCase(med.getName())) {
                        items.remove(i);
                        System.out.println("Producto " + med.getName() + " devuelto.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Sale> getAllSales() {
        return sales;
    }
}