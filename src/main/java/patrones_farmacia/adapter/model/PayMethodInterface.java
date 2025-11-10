package patrones_farmacia.adapter.model;

public interface PayMethodInterface {
    boolean pay(double amount);
    String getName();
}