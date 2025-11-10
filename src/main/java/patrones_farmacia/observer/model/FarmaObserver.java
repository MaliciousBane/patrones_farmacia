package patrones_farmacia.observer.model;

public interface FarmaObserver {
    void update(String productName, int currentStock);
}