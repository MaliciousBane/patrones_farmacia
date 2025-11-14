package patrones_farmacia.state.model;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private String id;
    private StateOrder state;
    private List<String> products;

    public Order(String id) {
        this.id = id;
        this.state = new PendientState();
        this.products = new ArrayList<>();
    }

    public void addProduct(String name) {
        products.add(name);
    }

    public void process() {
        state.manage(this);
    }

    public void setState(StateOrder s) { 
        this.state = s; 
    }
    
    public String getStateName() { 
        return state.getNameState(); 
    }
    
    public String getId() { 
        return id; 
    }
    
    public List<String> getProducts() {
        return new ArrayList<>(products);
    }
}