package patrones_farmacia.strategy.model;

public class NoDiscount implements DiscountStrategy {
    @Override
    public double calculate(double total) { return total; }
    @Override
    public String getDescription() { return "Sin descuento"; }
}