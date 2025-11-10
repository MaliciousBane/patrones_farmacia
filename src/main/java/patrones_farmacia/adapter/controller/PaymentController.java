package patrones_farmacia.adapter.controller;

import patrones_farmacia.adapter.model.*;

public class PaymentController {

    private AdapterPayMethod adapter;

    public PaymentController(AdapterPayMethod adapter) {
        this.adapter = adapter;
    }

    public boolean processPayment(String mode, double amount) {
        adapter.setMode(mode);
        System.out.println("\nProcesando pago por: " + adapter.getName());
        return adapter.pay(amount);
    }
}