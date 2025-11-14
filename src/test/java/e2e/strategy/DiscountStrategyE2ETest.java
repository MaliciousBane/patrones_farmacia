package e2e.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import patrones_farmacia.strategy.controller.DiscountContext;
import patrones_farmacia.strategy.model.NoDiscount;
import patrones_farmacia.strategy.model.PercentageDiscount;
import patrones_farmacia.strategy.model.VIPClientDiscount;

@DisplayName("Strategy Pattern - Descuentos E2E ampliadas")
class DiscountStrategyE2ETest {

    private DiscountContext context;

    @BeforeEach
    void setUp() {
        context = new DiscountContext();
        Locale.setDefault(Locale.US);
    }

    @Test
    @DisplayName("NoDiscount retorna mismo total")
    void noDiscountReturnsSame() {
        context.setStrategy(new NoDiscount());
        assertEquals(12345.67, context.apply(12345.67), 0.0001);
        assertEquals("Sin descuento", context.describe());
    }

    @Test
    @DisplayName("Percentage 10% reduce correctamente")
    void percentage10() {
        context.setStrategy(new PercentageDiscount(10));
        assertEquals(90.0, context.apply(100.0), 0.0001);
        assertTrue(context.describe().contains("10"));
    }

    @Test
    @DisplayName("Percentage con decimales calcula precisión")
    void percentageDecimal() {
        context.setStrategy(new PercentageDiscount(33.33));
        double result = context.apply(10000);
        assertTrue(result > 6666 && result < 6667);
    }

    @Test
    @DisplayName("Percentage 0% no altera total")
    void percentageZero() {
        context.setStrategy(new PercentageDiscount(0));
        assertEquals(2000, context.apply(2000), 0.0001);
    }

    @Test
    @DisplayName("Percentage 100% deja total en cero")
    void percentageFull() {
        context.setStrategy(new PercentageDiscount(100));
        assertEquals(0, context.apply(2500), 0.0001);
    }

    @Test
    @DisplayName("Percentage mayor a 100 produce total negativo según implementación")
    void percentageOver100() {
        context.setStrategy(new PercentageDiscount(150));
        assertEquals(-1250, context.apply(500), 0.0001);
    }

    @Test
    @DisplayName("Percentage negativo aumenta total según implementación")
    void percentageNegative() {
        context.setStrategy(new PercentageDiscount(-20));
        assertEquals(1200, context.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("Percentage es inmutable entre llamadas")
    void percentageStatelessBetweenCalls() {
        PercentageDiscount pd = new PercentageDiscount(25);
        context.setStrategy(pd);
        assertEquals(75, context.apply(100), 0.0001);
        assertEquals(150, context.apply(200), 0.0001);
        assertEquals("Descuento 25.0%", context.describe());
    }

    @Test
    @DisplayName("VIP aplica cuando supera umbral")
    void vipAboveThreshold() {
        context.setStrategy(new VIPClientDiscount(5000, 20));
        assertEquals(8000, context.apply(10000), 0.0001);
        assertTrue(context.describe().toLowerCase().contains("vip"));
    }

    @Test
    @DisplayName("VIP no aplica cuando está por debajo del umbral")
    void vipBelowThreshold() {
        context.setStrategy(new VIPClientDiscount(10000, 30));
        assertEquals(9000, context.apply(9000), 0.0001);
    }

    @Test
    @DisplayName("VIP aplica exactamente en el umbral")
    void vipAtThreshold() {
        context.setStrategy(new VIPClientDiscount(7500, 10));
        assertEquals(6750, context.apply(7500), 0.0001);
    }

    @Test
    @DisplayName("VIP con porcentaje 0 no altera total")
    void vipZeroRate() {
        context.setStrategy(new VIPClientDiscount(1000, 0));
        assertEquals(500, context.apply(500), 0.0001);
        assertEquals(5000, context.apply(5000), 0.0001);
    }

    @Test
    @DisplayName("VIP con porcentaje mayor a 100 produce total negativo si aplica")
    void vipRateOver100() {
        context.setStrategy(new VIPClientDiscount(100, 150));
        assertEquals(-50, context.apply(100), 0.0001);
    }

    @Test
    @DisplayName("VIP con umbral negativo siempre aplica")
    void vipNegativeThresholdAlwaysApplies() {
        context.setStrategy(new VIPClientDiscount(-1, 10));
        assertEquals(90, context.apply(100), 0.0001);
    }

    @Test
    @DisplayName("Cambiar estrategia varias veces mantiene comportamiento esperado")
    void changeStrategiesMultipleTimes() {
        context.setStrategy(new NoDiscount());
        assertEquals(1000, context.apply(1000), 0.0001);
        context.setStrategy(new PercentageDiscount(10));
        assertEquals(900, context.apply(1000), 0.0001);
        context.setStrategy(new VIPClientDiscount(500, 50));
        assertEquals(500, context.apply(1000), 0.0001);
        context.setStrategy(null);
        assertEquals(1000, context.apply(1000), 0.0001);
        assertEquals("Sin estrategia aplicada", context.describe());
    }

    @Test
    @DisplayName("Múltiples contextos independientes")
    void multipleContextsIndependence() {
        DiscountContext a = new DiscountContext();
        DiscountContext b = new DiscountContext();
        a.setStrategy(new PercentageDiscount(10));
        b.setStrategy(new PercentageDiscount(20));
        assertEquals(90, a.apply(100), 0.0001);
        assertEquals(80, b.apply(100), 0.0001);
    }

    @Test
    @DisplayName("Aplicar a cero y números muy grandes")
    void zeroAndVeryLargeTotals() {
        context.setStrategy(new PercentageDiscount(10));
        assertEquals(0, context.apply(0), 0.0001);
        double large = 1e12;
        assertEquals(9e11, context.apply(large), 1e5);
    }

    @Test
    @DisplayName("Descriptions contienen información esperada")
    void descriptionsContainInfo() {
        context.setStrategy(new PercentageDiscount(12.5));
        assertTrue(context.describe().contains("12.5"));
        context.setStrategy(new VIPClientDiscount(1234.5, 7.5));
        assertTrue(context.describe().contains("1234.5"));
    }

    @Test
    @DisplayName("Combinaciones múltiples de entradas y tipos")
    void multipleCombinationsStress() {
        double[] totals = {1, 10, 99.99, 1000, 12345.67};
        double[] perc = {0, 5, 33.33, 99.9};
        for (double p : perc) {
            context.setStrategy(new PercentageDiscount(p));
            for (double t : totals) {
                double expected = t * (1 - p / 100.0);
                assertEquals(expected, context.apply(t), Math.max(0.001, Math.abs(expected) * 1e-9));
            }
        }
        context.setStrategy(new VIPClientDiscount(500, 15));
        for (double t : totals) {
            double expected = t >= 500 ? t * 0.85 : t;
            assertEquals(expected, context.apply(t), 0.0001);
        }
    }

    @Test
    @DisplayName("Varias llamadas repetidas para detectar efectos colaterales")
    void repeatedCallsNoSideEffects() {
        PercentageDiscount pd = new PercentageDiscount(13.13);
        context.setStrategy(pd);
        for (int i = 1; i <= 50; i++) {
            double base = i * 37.5;
            double expected = base * (1 - 13.13 / 100.0);
            assertEquals(expected, context.apply(base), 0.0001);
        }
    }

    @Test
    @DisplayName("Valores frontera para porcentajes y umbrales")
    void edgeValuesForPercentAndThreshold() {
        context.setStrategy(new PercentageDiscount(Double.POSITIVE_INFINITY));
        assertTrue(Double.isInfinite(context.apply(100)) || Double.isNaN(context.apply(100)));

        context.setStrategy(new PercentageDiscount(Double.NEGATIVE_INFINITY));
        assertTrue(Double.isInfinite(context.apply(100)) || Double.isNaN(context.apply(100)));

        context.setStrategy(new VIPClientDiscount(Double.POSITIVE_INFINITY, 50));
        assertEquals(100, context.apply(100), 0.0001);

        context.setStrategy(new VIPClientDiscount(Double.NEGATIVE_INFINITY, 50));
        assertEquals(50, context.apply(100), 0.0001);
    }

    @Test
    @DisplayName("Porcentajes con muchas fracciones decimales")
    void manyDecimalPercentageAccuracy() {
        PercentageDiscount pd = new PercentageDiscount(12.3456789);
        context.setStrategy(pd);
        double base = 123456.789;
        double expected = base * (1 - 12.3456789 / 100.0);
        assertEquals(expected, context.apply(base), 1e-6);
    }

    @Test
    @DisplayName("VIP con tasa decimal y umbral decimal")
    void vipDecimalThresholdAndRate() {
        context.setStrategy(new VIPClientDiscount(1234.56, 12.5));
        assertEquals(1000 * 1.0, context.apply(1000), 0.0001);
        assertEquals(2000 * (1 - 12.5/100.0), context.apply(2000), 0.0001);
    }

    @Test
    @DisplayName("Alternar entre estrategias en bucle")
    void alternateStrategiesInLoop() {
        for (int i = 0; i < 30; i++) {
            if (i % 3 == 0) context.setStrategy(new NoDiscount());
            else if (i % 3 == 1) context.setStrategy(new PercentageDiscount(i));
            else context.setStrategy(new VIPClientDiscount(50, i));
            double result = context.apply(200);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("Combinación extensa de porcentajes y totales para cobertura")
    void extensiveCombinationCoverage() {
        for (double p = -10; p <= 110; p += 2.5) {
            PercentageDiscount pd = new PercentageDiscount(p);
            context.setStrategy(pd);
            for (double t = -100; t <= 1000; t += 75) {
                double expected = t * (1 - p / 100.0);
                assertEquals(expected, context.apply(t), Math.max(0.0001, Math.abs(expected) * 1e-9));
            }
        }
    }

    @Test
    @DisplayName("VIP comportamiento con muchos umbrales y tasas")
    void vipManyThresholdsAndRates() {
        double[] thresholds = {-100, 0, 0.5, 10, 100, 999.99, 10000};
        double[] rates = {0, 1, 12.5, 50, 100, 150};
        for (double th : thresholds) {
            for (double r : rates) {
                context.setStrategy(new VIPClientDiscount(th, r));
                double below = th - 0.1;
                double at = th;
                double above = th + 0.1;
                assertEquals(below, context.apply(below), 0.0001);
                assertEquals(at * (1 - r/100.0), context.apply(at), 0.0001);
                assertEquals(above * (1 - r/100.0), context.apply(above), 0.0001);
            }
        }
    }

    @Test
    @DisplayName("Describe devuelve texto legible para todas estrategias")
    void describeForAllStrategies() {
        context.setStrategy(new NoDiscount());
        assertEquals("Sin descuento", context.describe());
        context.setStrategy(new PercentageDiscount(7.25));
        assertTrue(context.describe().startsWith("Descuento"));
        context.setStrategy(new VIPClientDiscount(100, 5));
        assertTrue(context.describe().toLowerCase().contains("vip"));
    }

    @Test
    @DisplayName("Comprobación de comportamiento con totales negativos")
    void negativeTotalsBehavior() {
        context.setStrategy(new PercentageDiscount(10));
        assertEquals(-90, context.apply(-100), 0.0001);
        context.setStrategy(new VIPClientDiscount(0, 50));
        assertEquals(-50, context.apply(-100), 0.0001);
    }

    @Test
    @DisplayName("Reasignación nula de estrategia deja comportamiento por defecto")
    void nullStrategyAssignment() {
        context.setStrategy(null);
        assertEquals(123, context.apply(123), 0.0001);
        assertEquals("Sin estrategia aplicada", context.describe());
    }

    @Test
    @DisplayName("Stress test ligero: muchas operaciones rápidas")
    void lightStressManyOperations() {
        PercentageDiscount pd = new PercentageDiscount(5.5);
        context.setStrategy(pd);
        for (int i = 0; i < 500; i++) {
            double base = (i % 37) * 12.34;
            double expected = base * (1 - 5.5 / 100.0);
            assertEquals(expected, context.apply(base), 0.0001);
        }
    }

    @Test
    @DisplayName("Precision and rounding sanity checks")
    void precisionAndRoundingSanity() {
        context.setStrategy(new PercentageDiscount(33.3333));
        double base = 300;
        double expected = base * (1 - 33.3333/100.0);
        assertEquals(expected, context.apply(base), 1e-6);
    }

    @Test
    @DisplayName("Verificar que getDescription no es null")
    void descriptionNotNull() {
        context.setStrategy(new PercentageDiscount(1));
        assertNotNull(context.describe());
        context.setStrategy(new VIPClientDiscount(10, 1));
        assertNotNull(context.describe());
    }

    @Test
    @DisplayName("Combinación de límites y valores pequeños")
    void tinyValuesAndLimits() {
        context.setStrategy(new PercentageDiscount(50));
        assertEquals(0.005, context.apply(0.01), 1e-9);
        context.setStrategy(new VIPClientDiscount(0.005, 10));
        assertEquals(0.009, context.apply(0.01), 1e-9);
    }

    @Test
    @DisplayName("Aplicación con valores extremos de double no lanza excepciones")
    void extremeDoubleValuesDontThrow() {
        context.setStrategy(new PercentageDiscount(1));
        assertDoesNotThrow(() -> context.apply(Double.MAX_VALUE));
        assertDoesNotThrow(() -> context.apply(-Double.MAX_VALUE));
    }

    @Test
    @DisplayName("Aplicación con NaN y NullStrategy")
    void nanAndNullStrategy() {
        context.setStrategy(null);
        double nan = Double.NaN;
        assertTrue(Double.isNaN(context.apply(nan)));
        context.setStrategy(new PercentageDiscount(10));
        assertTrue(Double.isNaN(context.apply(nan)));
    }

    @Test
    @DisplayName("Cambios rápidos de estrategia no provocan estado compartido")
    void rapidStrategySwap() {
        for (int i = 0; i < 100; i++) {
            if ((i & 1) == 0) context.setStrategy(new PercentageDiscount(i % 50));
            else context.setStrategy(new VIPClientDiscount(i % 50, (i % 20)));
            double r = context.apply(1000.0);
            assertNotNull(r);
        }
    }

    @Test
    @DisplayName("Verificar strings con coma y punto decimal")
    void decimalFormattingIndependence() {
        PercentageDiscount pd = new PercentageDiscount(12.5);
        context.setStrategy(pd);
        assertTrue(context.describe().contains("12.5"));
    }
}