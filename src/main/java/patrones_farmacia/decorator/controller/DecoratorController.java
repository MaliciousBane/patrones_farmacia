package patrones_farmacia.decorator.controller;

import patrones_farmacia.decorator.model.Product;
import patrones_farmacia.decorator.model.BaseProduct;
import patrones_farmacia.decorator.model.DiscountDecorator;
import patrones_farmacia.decorator.model.TaxDecorator;

public class DecoratorController {

    public Product createBaseProduct(String name, double price) {
        validateName(name);
        validatePrice(price);
        return new BaseProduct(name, price);
    }

    public Product applyDiscount(Product product, double discountPercent) {
        if (product == null) throw new IllegalArgumentException("Producto no puede ser null.");
        if (discountPercent <= 0.0) return product; // nada que aplicar
        if (discountPercent >= 100.0) discountPercent = 100.0; // tope
        return new DiscountDecorator(product, discountPercent);
    }

    public Product applyTax(Product product, double taxPercent) {
        if (product == null) throw new IllegalArgumentException("Producto no puede ser null.");
        if (taxPercent <= 0.0) return product;
        return new TaxDecorator(product, taxPercent);
    }

    public Product applyDiscountThenTax(Product product, double discountPercent, double taxPercent) {
        Product afterDiscount = applyDiscount(product, discountPercent);
        return applyTax(afterDiscount, taxPercent);
    }

    public double calculateFinalPrice(Product product) {
        if (product == null) throw new IllegalArgumentException("Producto no puede ser null.");
        double price = product.getPrice();
        if (price < 0) throw new IllegalStateException("Precio calculado negativo.");
        return price;
    }

    public String describeProduct(Product product) {
        if (product == null) return "Producto no definido";
        return product.getDescription() + " | Precio final: $" + String.format("%.2f", product.getPrice());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("El nombre es obligatorio.");
    }

    private void validatePrice(double price) {
        if (price < 0.0) throw new IllegalArgumentException("El precio no puede ser negativo.");
    }
}