package Junit.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.strategy.model.*;
import patrones_farmacia.strategy.controller.*;
import patrones_farmacia.strategy.view.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Pruebas del Patrón Strategy para Descuentos - Ampliadas")
class DiscountContextTest {

    private DiscountContext contexto;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void configurarContexto() {
        contexto = new DiscountContext();
        originalOut = System.out;
        originalIn = System.in;
        baos = new ByteArrayOutputStream();
        System.setOut(new PrintStream(baos));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    @DisplayName("Aplicar descuento porcentual 10% sobre 10000")
    void aplica10Porciento() {
        contexto.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, contexto.apply(10000), 0.0001);
        assertEquals("Descuento 10.0%", contexto.describe());
    }

    @Test
    @DisplayName("NoDiscount mantiene el total")
    void noDiscountMantieneTotal() {
        contexto.setStrategy(new NoDiscount());
        assertEquals(12345.67, contexto.apply(12345.67), 0.0001);
        assertEquals("Sin descuento", contexto.describe());
    }

    @Test
    @DisplayName("VIP aplica cuando alcanza el umbral")
    void vipAplicaSobreUmbral() {
        contexto.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(80000, contexto.apply(100000), 0.0001);
        assertTrue(contexto.describe().contains("VIP"));
    }

    @Test
    @DisplayName("VIP no aplica si no alcanza el umbral")
    void vipNoAplicaSiNoAlcanza() {
        contexto.setStrategy(new VIPClientDiscount(50000, 20));
        assertEquals(40000, contexto.apply(40000), 0.0001);
    }

    @Test
    @DisplayName("VIP aplica en umbral exacto")
    void vipAplicaEnUmbralExacto() {
        contexto.setStrategy(new VIPClientDiscount(75000, 15));
        assertEquals(63750, contexto.apply(75000), 0.0001);
    }

    @Test
    @DisplayName("Porcentaje 0% no cambia precio")
    void porcentajeCero() {
        contexto.setStrategy(new PercentageDiscount(0));
        assertEquals(5000, contexto.apply(5000), 0.0001);
    }

    @Test
    @DisplayName("Porcentaje 100% deja precio en cero")
    void porcentajeCien() {
        contexto.setStrategy(new PercentageDiscount(100));
        assertEquals(0, contexto.apply(9999.99), 0.0001);
    }

    @Test
    @DisplayName("Porcentaje mayor a 100% devuelve valor negativo según implementación")
    void porcentajeMayorCienDevuelveNegativo() {
        contexto.setStrategy(new PercentageDiscount(150));
        assertEquals(-5000, contexto.apply(10000), 0.0001);
    }

    @Test
    @DisplayName("Descuentos con decimales funcionan correctamente")
    void porcentajeDecimal() {
        contexto.setStrategy(new PercentageDiscount(12.5));
        assertEquals(8750, contexto.apply(10000), 0.0001);
    }

    @Test
    @DisplayName("Cambiar estrategia dinámicamente")
    void cambiarEstrategiaDinamicamente() {
        contexto.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, contexto.apply(10000), 0.0001);
        contexto.setStrategy(new NoDiscount());
        assertEquals(10000, contexto.apply(10000), 0.0001);
        contexto.setStrategy(new VIPClientDiscount(5000, 50));
        assertEquals(5000, contexto.apply(10000), 0.0001);
    }

    @Test
    @DisplayName("apply con estrategia nula retorna total original")
    void applyConEstrategiaNula() {
        contexto.setStrategy(null);
        assertEquals(777.77, contexto.apply(777.77), 0.0001);
        assertEquals("Sin estrategia aplicada", contexto.describe());
    }

    @Test
    @DisplayName("Describe convierte correctamente para PercentageDiscount")
    void describePercentage() {
        contexto.setStrategy(new PercentageDiscount(7.5));
        String d = contexto.describe();
        assertTrue(d.contains("7.5"));
    }

    @Test
    @DisplayName("Describe para VIP incluye umbral")
    void describeVIPIncluyeUmbral() {
        contexto.setStrategy(new VIPClientDiscount(20000, 5));
        assertTrue(contexto.describe().contains("20000"));
    }

    @Test
    @DisplayName("Múltiples cálculos consecutivos con misma estrategia")
    void multipleCalculationsConMismaEstrategia() {
        contexto.setStrategy(new PercentageDiscount(10));
        assertEquals(9000, contexto.apply(10000), 0.0001);
        assertEquals(18000, contexto.apply(20000), 0.0001);
        assertEquals(27000, contexto.apply(30000), 0.0001);
    }

    @Test
    @DisplayName("Aplicar descuento a cero retorna cero")
    void aplicarSobreCero() {
        contexto.setStrategy(new PercentageDiscount(10));
        assertEquals(0, contexto.apply(0), 0.0001);
    }

    @Test
    @DisplayName("VIP con umbral muy bajo aplica siempre")
    void vipConUmbralBajo() {
        contexto.setStrategy(new VIPClientDiscount(1, 99));
        assertEquals(1, contexto.apply(100), 0.0001);
    }

    @Test
    @DisplayName("VIP con porcentaje 0 no modifica importe aunque supere umbral")
    void vipConPorcentajeCero() {
        contexto.setStrategy(new VIPClientDiscount(100, 0));
        assertEquals(200, contexto.apply(200), 0.0001);
    }

    @Test
    @DisplayName("Combinación: cambiar entre VIP y Percentage mantiene consistencia")
    void combinarVIPyPercentage() {
        contexto.setStrategy(new VIPClientDiscount(5000, 20));
        assertEquals(4000, contexto.apply(5000), 0.0001);
        contexto.setStrategy(new PercentageDiscount(20));
        assertEquals(4000, contexto.apply(5000), 0.0001);
    }

    @Test
    @DisplayName("Precision en resultados decimales")
    void precisionResultadosDecimales() {
        contexto.setStrategy(new PercentageDiscount(33.33));
        double res = contexto.apply(10000);
        assertTrue(res > 6666 && res < 6667);
    }

    @Test
    @DisplayName("Describe con estrategia nula retorna texto esperado")
    void describeConNulo() {
        contexto.setStrategy(null);
        assertEquals("Sin estrategia aplicada", contexto.describe());
    }

    @Test
    @DisplayName("Estrategia NoDiscount no modifica valores extremos")
    void noDiscountConValoresExtremos() {
        contexto.setStrategy(new NoDiscount());
        assertEquals(Double.MAX_VALUE, contexto.apply(Double.MAX_VALUE), 0.0001);
    }

    @Test
    @DisplayName("VIP con threshold igual a total y porcentaje 100 deja en cero")
    void vipThresholdYPorcentajeCien() {
        contexto.setStrategy(new VIPClientDiscount(1000, 100));
        assertEquals(0, contexto.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount con porcentaje negativo incrementa total")
    void porcentajeNegativoIncrementaTotal() {
        contexto.setStrategy(new PercentageDiscount(-10));
        assertEquals(11000, contexto.apply(10000), 0.0001);
    }

    @Test
    @DisplayName("VIP con threshold 0 aplica siempre")
    void vipWithZeroThresholdAppliesAlways() {
        contexto.setStrategy(new VIPClientDiscount(0, 10));
        assertEquals(900, contexto.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("Multiple strategy switches produce expected outcomes")
    void multipleStrategySwitches() {
        contexto.setStrategy(new PercentageDiscount(5));
        assertEquals(950, contexto.apply(1000), 0.0001);
        contexto.setStrategy(new VIPClientDiscount(2000, 50));
        assertEquals(1000, contexto.apply(1000), 0.0001);
        contexto.setStrategy(new NoDiscount());
        assertEquals(1000, contexto.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount.getDescription formato correcto")
    void percentageDescriptionFormat() {
        PercentageDiscount pd = new PercentageDiscount(2.5);
        assertEquals("Descuento 2.5%", pd.getDescription());
    }

    @Test
    @DisplayName("VIPClientDiscount.getDescription contiene umbral")
    void vipDescriptionContainsThreshold() {
        VIPClientDiscount vip = new VIPClientDiscount(9999, 30);
        assertTrue(vip.getDescription().contains("9999"));
    }

    @Test
    @DisplayName("Apply con valores negativos funciona según implementación")
    void applyWithNegativeTotals() {
        contexto.setStrategy(new PercentageDiscount(50));
        assertEquals(-50, contexto.apply(-100), 0.0001);
    }

    @Test
    @DisplayName("Repeated apply with VIP does not mutate internal state")
    void repeatedApplyVipIsStateless() {
        VIPClientDiscount vip = new VIPClientDiscount(100, 10);
        contexto.setStrategy(vip);
        double a = contexto.apply(200);
        double b = contexto.apply(200);
        assertEquals(a, b, 0.0001);
    }

    @Test
    @DisplayName("NoDiscount.calculate retorna exactamente el mismo total")
    void noDiscountCalculateReturnsSameTotal() {
        NoDiscount nd = new NoDiscount();
        assertEquals(5555.55, nd.calculate(5555.55), 0.0001);
        assertEquals(0, nd.calculate(0), 0.0001);
        assertEquals(-100, nd.calculate(-100), 0.0001);
    }

    @Test
    @DisplayName("NoDiscount.getDescription retorna exactamente 'Sin descuento'")
    void noDiscountDescriptionExact() {
        NoDiscount nd = new NoDiscount();
        assertEquals("Sin descuento", nd.getDescription());
    }

    @Test
    @DisplayName("PercentageDiscount constructor almacena porcentaje")
    void percentageConstructorStoresValue() {
        PercentageDiscount pd = new PercentageDiscount(33.33);
        assertEquals(33.33, pd.calculate(100), 0.01);
    }

    @Test
    @DisplayName("PercentageDiscount.calculate formula correcta")
    void percentageCalculateFormula() {
        PercentageDiscount pd = new PercentageDiscount(50);
        double result = pd.calculate(200);
        assertEquals(200 * (1 - 0.5), result, 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount con 1% descuenta 1/100")
    void percentageOnePercent() {
        PercentageDiscount pd = new PercentageDiscount(1);
        assertEquals(99, pd.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount con 0.5% descuenta correctamente")
    void percentageHalfPercent() {
        PercentageDiscount pd = new PercentageDiscount(0.5);
        assertEquals(99.5, pd.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount con 200% produce negativo")
    void percentageTwoHundred() {
        PercentageDiscount pd = new PercentageDiscount(200);
        assertEquals(-100, pd.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount constructor almacena threshold y rate")
    void vipConstructorStoresValues() {
        VIPClientDiscount vip = new VIPClientDiscount(5000, 15);
        assertEquals(15, vip.calculate(5000), 0.01);
    }

    @Test
    @DisplayName("VIPClientDiscount por debajo de threshold no aplica descuento")
    void vipBelowThresholdNoDiscount() {
        VIPClientDiscount vip = new VIPClientDiscount(1000, 50);
        assertEquals(500, vip.calculate(500), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount exactamente en threshold aplica descuento")
    void vipExactlyAtThresholdApplies() {
        VIPClientDiscount vip = new VIPClientDiscount(1000, 50);
        assertEquals(500, vip.calculate(1000), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount un cent arriba del threshold aplica")
    void vipJustAboveThresholdApplies() {
        VIPClientDiscount vip = new VIPClientDiscount(1000, 50);
        assertEquals(500.5, vip.calculate(1000.01), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount un cent abajo del threshold no aplica")
    void vipJustBelowThresholdNoApply() {
        VIPClientDiscount vip = new VIPClientDiscount(1000, 50);
        assertEquals(999.99, vip.calculate(999.99), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount con rate 100 y umbral alcanzado deja en cero")
    void vipRate100AlcanzandoUmbral() {
        VIPClientDiscount vip = new VIPClientDiscount(100, 100);
        assertEquals(0, vip.calculate(200), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount con rate 0 y umbral alcanzado no modifica")
    void vipRate0AlcanzandoUmbral() {
        VIPClientDiscount vip = new VIPClientDiscount(100, 0);
        assertEquals(200, vip.calculate(200), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount description contiene símbolo $")
    void vipDescriptionContainsDollarSign() {
        VIPClientDiscount vip = new VIPClientDiscount(5000, 15);
        assertTrue(vip.getDescription().contains("$"));
    }

    @Test
    @DisplayName("VIPClientDiscount description contiene 'VIP'")
    void vipDescriptionContainsVIP() {
        VIPClientDiscount vip = new VIPClientDiscount(5000, 15);
        assertTrue(vip.getDescription().contains("VIP"));
    }

    @Test
    @DisplayName("DiscountContext setStrategy y apply en secuencia")
    void contextSetStrategyAndApplySequence() {
        PercentageDiscount pd = new PercentageDiscount(25);
        contexto.setStrategy(pd);
        double result = contexto.apply(1000);
        assertEquals(750, result, 0.0001);
    }

    @Test
    @DisplayName("DiscountContext describe después de setStrategy")
    void contextDescribeAfterSetStrategy() {
        PercentageDiscount pd = new PercentageDiscount(15);
        contexto.setStrategy(pd);
        assertEquals("Descuento 15.0%", contexto.describe());
    }

    @Test
    @DisplayName("DiscountContext puede cambiar strategy múltiples veces")
    void contextCanChangeStrategyMultipleTimes() {
        PercentageDiscount pd1 = new PercentageDiscount(10);
        NoDiscount nd = new NoDiscount();
        VIPClientDiscount vip = new VIPClientDiscount(500, 5);
        
        contexto.setStrategy(pd1);
        assertEquals(90, contexto.apply(100), 0.0001);
        
        contexto.setStrategy(nd);
        assertEquals(100, contexto.apply(100), 0.0001);
        
        contexto.setStrategy(vip);
        assertEquals(95, contexto.apply(100), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount porcentaje 99.99% deja poco")
    void percentageMostly() {
        PercentageDiscount pd = new PercentageDiscount(99.99);
        double result = pd.calculate(10000);
        assertEquals(1, result, 0.1);
    }

    @Test
    @DisplayName("PercentageDiscount porcentaje 0.01% reduce mínimamente")
    void percentageTiny() {
        PercentageDiscount pd = new PercentageDiscount(0.01);
        double result = pd.calculate(10000);
        assertTrue(result > 9999 && result < 10000);
    }

    @Test
    @DisplayName("VIPClientDiscount threshold flotante funciona")
    void vipThresholdFloating() {
        VIPClientDiscount vip = new VIPClientDiscount(1234.56, 10);
        assertEquals(1111.104, vip.calculate(1234.56), 0.01);
    }

    @Test
    @DisplayName("VIPClientDiscount rate flotante funciona")
    void vipRateFloating() {
        VIPClientDiscount vip = new VIPClientDiscount(100, 7.5);
        assertEquals(925, vip.calculate(1000), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount con valores grandes")
    void percentageWithLargeValues() {
        PercentageDiscount pd = new PercentageDiscount(5);
        assertEquals(950000, pd.calculate(1000000), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount con valores muy grandes")
    void vipWithVeryLargeValues() {
        VIPClientDiscount vip = new VIPClientDiscount(1e8, 10);
        assertEquals(9e7, vip.calculate(1e8), 0.0001);
    }

    @Test
    @DisplayName("Concurrent context changes and applies")
    void concurrentContextChangesAndApplies() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Callable<Double>> tasks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            final int idx = i;
            tasks.add(() -> {
                DiscountContext ctx = new DiscountContext();
                if (idx % 3 == 0) {
                    ctx.setStrategy(new PercentageDiscount(10));
                } else if (idx % 3 == 1) {
                    ctx.setStrategy(new VIPClientDiscount(1000, 20));
                } else {
                    ctx.setStrategy(new NoDiscount());
                }
                return ctx.apply(1000);
            });
        }
        List<Future<Double>> results = es.invokeAll(tasks);
        for (Future<Double> f : results) assertNotNull(f.get());
        es.shutdownNow();
    }

    @Test
    @DisplayName("Aplicar múltiples descuentos porcentuales seguidos")
    void multiplePercentageApplications() {
        PercentageDiscount pd = new PercentageDiscount(10);
        double result = 1000;
        for (int i = 0; i < 5; i++) {
            result = pd.calculate(result);
        }
        assertTrue(result > 500 && result < 600);
    }

    @Test
    @DisplayName("VIPClientDiscount con threshold negativo")
    void vipWithNegativeThreshold() {
        VIPClientDiscount vip = new VIPClientDiscount(-1000, 50);
        assertEquals(50, vip.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount porcentaje negativo es incremento")
    void percentageNegativeIsIncrease() {
        PercentageDiscount pd = new PercentageDiscount(-10);
        assertEquals(110, pd.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("VIPClientDiscount rate negativa incrementa")
    void vipNegativeRateIncreases() {
        VIPClientDiscount vip = new VIPClientDiscount(100, -50);
        assertEquals(150, vip.calculate(100), 0.0001);
    }

    @Test
    @DisplayName("StrategyConsole.main ejecuta sin excepciones")
    void strategyConsoleMainExecutes() throws Exception {
        String input = "1000\n1\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        assertDoesNotThrow(() -> StrategyConsole.main(new String[]{}));
        String out = baos.toString();
        assertTrue(out.length() > 0);
    }

    @Test
    @DisplayName("Estrategia NoDiscount es singleton pattern potencial")
    void noDiscountSingletonPotential() {
        NoDiscount nd1 = new NoDiscount();
        NoDiscount nd2 = new NoDiscount();
        assertEquals(nd1.getDescription(), nd2.getDescription());
    }

    @Test
    @DisplayName("Múltiples descuentos sin cambiar estrategia son idempotentes")
    void multipleAppliesSameStrategyIdempotent() {
        PercentageDiscount pd = new PercentageDiscount(50);
        contexto.setStrategy(pd);
        double first = contexto.apply(1000);
        double second = contexto.apply(1000);
        assertEquals(first, second, 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount preserva precisión decimal")
    void percentagePreservesPrecision() {
        PercentageDiscount pd = new PercentageDiscount(33.333333);
        double result = pd.calculate(99.999999);
        assertTrue(result > 66 && result < 67);
    }

    @Test
    @DisplayName("VIPClientDiscount preserva precisión en threshold")
    void vipPreservesPrecisionThreshold() {
        VIPClientDiscount vip = new VIPClientDiscount(1234.5678, 25.5555);
        double result = vip.calculate(1234.5678);
        assertTrue(result > 900 && result < 950);
    }

    @Test
    @DisplayName("DiscountContext describe es inmutable entre applies")
    void contextDescribeImmutableBetweenApplies() {
        PercentageDiscount pd = new PercentageDiscount(25);
        contexto.setStrategy(pd);
        String desc1 = contexto.describe();
        contexto.apply(100);
        String desc2 = contexto.describe();
        assertEquals(desc1, desc2);
    }

    @Test
    @DisplayName("Cambiar strategy null a no null funciona")
    void changeStrategyFromNullToNotNull() {
        contexto.setStrategy(null);
        assertEquals(1000, contexto.apply(1000), 0.0001);
        
        contexto.setStrategy(new PercentageDiscount(50));
        assertEquals(500, contexto.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("Cambiar strategy no null a null funciona")
    void changeStrategyFromNotNullToNull() {
        contexto.setStrategy(new PercentageDiscount(50));
        assertEquals(500, contexto.apply(1000), 0.0001);
        
        contexto.setStrategy(null);
        assertEquals(1000, contexto.apply(1000), 0.0001);
    }

    @Test
    @DisplayName("100 aplicaciones diferentes de estrategias sin fallar")
    void hundredDifferentApplications() {
        for (int i = 1; i <= 100; i++) {
            contexto.setStrategy(new PercentageDiscount(i % 50));
            double result = contexto.apply(1000);
            assertTrue(result >= 0);
        }
    }

    @Test
    @DisplayName("VIPClientDiscount con threshold Double.MAX_VALUE")
    void vipWithMaxThreshold() {
        VIPClientDiscount vip = new VIPClientDiscount(Double.MAX_VALUE, 50);
        assertEquals(1000, vip.calculate(1000), 0.0001);
    }

    @Test
    @DisplayName("PercentageDiscount aplicado a Double.MAX_VALUE")
    void percentageWithDoubleMaxValue() {
        PercentageDiscount pd = new PercentageDiscount(50);
        double result = pd.calculate(Double.MAX_VALUE);
        assertTrue(result > 0);
    }

    @Test
    @DisplayName("NoDiscount con cualquier valor positivo mantiene")
    void noDiscountWithAnyPositiveValue() {
        NoDiscount nd = new NoDiscount();
        for (double val = 0.01; val <= 10000; val *= 10) {
            assertEquals(val, nd.calculate(val), 0.0001);
        }
    }

    @Test
    @DisplayName("PercentageDiscount descripción no contiene saltos de línea")
    void percentageDescriptionNoNewlines() {
        PercentageDiscount pd = new PercentageDiscount(15.5);
        assertFalse(pd.getDescription().contains("\n"));
    }

    @Test
    @DisplayName("VIPClientDiscount descripción no contiene saltos de línea")
    void vipDescriptionNoNewlines() {
        VIPClientDiscount vip = new VIPClientDiscount(5000, 20);
        assertFalse(vip.getDescription().contains("\n"));
    }

    @Test
    @DisplayName("Aplicar 1000 cambios de estrategia")
    void thousandStrategyChanges() {
        PercentageDiscount pd = new PercentageDiscount(10);
        NoDiscount nd = new NoDiscount();
        VIPClientDiscount vip = new VIPClientDiscount(500, 5);
        
        for (int i = 0; i < 1000; i++) {
            if (i % 3 == 0) contexto.setStrategy(pd);
            else if (i % 3 == 1) contexto.setStrategy(nd);
            else contexto.setStrategy(vip);
        }
        assertTrue(contexto.describe().length() > 0);
    }
}
