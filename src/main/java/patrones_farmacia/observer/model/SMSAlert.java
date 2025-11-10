package patrones_farmacia.observer.model;

public class SMSAlert implements FarmaObserver {

    private String phone;

    public SMSAlert(String phone) {
        this.phone = phone;
    }

    @Override
    public void update(String productName, int currentStock) {
        System.out.println("[SMS] " + phone + ": quedan " + currentStock + " unidades de " + productName);
    }

    @Override
    public String toString() {
        return "SMSAlert{" + phone + "}";
    }
}