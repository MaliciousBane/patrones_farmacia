package patrones_farmacia.state.controller;

import patrones_farmacia.state.model.Order;
import patrones_farmacia.state.model.StateOrder;
import java.util.ArrayList;
import java.util.List;

public class StateController {

    private List<Order> orders;

    public StateController() {
        this.orders = new ArrayList<>();
    }

    public Order createOrder(String id) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("ID inválido");
        Order order = new Order(id);
        orders.add(order);
        System.out.println("Pedido creado: " + id + " (estado: " + order.getStateName() + ")");
        return order;
    }

    public boolean addProductToOrder(String orderId, String productName) {
        Order ord = findOrder(orderId);
        if (ord == null) return false;
        ord.addProduct(productName);
        System.out.println("Producto '" + productName + "' añadido al pedido " + orderId);
        return true;
    }

    public boolean processOrder(String orderId) {
        Order ord = findOrder(orderId);
        if (ord == null) return false;
        System.out.println("Procesando pedido " + orderId + " (estado actual: " + ord.getStateName() + ")");
        ord.process(); 
        System.out.println("Nuevo estado: " + ord.getStateName());
        return true;
    }

    public boolean setOrderState(String orderId, StateOrder state) {
        Order ord = findOrder(orderId);
        if (ord == null) return false;
        ord.setState(state);
        System.out.println("Estado forzado de " + orderId + " a " + ord.getStateName());
        return true;
    }

    public List<String> listOrdersSummary() {
        List<String> summary = new ArrayList<>();
        for (Order o : orders) {
            summary.add("ID:" + o.getId() + " | Estado:" + o.getStateName());
        }
        return summary;
    }

    public Order findOrder(String orderId) {
        for (Order o : orders) {
            if (o.getId().equalsIgnoreCase(orderId)) return o;
        }
        return null;
    }
}