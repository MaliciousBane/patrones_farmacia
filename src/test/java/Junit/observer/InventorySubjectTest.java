package Junit.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;

import patrones_farmacia.observer.model.*;

class InventorySubjectTest {

    private InventorySubject subject;
    private FarmaObserver emailObs;

    @BeforeEach
    void setup() {
        subject = new InventorySubject(5);
        emailObs = mock(FarmaObserver.class);
        subject.addObserver(emailObs);
        subject.addProduct("Paracetamol", 10);
    }

    @Test
    void testObserverNotifiedWhenStockLow() {
        subject.addProduct("Paracetamol", 3);
        verify(emailObs, times(1)).update("Paracetamol", 3);
    }

    @Test
    void testObserverNotNotifiedWhenStockNormal() {
        subject.addProduct("Paracetamol", 8);
        verify(emailObs, never()).update("Paracetamol", 8);
    }
}
