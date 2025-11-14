package Junit.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;

@DisplayName("Pruebas del Patrón Decorator para Productos")
class DecoratorControllerTest {

    private DecoratorController controlador;

    @BeforeEach
    void configurarPrueba() {
        controlador = new DecoratorController();
    }

    @Test
    @DisplayName("Debe aplicar descuento del 10% y luego IVA del 19% correctamente")
    void debeAplicarDescuentoYLuegoIVA() {
        Product producto = controlador.createBaseProduct("Ibuprofeno", 10000);
        producto = controlador.applyDiscountThenTax(producto, 10, 19);
        
        double precioFinal = controlador.calculateFinalPrice(producto);
        
        assertEquals(10710.0, precioFinal, 0.1, 
            "El precio final debe ser $10.710 después de aplicar descuento del 10% e IVA del 19%");
    }

    @Test
    @DisplayName("Debe aplicar solo descuento del 20% sin IVA")
    void debeAplicarSoloDescuento() {
        Product producto = controlador.createBaseProduct("Acetaminofén", 5000);
        producto = controlador.applyDiscount(producto, 20);
        
        double precioFinal = producto.getPrice();
        
        assertEquals(4000, precioFinal, 0.1, 
            "El precio debe ser $4.000 después de aplicar descuento del 20%");
    }

    @Test
    @DisplayName("La descripción del producto debe incluir información del IVA aplicado")
    void laDescripcionDebeIncluirIVA() {
        Product producto = controlador.createBaseProduct("Jarabe", 8000);
        producto = controlador.applyTax(producto, 19);
        
        String descripcion = controlador.describeProduct(producto);
        
        assertTrue(descripcion.contains("IVA"), 
            "La descripción del producto debe mencionar el IVA cuando se aplica");
    }

    @Test
    @DisplayName("Debe mantener el precio base sin decoradores")
    void debeManternerPrecioBaseSinDecoradores() {
        Product producto = controlador.createBaseProduct("Vitamina C", 12000);
        
        double precioFinal = controlador.calculateFinalPrice(producto);
        
        assertEquals(12000, precioFinal, 0.1, 
            "El precio final debe ser igual al precio base cuando no se aplican decoradores");
    }

    @Test
    @DisplayName("Debe aplicar múltiples decoradores en el orden correcto")
    void debeAplicarMultiplesDecoradoresEnOrden() {
        Product producto = controlador.createBaseProduct("Antibiótico", 20000);
        producto = controlador.applyDiscount(producto, 15); 
        producto = controlador.applyTax(producto, 19);       
        
        double precioFinal = controlador.calculateFinalPrice(producto);
        
        assertEquals(20230.0, precioFinal, 0.1, 
            "El precio debe reflejar descuento del 15% seguido de IVA del 19%");
    }
}
