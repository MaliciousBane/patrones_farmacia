package patrones_farmacia.factoryMethod.model;

public class BrandMedicine implements Medicine {

    private String name;
    private String brand;
    private double price;

    public BrandMedicine(String name, String brand, double price) {
        this.name = name;
        this.brand = brand;
        this.price = price;
    }

    @Override
    public String getName() { return name; }

    @Override
    public double getPrice() { return price; }

    @Override
    public boolean isControlled() { return false; }

    @Override
    public String getLaboratory() { return brand; }

    @Override
    public String getType() { return "De Marca"; }

    @Override
    public String toString() {
        return "Medicamento de Marca: " + name + 
               " | Marca: " + brand + 
               " | Precio: $" + price;
    }
}
