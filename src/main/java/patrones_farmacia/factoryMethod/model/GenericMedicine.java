package patrones_farmacia.factoryMethod.model;

public class GenericMedicine implements Medicine {

    private String name;
    private String laboratory;
    private double price;

    public GenericMedicine(String name, String laboratory, double price) {
        this.name = name;
        this.laboratory = laboratory;
        this.price = price;
    }

    @Override
    public String getName() { return name; }

    @Override
    public double getPrice() { return price; }

    @Override
    public boolean isControlled() { return false; }

    @Override
    public String getLaboratory() { return laboratory; }

    @Override
    public String getType() { return "Genérico"; }

    @Override
    public String toString() {
        return "Medicamento Genérico: " + name + 
               " | Lab: " + laboratory + 
               " | Precio: $" + price;
    }
}