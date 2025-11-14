package patrones_farmacia.adapter.model;

public class CreditCardMethod {

    private String cardNumber;
    private String ownerName;
    private String cvv;
    private double limit;

    public CreditCardMethod(String cardNumber, String ownerName, String cvv, double limit) {
        this.cardNumber = cardNumber;
        this.ownerName = ownerName;
        this.cvv = cvv;
        this.limit = limit;
    }

    public boolean makePayment(double amount) {
        if (amount <= limit) {
            limit -= amount;
            System.out.println("Pago con tarjeta realizado correctamente.");
            return true;
        }
        System.out.println("Límite de crédito insuficiente.");
        return false;
    }

    public String getOwnerName() { return ownerName; }

    public double getLimit() { return limit; }
}