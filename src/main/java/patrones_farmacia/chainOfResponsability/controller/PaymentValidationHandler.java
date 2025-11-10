package patrones_farmacia.chainOfResponsability.controller;

import patrones_farmacia.chainOfResponsability.model.BaseHandler;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.adapter.model.PayMethodInterface;

public class PaymentValidationHandler extends BaseHandler {

    private PayMethodInterface payment;

    public PaymentValidationHandler(PayMethodInterface payment) {
        this.payment = payment;
    }

    @Override
    public boolean handle(Sale sale) {
        boolean success = payment.pay(sale.getTotal());
        if (!success) {
            System.out.println("Pago fallido durante validación.");
            return false;
        }
        return handleNext(sale);
    }
}