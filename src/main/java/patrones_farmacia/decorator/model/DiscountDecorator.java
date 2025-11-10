package patrones_farmacia.decorator.model;

public class DiscountDecorator extends ProductDecorator {

    private double discount; 

    public DiscountDecorator(Product product, double discount) {
        super(product);
        this.discount = discount;
    }

    @Override
    public double getPrice() {
        double discounted = product.getPrice() * (1 - discount / 100.0);
        return discounted;
    }

    @Override
    public String getDescription() {
        return product.getDescription() + " (Descuento " + discount + "%)";
    }
}