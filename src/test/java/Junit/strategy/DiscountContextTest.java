package Junit.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.model.*;
import patrones_farmacia.strategy.controller.*;

@DisplayName("Pruebas del Patrón Strategy para Descuentos")
class DiscountContextTest {

    private DiscountContext contexto;

    @BeforeEach
    void configurarContexto() {
        contexto = new DiscountContext();
    }

    @Test
    @DisplayName("Debe aplicar descuento porcentual del 10% correctamente")
    void debeAplicarDescuentoPorcentual() {
        contexto.setStrategy(new PercentageDiscount(10));
        
        double precioFinal = contexto.apply(10000);
        
        assertEquals(9000, precioFinal, 0.1, 
            "Un descuento del 10% sobre $10.000 debe resultar en $9.000");
    }

    @Test
    @DisplayName("Debe aplicar descuento VIP cuando se cumple el umbral mínimo")
    void debeAplicarDescuentoVIPCuandoCumpleUmbral() {
        double umbralMinimo = 50000;
        double porcentajeDescuento = 20;
        contexto.setStrategy(new VIPClientDiscount(umbralMinimo, porcentajeDescuento));
        
        double precioFinal = contexto.apply(100000);
        
        assertEquals(80000, precioFinal, 0.1, 
            "Un descuento VIP del 20% sobre $100.000 debe resultar en $80.000");
    }

    @Test
    @DisplayName("No debe aplicar descuento cuando se usa la estrategia NoDiscount")
    void noDebeAplicarDescuentoConEstrategiaNoDiscount() {
        contexto.setStrategy(new NoDiscount());
        
        double precioFinal = contexto.apply(10000);
        
        assertEquals(10000, precioFinal, 0.1, 
            "Con la estrategia NoDiscount, el precio debe permanecer sin cambios");
    }

    @Test
    @DisplayName("Debe poder cambiar de estrategia de descuento dinámicamente")
    void debeCambiarEstrategiaDescuentoDinamicamente() {
        double precioBase = 15000;
        
        contexto.setStrategy(new PercentageDiscount(15));
        double precio1 = contexto.apply(precioBase);
        assertEquals(12750, precio1, 0.1);
        
        contexto.setStrategy(new NoDiscount());
        double precio2 = contexto.apply(precioBase);
        assertEquals(15000, precio2, 0.1);

        contexto.setStrategy(new PercentageDiscount(25));
        double precio3 = contexto.apply(precioBase);
        assertEquals(11250, precio3, 0.1);
    }

    @Test
    @DisplayName("Descuento VIP no debe aplicarse si no se alcanza el umbral")
    void descuentoVIPNoDebeAplicarseSinAlcanzarUmbral() {
        double umbralMinimo = 50000;
        double porcentajeDescuento = 20;
        contexto.setStrategy(new VIPClientDiscount(umbralMinimo, porcentajeDescuento));
        
        double precioFinal = contexto.apply(30000);

        assertEquals(30000, precioFinal, 0.1, 
            "El descuento VIP no debe aplicarse si el monto no alcanza el umbral mínimo");
    }

    @Test
    @DisplayName("Debe aplicar descuento porcentual del 50% correctamente")
    void debeAplicarDescuentoPorcentualAlto() {
        contexto.setStrategy(new PercentageDiscount(50));
        
        double precioFinal = contexto.apply(20000);
        
        assertEquals(10000, precioFinal, 0.1, 
            "Un descuento del 50% sobre $20.000 debe resultar en $10.000");
    }

    @Test
    @DisplayName("Debe manejar correctamente descuentos con valores decimales")
    void debeManejarDescuentosConValoresDecimales() {
        contexto.setStrategy(new PercentageDiscount(12.5));
        
        double precioFinal = contexto.apply(8000);
        
        assertEquals(7000, precioFinal, 0.1, 
            "Debe manejar correctamente porcentajes decimales como 12.5%");
    }

    @Test
    @DisplayName("Descuento VIP con umbral exacto debe aplicarse")
    void descuentoVIPConUmbralExactoDebeAplicarse() {
        double umbralMinimo = 75000;
        double porcentajeDescuento = 15;
        contexto.setStrategy(new VIPClientDiscount(umbralMinimo, porcentajeDescuento));
        
        double precioFinal = contexto.apply(75000);
        
        assertEquals(63750, precioFinal, 0.1, 
            "El descuento VIP debe aplicarse cuando el monto es exactamente igual al umbral");
    }
}
