package e2e.decorator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;

class PriceDecoratorE2ETest {

    @Test
    void shouldApplyDiscountAndTaxCorrectly() {
        DecoratorController controller = new DecoratorController();
        Product product = controller.createBaseProduct("Vitamina C", 10000);
        
        product = controller.applyDiscount(product, 10);
        
        product = controller.applyTax(product, 19);

        double finalPrice = controller.calculateFinalPrice(product);
        assertEquals(10710, finalPrice, 0.5, 
                     "El precio final debe incluir 10% de descuento y 19% de impuesto");
    }
}