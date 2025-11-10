package patrones_farmacia.factoryMethod.model;

public interface Medicine {
    String getName();
    double getPrice();
    boolean isControlled();
    String getLaboratory();
    String getType();
}