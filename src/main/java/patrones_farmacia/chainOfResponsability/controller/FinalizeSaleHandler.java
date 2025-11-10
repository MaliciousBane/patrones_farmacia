package patrones_farmacia.chainOfResponsability.controller;

import patrones_farmacia.chainOfResponsability.model.BaseHandler;
import patrones_farmacia.facade.model.Sale;

public class FinalizeSaleHandler extends BaseHandler {
    @Override
    public boolean handle(Sale sale) {
        System.out.println("Venta " + sale.getId() + " validada y completada correctamente.");
        return true;
    }
}