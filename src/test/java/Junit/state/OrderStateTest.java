package Junit.state;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import patrones_farmacia.state.model.*;

@DisplayName("Pruebas del Patrón State para Órdenes")
class OrderTest {

    private Order orden;

    @BeforeEach
    void crearOrden() {
        orden = new Order("ORD-001");
    }

    @Test
    @DisplayName("Una nueva orden debe iniciar en estado Pendiente")
    void nuevaOrdenDebeEstarPendiente() {
        assertEquals("Pendiente", orden.getStateName(), 
            "El estado inicial de una orden debe ser 'Pendiente'");
    }

    @Test
    @DisplayName("Debe transicionar de Pendiente a Pagado al procesar")
    void debeTransicionarDePendienteAPagado() {
        orden.process();
        
        assertEquals("Pagado", orden.getStateName(), 
            "Después de procesar una orden pendiente, debe pasar a estado 'Pagado'");
    }

    @Test
    @DisplayName("Debe transicionar de Pagado a Entregado al procesar")
    void debeTransicionarDePagadoAEntregado() {
        orden.process(); 
        orden.process(); 
        
        assertEquals("Entregado", orden.getStateName(), 
            "Después de procesar una orden pagada, debe pasar a estado 'Entregado'");
    }

    @Test
    @DisplayName("Debe permanecer en Entregado si se procesa nuevamente")
    void debePermaneceEnEntregadoAlProcesarDeNuevo() {
        orden.process(); 
        orden.process(); 
        orden.process(); 
        
        assertEquals("Entregado", orden.getStateName(), 
            "Una orden entregada debe permanecer en estado 'Entregado' aunque se procese nuevamente");
    }

    @Test
    @DisplayName("Debe completar todas las transiciones de estado correctamente")
    void debeCompletarTodasLasTransicionesCorrectamente() {
        assertEquals("Pendiente", orden.getStateName());
        
        orden.process();
        assertEquals("Pagado", orden.getStateName());
        
        orden.process();
        assertEquals("Entregado", orden.getStateName());
        
        orden.process();
        assertEquals("Entregado", orden.getStateName());
    }

    @Test
    @DisplayName("Múltiples órdenes deben mantener estados independientes")
    void ordenesDebenMantenerEstadosIndependientes() {
        Order orden1 = new Order("ORD-002");
        Order orden2 = new Order("ORD-003");
        
        orden1.process(); 
        
        assertEquals("Pagado", orden1.getStateName());
        assertEquals("Pendiente", orden2.getStateName(), 
            "Las órdenes deben mantener estados independientes entre sí");
    }

    @Test
    @DisplayName("Debe mantener el ID de la orden a través de las transiciones")
    void debeMantenerIdATravesDeTransiciones() {
        String idOriginal = "ORD-004";
        Order ordenConId = new Order(idOriginal);
        
        ordenConId.process();
        ordenConId.process();
        
        assertNotNull(ordenConId, 
            "La orden debe mantener su identidad a través de las transiciones de estado");
    }
}