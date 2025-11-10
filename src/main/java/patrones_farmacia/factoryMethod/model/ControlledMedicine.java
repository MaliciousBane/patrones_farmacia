package patrones_farmacia.factoryMethod.model;

public class ControlledMedicine implements Medicine {

    private String name;
    private String codeHealth;
    private double price;

    public ControlledMedicine(String name, String codeHealth, double price) {
        this.name = name;
        this.codeHealth = codeHealth;
        this.price = price;
    }

    @Override
    public String getName() { return name; }

    @Override
    public double getPrice() { return price; }

    @Override
    public boolean isControlled() { return true; }

    @Override
    public String getLaboratory() { return codeHealth; }

    @Override
    public String getType() { return "Controlado"; }

    @Override
    public String toString() {
        return "Medicamento Controlado: " + name +
               " | Registro Invima: " + codeHealth +
               " | Precio: $" + price;
    }
}