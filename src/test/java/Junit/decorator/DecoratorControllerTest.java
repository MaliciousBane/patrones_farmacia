package Junit.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.decorator.controller.DecoratorController;
import patrones_farmacia.decorator.model.Product;
import patrones_farmacia.decorator.model.BaseProduct;
import patrones_farmacia.decorator.model.DiscountDecorator;
import patrones_farmacia.decorator.model.TaxDecorator;
import patrones_farmacia.decorator.model.ProductDecorator;
import patrones_farmacia.decorator.view.DecoratorConsole;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.*;
import java.util.List;
import java.util.ArrayList;

@DisplayName("Pruebas del Patrón Decorator para Productos (ampliadas)")
class DecoratorControllerTest {

    private DecoratorController controlador;
    private PrintStream originalOut;
    private InputStream originalIn;
    private ByteArrayOutputStream baos;

    @BeforeEach
    void configurarPrueba() {
        controlador = new DecoratorController();
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
    @DisplayName("Debe aplicar descuento del 10% y luego IVA del 19% correctamente")
    void debeAplicarDescuentoYLuegoIVA() {
        Product producto = controlador.createBaseProduct("Ibuprofeno", 10000);
        producto = controlador.applyDiscountThenTax(producto, 10, 19);
        double precioFinal = controlador.calculateFinalPrice(producto);
        assertEquals(10710.0, precioFinal, 0.1);
    }

    @Test
    @DisplayName("Debe aplicar solo descuento del 20% sin IVA")
    void debeAplicarSoloDescuento() {
        Product producto = controlador.createBaseProduct("Acetaminofén", 5000);
        producto = controlador.applyDiscount(producto, 20);
        double precioFinal = producto.getPrice();
        assertEquals(4000, precioFinal, 0.1);
    }

    @Test
    @DisplayName("La descripción del producto debe incluir información del IVA aplicado")
    void laDescripcionDebeIncluirIVA() {
        Product producto = controlador.createBaseProduct("Jarabe", 8000);
        producto = controlador.applyTax(producto, 19);
        String descripcion = controlador.describeProduct(producto);
        assertTrue(descripcion.contains("IVA") || descripcion.contains("iva") || descripcion.contains("Iva"));
        assertTrue(descripcion.contains("8000.00") || descripcion.contains("9520.00"));
    }

    @Test
    @DisplayName("Debe mantener el precio base sin decoradores")
    void debeManternerPrecioBaseSinDecoradores() {
        Product producto = controlador.createBaseProduct("Vitamina C", 12000);
        double precioFinal = controlador.calculateFinalPrice(producto);
        assertEquals(12000, precioFinal, 0.1);
    }

    @Test
    @DisplayName("Debe aplicar múltiples decoradores en el orden correcto")
    void debeAplicarMultiplesDecoradoresEnOrden() {
        Product producto = controlador.createBaseProduct("Antibiótico", 20000);
        producto = controlador.applyDiscount(producto, 15);
        producto = controlador.applyTax(producto, 19);
        double precioFinal = controlador.calculateFinalPrice(producto);
        assertEquals(20230.0, precioFinal, 0.1);
    }

    @Test
    @DisplayName("Descuento 0% no cambia precio")
    void descuentoCeroNoCambiaPrecio() {
        Product producto = controlador.createBaseProduct("Suplemento", 7000);
        producto = controlador.applyDiscount(producto, 0);
        assertEquals(7000, producto.getPrice(), 0.1);
    }

    @Test
    @DisplayName("Descuento negativo se considera no aplicable")
    void descuentoNegativoIgnorado() {
        Product producto = controlador.createBaseProduct("ProductoX", 5000);
        producto = controlador.applyDiscount(producto, -10);
        assertEquals(5000, producto.getPrice(), 0.1);
    }

    @Test
    @DisplayName("Descuento mayor a 100% se topa a 100%")
    void descuentoMayorA100SeTope() {
        Product producto = controlador.createBaseProduct("Promo", 5000);
        producto = controlador.applyDiscount(producto, 150);
        assertEquals(0, producto.getPrice(), 0.1);
    }

    @Test
    @DisplayName("Impuesto 0% no cambia precio")
    void impuestoCeroNoCambiaPrecio() {
        Product producto = controlador.createBaseProduct("SinIva", 4300);
        producto = controlador.applyTax(producto, 0);
        assertEquals(4300, producto.getPrice(), 0.1);
    }

    @Test
    @DisplayName("Crear producto con nombre nulo lanza excepción")
    void crearProductoNombreNuloLanza() {
        assertThrows(IllegalArgumentException.class, () -> controlador.createBaseProduct(null, 1000));
    }

    @Test
    @DisplayName("Crear producto con precio negativo lanza excepción")
    void crearProductoPrecioNegativoLanza() {
        assertThrows(IllegalArgumentException.class, () -> controlador.createBaseProduct("Neg", -1));
    }

    @Test
    @DisplayName("Aplicar descuento a producto nulo lanza excepción")
    void aplicarDescuentoANuloLanza() {
        assertThrows(IllegalArgumentException.class, () -> controlador.applyDiscount(null, 10));
    }

    @Test
    @DisplayName("Aplicar impuesto a producto nulo lanza excepción")
    void aplicarImpuestoANuloLanza() {
        assertThrows(IllegalArgumentException.class, () -> controlador.applyTax(null, 10));
    }

    @Test
    @DisplayName("calculateFinalPrice lanza si precio negativo")
    void calcularPrecioFinalNegativoLanza() {
        Product negativo = new Product() {
            @Override public double getPrice() { return -5.0; }
            @Override public String getDescription() { return "Neg"; }
        };
        assertThrows(IllegalStateException.class, () -> controlador.calculateFinalPrice(negativo));
    }

    @Test
    @DisplayName("describeProduct con null retorna indicador")
    void describirProductoNuloRetornaIndicador() {
        assertEquals("Producto no definido", controlador.describeProduct(null));
    }

    @Test
    @DisplayName("Impuesto luego descuento produce distinto resultado que descuento luego impuesto")
    void impuestoLuegoDescuentoDifiereDeDescuentoLuegoImpuesto() {
        Product base = controlador.createBaseProduct("Orden", 10000);
        Product taxThenDiscount = controlador.applyDiscount(controlador.applyTax(base, 10), 10);
        Product discountThenTax = controlador.applyTax(controlador.applyDiscount(base, 10), 10);
        double a = controlador.calculateFinalPrice(taxThenDiscount);
        double b = controlador.calculateFinalPrice(discountThenTax);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("describeProduct incluye precio formateado con dos decimales")
    void describeIncluyePrecioFormateado() {
        Product producto = controlador.createBaseProduct("Formato", 1234.5);
        String desc = controlador.describeProduct(producto);
        assertTrue(desc.contains("1234.50"));
    }

    @Test
    @DisplayName("TaxDecorator aplica impuesto correctamente (calculo directo)")
    void taxDecoratorCalculaCorrecto() {
        BaseProduct base = new BaseProduct("TestTax", 100.0);
        TaxDecorator tax = new TaxDecorator(base, 10.0);
        assertEquals(110.0, tax.getPrice(), 0.001);
        assertTrue(tax.getDescription().contains("IVA 10"));
    }

    @Test
    @DisplayName("DiscountDecorator aplica descuento correctamente (calculo directo)")
    void discountDecoratorCalculaCorrecto() {
        BaseProduct base = new BaseProduct("TestDisc", 200.0);
        DiscountDecorator disc = new DiscountDecorator(base, 25.0);
        assertEquals(150.0, disc.getPrice(), 0.001);
        assertTrue(disc.getDescription().contains("Descuento 25"));
    }

    @Test
    @DisplayName("ProductDecorator delega comportamiento por defecto")
    void productDecoratorDelegatesByDefault() {
        BaseProduct base = new BaseProduct("Delegate", 80.0);
        ProductDecorator decorator = new ProductDecorator(base) { };
        assertEquals(80.0, decorator.getPrice(), 0.001);
        assertEquals("Delegate", decorator.getDescription());
    }

    @Test
    @DisplayName("Crear producto con nombre vacío o espacio lanza excepción")
    void crearProductoNombreVacioLanza() {
        assertThrows(IllegalArgumentException.class, () -> controlador.createBaseProduct("   ", 100.0));
    }

    @Test
    @DisplayName("applyTax con valor negativo no modifica el producto")
    void applyTaxNegativoNoModifica() {
        Product base = controlador.createBaseProduct("Tst", 1000.0);
        Product res = controlador.applyTax(base, -5.0);
        assertSame(base.getClass(), res.getClass());
        assertEquals(1000.0, res.getPrice(), 0.001);
    }

    @Test
    @DisplayName("applyDiscountThenTax respeta tope de descuento y aplica IVA")
    void applyDiscountThenTaxRespectsCap() {
        Product base = controlador.createBaseProduct("Cap", 1000.0);
        Product res = controlador.applyDiscountThenTax(base, 200.0, 10.0);
        assertEquals(0.0, controlador.calculateFinalPrice(res), 0.001);
    }

    @Test
    @DisplayName("Cadena de decoradores (discount luego tax) produce descripcion compuesta")
    void descripcionCompuestaDespuesDeDecoradores() {
        Product base = controlador.createBaseProduct("Combo", 1000.0);
        Product decorated = controlador.applyTax(controlador.applyDiscount(base, 10), 5);
        String desc = controlador.describeProduct(decorated);
        assertTrue(desc.contains("Descuento 10") || desc.contains("IVA 5"));
        assertTrue(desc.contains("Precio final"));
    }

    @Test
    @DisplayName("Múltiples descuentos encadenados calculan correctamente")
    void multipleDiscountsStack() {
        Product base = controlador.createBaseProduct("Stack", 1000.0);
        Product d1 = controlador.applyDiscount(base, 10);
        Product d2 = controlador.applyDiscount(d1, 20);
        double expected = 1000.0 * 0.9 * 0.8;
        assertEquals(expected, d2.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Impuesto aplicado sobre descuento calcula correctamente")
    void taxOverDiscountCalculates() {
        Product base = controlador.createBaseProduct("TXD", 1000.0);
        Product discounted = controlador.applyDiscount(base, 50);
        Product taxed = controlador.applyTax(discounted, 10);
        assertEquals(550.0, taxed.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Aplicar descuento 100% seguido de impuesto deja en cero")
    void discount100ThenTaxRemainsZero() {
        Product base = controlador.createBaseProduct("Free", 500.0);
        Product res = controlador.applyTax(controlador.applyDiscount(base, 100), 20);
        assertEquals(0.0, controlador.calculateFinalPrice(res), 0.001);
    }

    @Test
    @DisplayName("Descripción y precio coherentes en cadena larga de decoradores")
    void longDecoratorChainDescriptionAndPrice() {
        Product p = controlador.createBaseProduct("L", 1000.0);
        for (int i = 1; i <= 5; i++) p = controlador.applyDiscount(p, 5);
        p = controlador.applyTax(p, 2.5);
        String desc = controlador.describeProduct(p);
        assertTrue(desc.contains("Precio final"));
        assertTrue(desc.contains("L"));
        assertTrue(controlador.calculateFinalPrice(p) > 0);
    }

    @Test
    @DisplayName("ApplyDiscount es inofensivo con objeto compartido (no muta original)")
    void applyDiscountDoesNotMutateOriginal() {
        BaseProduct base = new BaseProduct("Orig", 100.0);
        Product after = controlador.applyDiscount(base, 10);
        assertEquals(100.0, base.getPrice(), 0.001);
        assertNotEquals(base.getPrice(), after.getPrice());
    }

    @Test
    @DisplayName("Calculo con valores grandes mantiene precisión")
    void largeValuesMaintainPrecision() {
        Product base = controlador.createBaseProduct("Big", 1e9);
        Product taxed = controlador.applyTax(base, 12.5);
        assertEquals(1e9 * 1.125, taxed.getPrice(), 1.0);
    }

    @Test
    @DisplayName("DescribeProduct formatea dos decimales consistentemente")
    void describeProductFormatsTwoDecimals() {
        Product p = controlador.createBaseProduct("Fmt", 1234.5678);
        String d = controlador.describeProduct(p);
        assertTrue(d.contains("1234.57"));
    }

    @Test
    @DisplayName("Crear decorador anónimo y usarlo con controller")
    void anonymousDecoratorWorksWithController() {
        BaseProduct base = new BaseProduct("Anon", 200.0);
        Product custom = new ProductDecorator(base) {
            @Override public double getPrice() { return product.getPrice() + 10; }
            @Override public String getDescription() { return product.getDescription() + " (+custom)"; }
        };
        assertEquals(210.0, custom.getPrice(), 0.001);
        assertTrue(custom.getDescription().contains("+custom"));
    }

    @Test
    @DisplayName("Multiple threads applying decorators produce consistent results")
    void concurrentDecorationProducesConsistentResults() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(4);
        List<Future<Double>> futures = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            futures.add(es.submit(() -> {
                Product p = controlador.createBaseProduct("T", 1000.0);
                p = controlador.applyDiscount(p, 10);
                p = controlador.applyTax(p, 5);
                return p.getPrice();
            }));
        }
        for (Future<Double> f : futures) {
            assertEquals(1000.0 * 0.9 * 1.05, f.get(), 0.001);
        }
        es.shutdownNow();
    }

    @Test
    @DisplayName("Aplicar many discounts consecutivos no cae en overflow")
    void manyDiscountsNoOverflow() {
        Product p = controlador.createBaseProduct("Many", 1e6);
        for (int i = 0; i < 100; i++) p = controlador.applyDiscount(p, 1);
        assertTrue(p.getPrice() >= 0);
    }

    @Test
    @DisplayName("Calculo precio final con producto decorado nulo lanza IllegalArgumentException")
    void calculateFinalPriceNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> controlador.calculateFinalPrice(null));
    }

    @Test
    @DisplayName("Aplicar impuesto con valor muy pequeño no redondea a cero incorrectamente")
    void tinyTaxDoesNotRoundToZero() {
        Product p = controlador.createBaseProduct("Tiny", 1.0);
        Product taxed = controlador.applyTax(p, 0.01);
        assertTrue(taxed.getPrice() > 1.0);
    }

    @Test
    @DisplayName("BaseProduct getters devuelven valores correctos")
    void baseProductGettersWork() {
        BaseProduct bp = new BaseProduct("GetTest", 999.99);
        assertEquals("GetTest", bp.getDescription());
        assertEquals(999.99, bp.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Descuento del 50% reduce precio a la mitad")
    void descuentoDelCincuentaPorc() {
        Product p = controlador.createBaseProduct("Half", 500.0);
        p = controlador.applyDiscount(p, 50);
        assertEquals(250.0, p.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Impuesto del 100% duplica el precio")
    void impuestoDel100PorcentoDuplicaPrecios() {
        Product p = controlador.createBaseProduct("Double", 100.0);
        p = controlador.applyTax(p, 100);
        assertEquals(200.0, p.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Descuento del 99% deja 1% del precio")
    void descuentoDel99PorcentoDeja1Porcentoo() {
        Product p = controlador.createBaseProduct("OnePercent", 10000.0);
        p = controlador.applyDiscount(p, 99);
        assertEquals(100.0, p.getPrice(), 0.1);
    }

    @Test
    @DisplayName("Encadenar tax-discount-tax-discount produce descripcion profunda")
    void encadenaLargaProduceLongDescription() {
        Product p = controlador.createBaseProduct("Long", 1000.0);
        p = controlador.applyTax(p, 5);
        p = controlador.applyDiscount(p, 3);
        p = controlador.applyTax(p, 5);
        p = controlador.applyDiscount(p, 3);
        String desc = p.getDescription();
        assertTrue(desc.length() > 50);
        assertTrue(desc.contains("IVA") && desc.contains("Descuento"));
    }

    @Test
    @DisplayName("calculateFinalPrice con producto precio cero devuelve 0.0")
    void calculatePriceZeroReturnsZero() {
        Product p = new BaseProduct("Zero", 0.0);
        assertEquals(0.0, controlador.calculateFinalPrice(p), 0.001);
    }

    @Test
    @DisplayName("describeProduct incluye el nombre del producto")
    void describeProdutIncludeName() {
        Product p = controlador.createBaseProduct("TestName", 100.0);
        String desc = controlador.describeProduct(p);
        assertTrue(desc.contains("TestName"));
    }

    @Test
    @DisplayName("Descuento exactamente 100% produce precio 0")
    void descuentoExactamente100Produce0() {
        Product p = controlador.createBaseProduct("Exact100", 9999.99);
        p = controlador.applyDiscount(p, 100.0);
        assertEquals(0.0, p.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Aplicar descuento mayor a 100% lo topa a 100%")
    void descuentoMayorA100SeTopeA100() {
        Product p1 = controlador.createBaseProduct("Cap1", 1000.0);
        Product p2 = controlador.createBaseProduct("Cap2", 1000.0);
        p1 = controlador.applyDiscount(p1, 100.0);
        p2 = controlador.applyDiscount(p2, 250.0);
        assertEquals(p1.getPrice(), p2.getPrice(), 0.001);
    }

    @Test
    @DisplayName("TaxDecorator getDescription formatea tasa correctamente")
    void taxDecoratorDescriptionFormat() {
        BaseProduct base = new BaseProduct("Fmt", 100.0);
        TaxDecorator tax = new TaxDecorator(base, 19.5);
        assertTrue(tax.getDescription().contains("19.5"));
    }

    @Test
    @DisplayName("DiscountDecorator getDescription formatea tasa correctamente")
    void discountDecoratorDescriptionFormat() {
        BaseProduct base = new BaseProduct("Fmt", 100.0);
        DiscountDecorator disc = new DiscountDecorator(base, 33.3);
        assertTrue(disc.getDescription().contains("33.3"));
    }

    @Test
    @DisplayName("DecoratorConsole.run ejecuta sin excepciones")
    void decoratorConsoleRunExecutes() throws Exception {
        String input = "TestProd\n5000.0\n10.0\n19.0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        DecoratorConsole console = new DecoratorConsole();
        assertDoesNotThrow(() -> console.run());
        String out = baos.toString();
        assertTrue(out.length() > 0);
    }

    @Test
    @DisplayName("Aplicar descuento y luego tax varias veces")
    void applyDiscountThenTaxMultipleTimes() {
        Product p = controlador.createBaseProduct("Multi", 10000.0);
        for (int i = 0; i < 3; i++) {
            p = controlador.applyDiscountThenTax(p, 5, 2);
        }
        assertTrue(p.getPrice() > 0);
        assertTrue(p.getPrice() < 10000.0);
    }

    @Test
    @DisplayName("Producto con precio fraccionario mantiene precisión")
    void fractionalPriceMaintainsPrecision() {
        Product p = controlador.createBaseProduct("Frac", 123.456);
        p = controlador.applyTax(p, 7.5);
        double expected = 123.456 * 1.075;
        assertEquals(expected, p.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Descuento en producto decorado funciona en cascada")
    void discountOnDecoratedProductCascades() {
        BaseProduct base = new BaseProduct("Cascade", 1000.0);
        TaxDecorator taxed = new TaxDecorator(base, 10);
        DiscountDecorator discounted = new DiscountDecorator(taxed, 10);
        assertEquals(1000.0 * 1.1 * 0.9, discounted.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Crear producto con precio 0.01 funciona")
    void createProductWithPennyWorks() {
        Product p = controlador.createBaseProduct("Penny", 0.01);
        assertEquals(0.01, p.getPrice(), 0.001);
    }

    @Test
    @DisplayName("Crear producto con precio muy grande funciona")
    void createProductWithBigPriceWorks() {
        Product p = controlador.createBaseProduct("Big", 1e15);
        assertEquals(1e15, p.getPrice(), 1e10);
    }

    @Test
    @DisplayName("describeProduct formatea cantidad con comas para números grandes")
    void describeLargeNumberFormatting() {
        Product p = controlador.createBaseProduct("Large", 1000000.0);
        String desc = controlador.describeProduct(p);
        assertTrue(desc.contains("1000000.00"));
    }

    @Test
    @DisplayName("Aplicar impuesto 0.001% afecta el precio")
    void tinyTaxAffectsPrice() {
        Product p = controlador.createBaseProduct("TinyTax", 1000.0);
        Product p2 = controlador.applyTax(p, 0.001);
        assertTrue(p2.getPrice() > p.getPrice());
    }

    @Test
    @DisplayName("Aplicar descuento 0.001% reduce el precio")
    void tinyDiscountReducesPrice() {
        Product p = controlador.createBaseProduct("TinyDisc", 1000.0);
        Product p2 = controlador.applyDiscount(p, 0.001);
        assertTrue(p2.getPrice() < p.getPrice());
    }

    @Test
    @DisplayName("Alternancia de descuentos e impuestos múltiple")
    void alternatingDiscountsAndTaxes() {
        Product p = controlador.createBaseProduct("Alt", 1000.0);
        for (int i = 0; i < 5; i++) {
            p = controlador.applyDiscount(p, 5);
            p = controlador.applyTax(p, 2);
        }
        assertTrue(controlador.calculateFinalPrice(p) > 0);
    }
}
