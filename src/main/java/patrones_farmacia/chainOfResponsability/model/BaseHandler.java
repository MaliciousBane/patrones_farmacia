package patrones_farmacia.chainOfResponsability.model;

import patrones_farmacia.facade.model.Sale;

public abstract class BaseHandler implements Handler {
    protected Handler next;

    @Override
    public void setNext(Handler next) { this.next = next; }

    protected boolean handleNext(Sale sale) {
        return (next == null) || next.handle(sale);
    }
}