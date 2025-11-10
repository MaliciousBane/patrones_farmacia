package patrones_farmacia.state.model;

public class PendientState implements StateOrder {

    @Override
    public void manage(Order order) {
        System.out.println("Pedido " + order.getId() + " confirmado, pasa a estado PAGADO.");
        order.setState(new PayState());
    }

    @Override
    public String getNameState() { return "PENDIENTE"; }
}