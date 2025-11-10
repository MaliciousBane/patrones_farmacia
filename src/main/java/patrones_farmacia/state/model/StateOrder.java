package patrones_farmacia.state.model;

import patrones_farmacia.state.model.Order;

public interface StateOrder {
    void manage(Order order);
    String getNameState();
}