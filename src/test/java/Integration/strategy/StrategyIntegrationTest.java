package Integration.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.*;

@DisplayName("Pruebas de Integración del Patrón Strategy")
class StrategyIntegrationTest {

    private DiscountContext contextoDescuento;
    private static final double PRECIO_BASE = 10000;
    private static final double PRECIO_ALTO = 100000;
    private static final double TOLERANCIA = 0.1;

    @BeforeEach
    void configurarContextoDescuento() {
        contextoDescuento = new DiscountContext();
    }

    @Test
    @DisplayName("Debe aplicar todas las estrategias de descuento disponibles correctamente")
    void debeAplicarTodasLasEstrategiasCorrectamente() {
        // Estrategia: Sin descuento
        contextoDescuento.setStrategy(new NoDiscount());
        assertEquals(PRECIO_BASE, contextoDescuento.apply(PRECIO_BASE), TOLERANCIA,
            "Con NoDiscount, el precio debe permanecer sin cambios en $10.000");

        // Estrategia: Descuento porcentual del 15%
        contextoDescuento.setStrategy(new PercentageDiscount(15));
        assertEquals(8500, contextoDescuento.apply(PRECIO_BASE), TOLERANCIA,
            "Con descuento del 15%, el precio debe ser $8.500");

        // Estrategia: Descuento VIP con umbral de $50.000 y 20%
        contextoDescuento.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, contextoDescuento.apply(PRECIO_ALTO), TOLERANCIA,
            "Con descuento VIP del 20% sobre $100.000, el precio debe ser $80.000");
    }

    @Test
    @DisplayName("Debe cambiar dinámicamente entre diferentes estrategias de descuento")
    void debeCambiarDinamicamenteEntreEstrategias() {
        double precioBase = 15000;

        // Aplicar primera estrategia
        contextoDescuento.setStrategy(new PercentageDiscount(10));
        double precio1 = contextoDescuento.apply(precioBase);
        assertEquals(13500, precio1, TOLERANCIA, 
            "Primera estrategia: 10% de descuento");

        // Cambiar a segunda estrategia
        contextoDescuento.setStrategy(new PercentageDiscount(25));
        double precio2 = contextoDescuento.apply(precioBase);
        assertEquals(11250, precio2, TOLERANCIA, 
            "Segunda estrategia: 25% de descuento");

        // Cambiar a tercera estrategia
        contextoDescuento.setStrategy(new NoDiscount());
        double precio3 = contextoDescuento.apply(precioBase);
        assertEquals(15000, precio3, TOLERANCIA, 
            "Tercera estrategia: sin descuento");
    }

    @Test
    @DisplayName("Debe aplicar descuento VIP solo cuando se cumple el umbral mínimo")
    void debeAplicarDescuentoVIPSoloConUmbral() {
        double umbral = 50000;
        double descuento = 25;
        contextoDescuento.setStrategy(new VIPClientDiscount(umbral, descuento));

        // Por encima del umbral
        double precioAlto = 100000;
        double resultadoAlto = contextoDescuento.apply(precioAlto);
        assertEquals(75000, resultadoAlto, TOLERANCIA,
            "Con precio de $100.000 sobre umbral de $50.000, debe aplicarse descuento VIP del 25%");

        // Por debajo del umbral
        double precioBajo = 30000;
        double resultadoBajo = contextoDescuento.apply(precioBajo);
        assertEquals(30000, resultadoBajo, TOLERANCIA,
            "Con precio de $30.000 bajo umbral de $50.000, no debe aplicarse descuento VIP");
    }

    @Test
    @DisplayName("Debe manejar múltiples cambios de estrategia en el mismo contexto")
    void debeManejarMultiplesCambiosEstrategia() {
        double precio = 20000;

        // Primera aplicación
        contextoDescuento.setStrategy(new PercentageDiscount(10));
        assertEquals(18000, contextoDescuento.apply(precio), TOLERANCIA);

        // Segunda aplicación con diferente estrategia
        contextoDescuento.setStrategy(new PercentageDiscount(20));
        assertEquals(16000, contextoDescuento.apply(precio), TOLERANCIA);

        // Tercera aplicación con VIP
        contextoDescuento.setStrategy(new VIPClientDiscount(15000, 15));
        assertEquals(17000, contextoDescuento.apply(precio), TOLERANCIA);

        // Cuarta aplicación sin descuento
        contextoDescuento.setStrategy(new NoDiscount());
        assertEquals(20000, contextoDescuento.apply(precio), TOLERANCIA);
    }

    @Test
    @DisplayName("Debe calcular correctamente descuentos con diferentes porcentajes")
    void debeCalcularDescuentosConDiferentesPorcentajes() {
        double precioBase = 50000;

        // 5% de descuento
        contextoDescuento.setStrategy(new PercentageDiscount(5));
        assertEquals(47500, contextoDescuento.apply(precioBase), TOLERANCIA);

        // 15% de descuento
        contextoDescuento.setStrategy(new PercentageDiscount(15));
        assertEquals(42500, contextoDescuento.apply(precioBase), TOLERANCIA);

        // 30% de descuento
        contextoDescuento.setStrategy(new PercentageDiscount(30));
        assertEquals(35000, contextoDescuento.apply(precioBase), TOLERANCIA);

        // 50% de descuento
        contextoDescuento.setStrategy(new PercentageDiscount(50));
        assertEquals(25000, contextoDescuento.apply(precioBase), TOLERANCIA);
    }

    @Test
    @DisplayName("Debe validar comportamiento VIP con diferentes umbrales")
    void debeValidarComportamientoVIPConDiferentesUmbrales() {
        double precio = 75000;

        // Umbral bajo (cumple)
        contextoDescuento.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(60000, contextoDescuento.apply(precio), TOLERANCIA,
            "Con umbral de $50.000 debe aplicarse descuento");

        // Umbral exacto (cumple)
        contextoDescuento.setStrategy(new VIPClientDiscount(75000, 15));
        assertEquals(63750, contextoDescuento.apply(precio), TOLERANCIA,
            "Con umbral exacto de $75.000 debe aplicarse descuento");

        // Umbral alto (no cumple)
        contextoDescuento.setStrategy(new VIPClientDiscount(80000, 25));
        assertEquals(75000, contextoDescuento.apply(precio), TOLERANCIA,
            "Con umbral de $80.000 NO debe aplicarse descuento");
    }

    @Test
    @DisplayName("Debe mantener consistencia al aplicar misma estrategia múltiples veces")
    void debeMantenerConsistenciaConMismaEstrategia() {
        contextoDescuento.setStrategy(new PercentageDiscount(12));
        double precio = 25000;

        double resultado1 = contextoDescuento.apply(precio);
        double resultado2 = contextoDescuento.apply(precio);
        double resultado3 = contextoDescuento.apply(precio);

        assertEquals(resultado1, resultado2, TOLERANCIA,
            "Múltiples aplicaciones de la misma estrategia deben dar el mismo resultado");
        assertEquals(resultado2, resultado3, TOLERANCIA,
            "Los resultados deben ser consistentes");
        assertEquals(22000, resultado1, TOLERANCIA,
            "El descuento del 12% sobre $25.000 debe ser $22.000");
    }

    @Test
    @DisplayName("Debe integrar correctamente todas las estrategias en un flujo completo")
    void debeIntegrarTodasEstrategiasEnFlujoCompleto() {
        double[] precios = {10000, 25000, 50000, 75000, 100000};
        
        for (double precio : precios) {
            // Sin descuento
            contextoDescuento.setStrategy(new NoDiscount());
            assertEquals(precio, contextoDescuento.apply(precio), TOLERANCIA);

            // Con descuento porcentual
            contextoDescuento.setStrategy(new PercentageDiscount(10));
            assertEquals(precio * 0.9, contextoDescuento.apply(precio), TOLERANCIA);

            // Con descuento VIP si aplica
            contextoDescuento.setStrategy(new VIPClientDiscount(40000, 20));
            double esperadoVIP = precio >= 40000 ? precio * 0.8 : precio;
            assertEquals(esperadoVIP, contextoDescuento.apply(precio), TOLERANCIA,
                "El descuento VIP debe aplicarse correctamente según el umbral");
        }
    }
}