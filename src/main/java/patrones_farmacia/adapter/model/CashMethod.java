package patrones_farmacia.adapter.model;

public class CashMethod {

    private double cashAvailable;

    public CashMethod(double cashAvailable) {
        this.cashAvailable = cashAvailable;
    }

    public boolean cashPay(double amount) {
        if (cashAvailable >= amount) {
            cashAvailable -= amount;
            System.out.println("Pago en efectivo realizado. Cambio: $" + (cashAvailable));
            return true;
        } else {
            System.out.println("Fondos insuficientes en efectivo.");
            return false;
        }
    }

    public double getCashAvailable() {
        return cashAvailable;
    }
}