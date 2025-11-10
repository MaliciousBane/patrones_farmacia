package patrones_farmacia.state.model;

public class DeliveredState implements StateOrder {

    @Override
    public void manage(Order order) {
        System.out.println("Pedido " + order.getId() + " ya fue entregado al cliente.");
    }

    @Override
    public String getNameState() { return "ENTREGADO"; }
}