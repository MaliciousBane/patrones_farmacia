package patrones_farmacia.strategy.model;

public class VIPClientDiscount implements DiscountStrategy {

    private double threshold;
    private double vipRate;

    public VIPClientDiscount(double threshold, double vipRate) {
        this.threshold = threshold;
        this.vipRate = vipRate;
    }

    @Override
    public double calculate(double total) {
        if (total >= threshold) {
            return total * (1 - vipRate / 100.0);
        }
        return total;
    }

    @Override
    public String getDescription() {
        return "Descuento VIP sobre compras superiores a $" + threshold;
    }
}
