package patrones_farmacia.decorator.model;

public class BaseProduct implements Product {

    private String name;
    private double price;

    public BaseProduct(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() { return price; }

    @Override
    public String getDescription() { return name; }
}