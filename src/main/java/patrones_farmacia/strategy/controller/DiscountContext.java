package patrones_farmacia.strategy.controller;

import patrones_farmacia.strategy.model.DiscountStrategy;

public class DiscountContext {

    private DiscountStrategy strategy;

    public void setStrategy(DiscountStrategy strategy) {
        this.strategy = strategy;
    }

    public double apply(double total) {
        if (strategy == null) return total;
        return strategy.calculate(total);
    }

    public String describe() {
        return (strategy != null) ? strategy.getDescription() : "Sin estrategia aplicada";
    }
}
