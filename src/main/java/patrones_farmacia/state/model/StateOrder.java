package patrones_farmacia.state.model;

public interface StateOrder {
    void manage(Order order);
    String getNameState();
}