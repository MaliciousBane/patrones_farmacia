package patrones_farmacia.state.view;

import patrones_farmacia.state.controller.*;

public class StateConsole {

    public void run() {
        StateController controller = new StateController();
        controller.createOrder("ORD-001");
        controller.addProductToOrder("ORD-001", "Paracetamol");
        controller.processOrder("ORD-001"); 
        controller.processOrder("ORD-001"); 
    }

}
