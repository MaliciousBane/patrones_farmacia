package e2e.factoryMethod;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.Medicine;

class MedicineCreationE2ETest {

    @Test
    void testCreateDifferentTypesOfMedicine() {
        FCreator creator = new FCreator();
        Medicine generic = creator.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 3000);
        Medicine brand = creator.createMedicine(FCreator.Type.BRAND, "Dolex Forte", "GSK", 6000);

        assertNotNull(generic);
        assertNotNull(brand);
        assertNotEquals(generic.getType(), brand.getType());
    }
}