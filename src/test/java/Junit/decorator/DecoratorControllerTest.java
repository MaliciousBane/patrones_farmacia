package Junit.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;

class DecoratorControllerTest {

    private DecoratorController controller;

    @BeforeEach
    void setUp() {
        controller = new DecoratorController();
    }

    @Test
    void testApplyDiscountAndTax() {
        Product p = controller.createBaseProduct("Ibuprofeno", 10000);
        p = controller.applyDiscountThenTax(p, 10, 19); 
        double finalPrice = controller.calculateFinalPrice(p);
        assertEquals(10710.0, finalPrice, 0.1);
    }

    @Test
    void testOnlyDiscount() {
        Product p = controller.createBaseProduct("Acetaminofén", 5000);
        p = controller.applyDiscount(p, 20);
        assertEquals(4000, p.getPrice(), 0.1);
    }

    @Test
    void testDescriptionOutput() {
        Product p = controller.createBaseProduct("Jarabe", 8000);
        p = controller.applyTax(p, 19);
        assertTrue(controller.describeProduct(p).contains("IVA"));
    }
}
