package patrones_farmacia.state.model;

public class PayState implements StateOrder {

    @Override
    public void manage(Order order) {
        System.out.println("Pedido " + order.getId() + " pagado, pasa a ENTREGADO.");
        order.setState(new DeliveredState());
    }

    @Override
    public String getNameState() { return "PAGADO"; }
}