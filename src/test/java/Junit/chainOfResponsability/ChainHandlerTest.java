package Junit.chainOfResponsability;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import patrones_farmacia.chainOfResponsability.controller.FinalizeSaleHandler;
import patrones_farmacia.chainOfResponsability.controller.PaymentValidationHandler;
import patrones_farmacia.chainOfResponsability.controller.StockValidationHandler;
import patrones_farmacia.chainOfResponsability.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.factoryMethod.controller.FCreator;

@DisplayName("Pruebas de la Cadena de Responsabilidad para Ventas")
class ChainHandlerTest {

    private FCreator creadorMedicamentos;
    private Handler cadenaValidacion;

    @BeforeEach
    void configurarCadenaDeManejadores() {
        creadorMedicamentos = new FCreator();
        
        Handler validacionStock = new StockValidationHandler(null);
        Handler validacionPago = new PaymentValidationHandler(null);
        Handler finalizacion = new FinalizeSaleHandler();
        
        validacionStock.setNext(validacionPago);
        validacionPago.setNext(finalizacion);
        
        cadenaValidacion = validacionStock;
    }

    @Test
    @DisplayName("Debe procesar exitosamente una venta cuando todos los manejadores aprueban")
    void debeProcesarVentaExitosamentePorCadenaCompleta() {
        Sale venta = new Sale("CHAIN001", "Juan Pérez");
        Medicine medicamento = creadorMedicamentos.createMedicine(
            FCreator.Type.GENERIC, 
            "Dolex", 
            "GSK", 
            3000
        );
        venta.addMedicine(medicamento);

        boolean resultado = cadenaValidacion.handle(venta);

        assertTrue(resultado, "La venta debe procesarse exitosamente a través de toda la cadena de validación");
    }

    @Test
    @DisplayName("Debe procesar venta con múltiples medicamentos correctamente")
    void debeProcesarVentaConVariosMedicamentos() {
        Sale venta = new Sale("CHAIN002", "María González");
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Paracetamol", "GenFarma", 2500));
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.BRAND, "Aspirina", "Bayer", 5000));

        boolean resultado = cadenaValidacion.handle(venta);

        assertTrue(resultado, "La venta con múltiples medicamentos debe procesarse correctamente");
    }

    @Test
    @DisplayName("Debe validar que la venta tenga un ID válido")
    void debeValidarVentaConIdValido() {
        Sale venta = new Sale("CHAIN003", "Carlos Ramírez");
        venta.addMedicine(creadorMedicamentos.createMedicine(FCreator.Type.GENERIC, "Ibuprofeno", "TecnoFarma", 4000));

        assertNotNull(venta.getId(), "El ID de la venta no debe ser nulo");
        assertTrue(venta.getId().startsWith("CHAIN"), "El ID debe tener el formato correcto");
    }
}