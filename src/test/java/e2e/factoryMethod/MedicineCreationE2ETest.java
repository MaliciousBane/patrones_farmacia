package e2e.factoryMethod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class MedicineCreationE2ETest {

    @Test
    void shouldCreateGenericAndBrandMedicinesWithDifferentTypes() {
        FCreator creator = new FCreator();
        
        Medicine generic = creator.createMedicine(FCreator.Type.GENERIC, 
                                                   "Paracetamol", "GenFarma", 3000);
        Medicine brand = creator.createMedicine(FCreator.Type.BRAND, 
                                                 "Dolex Forte", "GSK", 6000);

        assertNotNull(generic, "El medicamento genérico debe ser creado");
        assertNotNull(brand, "El medicamento de marca debe ser creado");
        
        assertNotEquals(generic.getType(), brand.getType(), 
                        "Los medicamentos genéricos y de marca deben tener tipos diferentes");
    }
}