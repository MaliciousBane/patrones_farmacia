package patrones_farmacia.strategy.model;

public class PercentageDiscount implements DiscountStrategy {

    private double percentage;

    public PercentageDiscount(double percentage) {
        this.percentage = percentage;
    }

    @Override
    public double calculate(double total) {
        return total * (1 - percentage / 100.0);
    }

    @Override
    public String getDescription() {
        return "Descuento " + percentage + "%";
    }
}