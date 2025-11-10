package patrones_farmacia.chainOfResponsability.model;

import patrones_farmacia.facade.model.Sale;

public interface Handler {
    void setNext(Handler next);
    boolean handle(Sale sale);
}