package Junit.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.mockito.Mockito.*;

import patrones_farmacia.observer.model.*;

@DisplayName("Pruebas del Patrón Observer para Inventario")
class InventorySubjectTest {

    private InventorySubject sujetoInventario;
    private FarmaObserver observadorEmail;
    private static final int UMBRAL_STOCK_BAJO = 5;

    @BeforeEach
    void configurarSujetoYObservador() {
        sujetoInventario = new InventorySubject(UMBRAL_STOCK_BAJO);
        observadorEmail = mock(FarmaObserver.class);
        sujetoInventario.addObserver(observadorEmail);
        sujetoInventario.addProduct("Paracetamol", 10);
    }

    @Test
    @DisplayName("Debe notificar al observador cuando el stock cae por debajo del umbral")
    void debeNotificarCuandoStockEstaBajo() {
        int cantidadBaja = 3;
        
        sujetoInventario.addProduct("Paracetamol", cantidadBaja);
        
        verify(observadorEmail, times(1)).update("Paracetamol", cantidadBaja);
    }

    @Test
    @DisplayName("No debe notificar al observador cuando el stock está por encima del umbral")
    void noDebeNotificarCuandoStockEsNormal() {
        int cantidadNormal = 8;
        
        sujetoInventario.addProduct("Paracetamol", cantidadNormal);
        
        verify(observadorEmail, never()).update("Paracetamol", cantidadNormal);
    }

    @Test
    @DisplayName("Debe notificar cuando el stock es exactamente igual al umbral")
    void debeNotificarCuandoStockEsIgualAlUmbral() {
        sujetoInventario.addProduct("Ibuprofeno", 10);
        sujetoInventario.addProduct("Ibuprofeno", UMBRAL_STOCK_BAJO);
        
        verify(observadorEmail, times(1)).update("Ibuprofeno", UMBRAL_STOCK_BAJO);
    }

    @Test
    @DisplayName("Debe notificar múltiples veces si el stock baja repetidamente")
    void debeNotificarMultiplesVecesSiStockBajaRepetidamente() {
        sujetoInventario.addProduct("Amoxicilina", 10);
        sujetoInventario.addProduct("Amoxicilina", 4);
        sujetoInventario.addProduct("Amoxicilina", 2);
        sujetoInventario.addProduct("Amoxicilina", 1);
        
        verify(observadorEmail, times(3)).update(eq("Amoxicilina"), anyInt());
    }

    @Test
    @DisplayName("Debe notificar a múltiples observadores cuando el stock está bajo")
    void debeNotificarAMultiplesObservadores() {
        FarmaObserver observadorSMS = mock(FarmaObserver.class);
        sujetoInventario.addObserver(observadorSMS);
        
        sujetoInventario.addProduct("Aspirina", 10);
        sujetoInventario.addProduct("Aspirina", 3);
        
        verify(observadorEmail, times(1)).update("Aspirina", 3);
        verify(observadorSMS, times(1)).update("Aspirina", 3);
    }

    @Test
    @DisplayName("No debe notificar después de remover al observador")
    void noDebeNotificarDespuesDeRemoverObservador() {
        sujetoInventario.removeObserver(observadorEmail);
        
        sujetoInventario.addProduct("Dolex", 10);
        sujetoInventario.addProduct("Dolex", 2);
        
        verify(observadorEmail, never()).update(anyString(), anyInt());
    }
}