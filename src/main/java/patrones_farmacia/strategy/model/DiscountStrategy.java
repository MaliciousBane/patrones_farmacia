package patrones_farmacia.strategy.model;

public interface DiscountStrategy {
    double calculate(double total);
    String getDescription();
}