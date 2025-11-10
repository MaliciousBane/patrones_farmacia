package patrones_farmacia.decorator.model;

public class TaxDecorator extends ProductDecorator {

    private double tax; 

    public TaxDecorator(Product product, double tax) {
        super(product);
        this.tax = tax;
    }

    @Override
    public double getPrice() {
        return product.getPrice() * (1 + tax / 100.0);
    }

    @Override
    public String getDescription() {
        return product.getDescription() + " (IVA " + tax + "%)";
    }
}