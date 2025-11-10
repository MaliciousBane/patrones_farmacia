package patrones_farmacia.facade.controller;

import patrones_farmacia.adapter.model.*;

public class PaySystem {

    private AdapterPayMethod payAdapter;

    public PaySystem(AdapterPayMethod payAdapter) {
        this.payAdapter = payAdapter;
    }

    public boolean processPayment(double amount, String mode) {
        payAdapter.setMode(mode);
        return payAdapter.pay(amount);
    }
}