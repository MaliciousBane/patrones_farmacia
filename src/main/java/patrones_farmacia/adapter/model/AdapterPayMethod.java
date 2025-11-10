package patrones_farmacia.adapter.model;

public class AdapterPayMethod implements PayMethodInterface {

    private CashMethod cash;
    private CreditCardMethod credit;
    private EWalletMethod wallet;
    private String currentMode;

    public AdapterPayMethod(CashMethod cash, CreditCardMethod credit, EWalletMethod wallet) {
        this.cash = cash;
        this.credit = credit;
        this.wallet = wallet;
        this.currentMode = "CASH"; 
    }

    public void setMode(String mode) {
        this.currentMode = mode.toUpperCase();
    }

    @Override
    public boolean pay(double amount) {
        switch (currentMode) {
            case "CASH":
                return cash.cashPay(amount);
            case "CREDIT":
                return credit.makePayment(amount);
            case "EWALLET":
                return wallet.transferCash(amount);
            default:
                System.out.println("Modo de pago no soportado.");
                return false;
        }
    }

    @Override
    public String getName() {
        switch (currentMode) {
            case "CASH": return "Efectivo";
            case "CREDIT": return "Tarjeta de Crédito";
            case "EWALLET": return "E-Wallet";
            default: return "Desconocido";
        }
    }
}
