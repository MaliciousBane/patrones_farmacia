package patrones_farmacia.observer.model;

public class EmailAlert implements FarmaObserver {

    private String email;

    public EmailAlert(String email) {
        this.email = email;
    }

    @Override
    public void update(String productName, int currentStock) {
        System.out.println("[EMAIL] A " + email + ": Stock bajo de " + productName + " (" + currentStock + " unidades)");
    }

    @Override
    public String toString() {
        return "EmailAlert{" + email + "}";
    }
}