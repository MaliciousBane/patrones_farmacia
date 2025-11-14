package patrones_farmacia.factoryMethod.controller;

import patrones_farmacia.factoryMethod.model.*;

public class FCreator {

    public enum Type { GENERIC, BRAND, CONTROLLED, BRANDED }

    public Medicine createMedicine(Type type, String name, String info, double price) {
        switch (type) {
            case GENERIC:
                return new GenericMedicine(name, info, price);
            case BRAND:
                return new BrandMedicine(name, info, price);
            case CONTROLLED:
                return new ControlledMedicine(name, info, price);
            default:
                throw new IllegalArgumentException("Tipo de medicamento no válido.");
        }
    }
}
