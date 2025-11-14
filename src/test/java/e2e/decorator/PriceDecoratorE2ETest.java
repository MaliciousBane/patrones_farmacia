package e2e.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.*;

@DisplayName("Decorator Pattern - Descuentos e Impuestos E2E")
class PriceDecoratorE2ETest {

    private DecoratorController controller;

    @BeforeEach
    void setUp() {
        controller = new DecoratorController();
    }

    @Test
    @DisplayName("Debe aplicar descuento e impuesto correctamente")
    void testApplyDiscountAndTaxCorrectly() {
        Product product = controller.createBaseProduct("Vitamina C", 10000);
        product = controller.applyDiscount(product, 10);
        product = controller.applyTax(product, 19);

        double finalPrice = controller.calculateFinalPrice(product);
        assertEquals(10710, finalPrice, 0.5, "El precio final debe incluir 10% descuento y 19% impuesto");
    }

    @Test
    @DisplayName("Debe crear producto base exitosamente")
    void testCreateBaseProductSuccessfully() {
        Product product = controller.createBaseProduct("Paracetamol", 5000);

        assertNotNull(product, "El producto no debe ser nulo");
        assertEquals(5000, product.getPrice(), "El precio debe ser 5000");
        assertEquals("Paracetamol", product.getDescription(), "La descripción debe ser Paracetamol");
    }

    @Test
    @DisplayName("Debe rechazar nombre vacío al crear producto")
    void testRejectEmptyNameOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.createBaseProduct("", 5000), "Debe lanzar excepción con nombre vacío");
    }

    @Test
    @DisplayName("Debe rechazar precio negativo al crear producto")
    void testRejectNegativePriceOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.createBaseProduct("Producto", -1000), "Debe lanzar excepción con precio negativo");
    }

    @Test
    @DisplayName("Debe rechazar nombre nulo al crear producto")
    void testRejectNullNameOnCreation() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.createBaseProduct(null, 5000), "Debe lanzar excepción con nombre nulo");
    }

    @Test
    @DisplayName("Debe aplicar descuento exitosamente")
    void testApplyDiscountSuccessfully() {
        Product product = controller.createBaseProduct("Ibupirac", 8000);
        Product discounted = controller.applyDiscount(product, 15);

        double expectedPrice = 8000 * 0.85;
        assertEquals(expectedPrice, discounted.getPrice(), 0.01, "El descuento debe ser 15%");
        assertTrue(discounted.getDescription().contains("Descuento 15%"), "Descripción debe incluir descuento");
    }

    @Test
    @DisplayName("Debe aplicar impuesto exitosamente")
    void testApplyTaxSuccessfully() {
        Product product = controller.createBaseProduct("Amoxicilina", 4500);
        Product taxed = controller.applyTax(product, 19);

        double expectedPrice = 4500 * 1.19;
        assertEquals(expectedPrice, taxed.getPrice(), 0.01, "El impuesto debe ser 19%");
        assertTrue(taxed.getDescription().contains("IVA 19%"), "Descripción debe incluir IVA");
    }

    @Test
    @DisplayName("Debe ignorar descuento de cero por ciento")
    void testIgnoreZeroDiscount() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product result = controller.applyDiscount(product, 0);

        assertEquals(1000, result.getPrice(), "El precio no debe cambiar con descuento 0");
    }

    @Test
    @DisplayName("Debe ignorar impuesto de cero por ciento")
    void testIgnoreZeroTax() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product result = controller.applyTax(product, 0);

        assertEquals(1000, result.getPrice(), "El precio no debe cambiar con impuesto 0");
    }

    @Test
    @DisplayName("Debe rechazar producto nulo al aplicar descuento")
    void testRejectNullProductForDiscount() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.applyDiscount(null, 10), "Debe lanzar excepción con producto nulo");
    }

    @Test
    @DisplayName("Debe rechazar producto nulo al aplicar impuesto")
    void testRejectNullProductForTax() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.applyTax(null, 19), "Debe lanzar excepción con producto nulo");
    }

    @Test
    @DisplayName("Debe aplicar descuento múltiple en secuencia")
    void testApplyMultipleDiscountsSequentially() {
        Product product = controller.createBaseProduct("Medicina", 10000);
        product = controller.applyDiscount(product, 10);
        product = controller.applyDiscount(product, 5);

        double expectedPrice = 10000 * 0.90 * 0.95;
        assertEquals(expectedPrice, product.getPrice(), 0.01, "Descuentos deben acumularse");
    }

    @Test
    @DisplayName("Debe aplicar descuento e impuesto en orden correcto")
    void testApplyDiscountAndTaxInCorrectOrder() {
        Product product = controller.createBaseProduct("Producto", 10000);
        Product discountedAndTaxed = controller.applyDiscountThenTax(product, 10, 19);

        double expectedPrice = 10000 * 0.90 * 1.19;
        assertEquals(expectedPrice, discountedAndTaxed.getPrice(), 0.01, "Cálculo debe ser correcto");
    }

    @Test
    @DisplayName("Debe calcular precio final sin modificadores")
    void testCalculateFinalPriceWithoutModifiers() {
        Product product = controller.createBaseProduct("Producto Simple", 5000);
        double finalPrice = controller.calculateFinalPrice(product);

        assertEquals(5000, finalPrice, "Precio final debe ser igual al precio base");
    }

    @Test
    @DisplayName("Debe rechazar nulo al calcular precio final")
    void testRejectNullProductForFinalPrice() {
        assertThrows(IllegalArgumentException.class, () -> 
            controller.calculateFinalPrice(null), "Debe lanzar excepción con producto nulo");
    }

    @Test
    @DisplayName("Debe describir producto correctamente")
    void testDescribeProductCorrectly() {
        Product product = controller.createBaseProduct("Aspirina", 2000);
        String description = controller.describeProduct(product);

        assertTrue(description.contains("Aspirina"), "Debe contener nombre del producto");
        assertTrue(description.contains("2000"), "Debe contener precio");
    }

    @Test
    @DisplayName("Debe retornar descripción predeterminada para nulo")
    void testDescribeNullProduct() {
        String description = controller.describeProduct(null);

        assertEquals("Producto no definido", description, "Debe retornar mensaje predeterminado");
    }

    @Test
    @DisplayName("Debe describir producto con descuento")
    void testDescribeProductWithDiscount() {
        Product product = controller.createBaseProduct("Vitamina B12", 8000);
        product = controller.applyDiscount(product, 20);
        String description = controller.describeProduct(product);

        assertTrue(description.contains("Descuento 20%"), "Descripción debe incluir descuento");
        assertTrue(description.contains("6400"), "Descripción debe incluir precio con descuento");
    }

    @Test
    @DisplayName("Debe describir producto con impuesto")
    void testDescribeProductWithTax() {
        Product product = controller.createBaseProduct("Medicamento", 5000);
        product = controller.applyTax(product, 19);
        String description = controller.describeProduct(product);

        assertTrue(description.contains("IVA 19%"), "Descripción debe incluir impuesto");
    }

    @Test
    @DisplayName("Debe describir producto con descuento e impuesto")
    void testDescribeProductWithDiscountAndTax() {
        Product product = controller.createBaseProduct("Producto Premium", 10000);
        product = controller.applyDiscount(product, 15);
        product = controller.applyTax(product, 19);
        String description = controller.describeProduct(product);

        assertTrue(description.contains("Descuento 15%"), "Debe contener descuento");
        assertTrue(description.contains("IVA 19%"), "Debe contener impuesto");
    }

    @Test
    @DisplayName("Debe aplicar descuento máximo al 100%")
    void testApplyMaximumDiscount() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product discounted = controller.applyDiscount(product, 100);

        assertEquals(0, discounted.getPrice(), 0.01, "Descuento al 100% debe resultar en precio 0");
    }

    @Test
    @DisplayName("Debe limitar descuento a 100% cuando se excede")
    void testCapDiscountAt100Percent() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product discounted = controller.applyDiscount(product, 150);

        assertEquals(0, discounted.getPrice(), 0.01, "Descuento mayor a 100% debe limitarse a 100%");
    }

    @Test
    @DisplayName("Debe aplicar diferentes porcentajes de descuento")
    void testApplyVariousDiscountPercentages() {
        double[] discounts = {5, 10, 25, 50};
        
        for (double discount : discounts) {
            Product product = controller.createBaseProduct("Producto", 1000);
            Product discounted = controller.applyDiscount(product, discount);
            double expected = 1000 * (1 - discount / 100.0);
            assertEquals(expected, discounted.getPrice(), 0.01, "Descuento de " + discount + "% incorrecto");
        }
    }

    @Test
    @DisplayName("Debe aplicar diferentes porcentajes de impuesto")
    void testApplyVariousTaxPercentages() {
        double[] taxes = {5, 12, 19, 28};
        
        for (double tax : taxes) {
            Product product = controller.createBaseProduct("Producto", 1000);
            Product taxed = controller.applyTax(product, tax);
            double expected = 1000 * (1 + tax / 100.0);
            assertEquals(expected, taxed.getPrice(), 0.01, "Impuesto de " + tax + "% incorrecto");
        }
    }

    @Test
    @DisplayName("Debe procesar flujo completo con múltiples decoradores")
    void testCompleteFlowWithMultipleDecorators() {
        Product product = controller.createBaseProduct("Medicamento Complejo", 20000);
        product = controller.applyDiscount(product, 20);
        product = controller.applyTax(product, 19);
        product = controller.applyDiscount(product, 5);
        product = controller.applyTax(product, 8);

        double finalPrice = controller.calculateFinalPrice(product);
        assertNotEquals(20000, finalPrice, "El precio final debe diferir del precio base");
        assertTrue(finalPrice > 0, "El precio final debe ser positivo");
    }

    @Test
    @DisplayName("Debe mantener consistencia en múltiples cálculos")
    void testConsistencyInMultipleCalculations() {
        Product product1 = controller.createBaseProduct("Producto A", 5000);
        product1 = controller.applyDiscount(product1, 10);
        product1 = controller.applyTax(product1, 19);

        Product product2 = controller.createBaseProduct("Producto B", 5000);
        product2 = controller.applyDiscount(product2, 10);
        product2 = controller.applyTax(product2, 19);

        assertEquals(product1.getPrice(), product2.getPrice(), 0.01, "Precios finales deben ser iguales");
    }

    @Test
    @DisplayName("Debe procesar producto con precio decimal")
    void testProcessProductWithDecimalPrice() {
        Product product = controller.createBaseProduct("Producto Decimal", 3500.50);
        assertEquals(3500.50, product.getPrice(), 0.01, "Debe preservar precio decimal");
    }

    @Test
    @DisplayName("Debe aplicar descuento decimal")
    void testApplyDecimalDiscount() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product discounted = controller.applyDiscount(product, 12.5);

        double expected = 1000 * 0.875;
        assertEquals(expected, discounted.getPrice(), 0.01, "Descuento decimal debe funcionar");
    }

    @Test
    @DisplayName("Debe aplicar impuesto decimal")
    void testApplyDecimalTax() {
        Product product = controller.createBaseProduct("Producto", 1000);
        Product taxed = controller.applyTax(product, 7.5);

        double expected = 1000 * 1.075;
        assertEquals(expected, taxed.getPrice(), 0.01, "Impuesto decimal debe funcionar");
    }

    @Test
    @DisplayName("Debe manejar nombres especiales de productos")
    void testHandleSpecialProductNames() {
        Product product = controller.createBaseProduct("Medicamento 123-ABC_XYZ", 5000);
        assertEquals("Medicamento 123-ABC_XYZ", product.getDescription(), "Debe aceptar nombres especiales");
    }

    @Test
    @DisplayName("Debe aplicar descuento a precio muy alto")
    void testApplyDiscountToHighPrice() {
        Product product = controller.createBaseProduct("Medicina Cara", 500000);
        product = controller.applyDiscount(product, 10);

        double expected = 500000 * 0.90;
        assertEquals(expected, product.getPrice(), 0.01, "Descuento debe aplicarse correctamente a precios altos");
    }

    @Test
    @DisplayName("Debe aplicar impuesto a precio muy bajo")
    void testApplyTaxToLowPrice() {
        Product product = controller.createBaseProduct("Medicina Barata", 100);
        product = controller.applyTax(product, 19);

        double expected = 100 * 1.19;
        assertEquals(expected, product.getPrice(), 0.01, "Impuesto debe aplicarse correctamente a precios bajos");
    }

    @Test
    @DisplayName("Debe crear varios productos independientes")
    void testCreateMultipleIndependentProducts() {
        Product product1 = controller.createBaseProduct("Producto 1", 1000);
        Product product2 = controller.createBaseProduct("Producto 2", 2000);
        Product product3 = controller.createBaseProduct("Producto 3", 3000);

        assertEquals(1000, product1.getPrice());
        assertEquals(2000, product2.getPrice());
        assertEquals(3000, product3.getPrice());
    }

    @Test
    @DisplayName("Debe aplicar mismo descuento a múltiples productos")
    void testApplySameDiscountToMultipleProducts() {
        Product product1 = controller.createBaseProduct("Producto 1", 1000);
        Product product2 = controller.createBaseProduct("Producto 2", 2000);
        Product product3 = controller.createBaseProduct("Producto 3", 3000);

        product1 = controller.applyDiscount(product1, 10);
        product2 = controller.applyDiscount(product2, 10);
        product3 = controller.applyDiscount(product3, 10);

        assertEquals(900, product1.getPrice(), 0.01);
        assertEquals(1800, product2.getPrice(), 0.01);
        assertEquals(2700, product3.getPrice(), 0.01);
    }
}