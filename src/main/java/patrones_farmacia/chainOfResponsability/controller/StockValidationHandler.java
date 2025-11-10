package patrones_farmacia.chainOfResponsability.controller;

import patrones_farmacia.chainOfResponsability.model.BaseHandler;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.Medicine;
import java.util.List;


public class StockValidationHandler extends BaseHandler {

    private List<Medicine> inventory;

    public StockValidationHandler(List<Medicine> inventory) {
        this.inventory = inventory;
    }

    @Override
    public boolean handle(Sale sale) {
        for (Medicine med : sale.getItems()) {
            boolean found = false;
            for (Medicine inv : inventory) {
                if (inv.getName().equalsIgnoreCase(med.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("No hay stock del producto " + med.getName());
                return false;
            }
        }
        return handleNext(sale);
    }
}