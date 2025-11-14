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
        this.currentMode = (mode == null) ? null : mode.toUpperCase();
    }

    @Override
    public boolean pay(double amount) {
        if (amount < 0) return false;
        if (currentMode == null) return false;

        switch (currentMode) {
            case "CASH":
                if (cash == null) return false;
                return cash.cashPay(amount);
            case "CREDIT":
                if (credit == null) return false;
                return credit.makePayment(amount);
            case "EWALLET":
                if (wallet == null) return false;
                return wallet.transferCash(amount);
            default:
                System.out.println("Modo de pago no soportado.");
                return false;
        }
    }

    @Override
    public String getName() {
        if (currentMode == null) return "Desconocido";
        switch (currentMode) {
            case "CASH": return "Efectivo";
            case "CREDIT": return "Tarjeta de Crédito";
            case "EWALLET": return "E-Wallet";
            default: return "Desconocido";
        }
    }
}
