package Junit.factoryMethod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class FactoryMethodTest {

    @Test
    void testCreateGenericMedicine() {
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3500);

        assertNotNull(med);
        assertEquals("Paracetamol", med.getName());
        assertTrue(med.getType().contains("Genérico"));
    }

    @Test
    void testCreateBrandMedicine() {
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6000);

        assertNotNull(med);
        assertEquals("Dolex Forte", med.getName());
        assertTrue(med.getType().contains("Marca"));
    }
}