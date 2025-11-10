package patrones_farmacia.decorator.model;

public abstract class ProductDecorator implements Product {

    protected Product product;

    public ProductDecorator(Product product) {
        this.product = product;
    }

    @Override
    public double getPrice() { return product.getPrice(); }

    @Override
    public String getDescription() { return product.getDescription(); }
}