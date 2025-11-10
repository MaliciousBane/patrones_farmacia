package patrones_farmacia.command.controller;

import patrones_farmacia.command.model.Command;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.factoryMethod.model.Medicine;
import patrones_farmacia.facade.model.Sale;

public class ReturnProductCommand implements Command {

    private SaleReceiver receiver;
    private String saleId;
    private Medicine product;

    public ReturnProductCommand(SaleReceiver receiver, String saleId, Medicine product) {
        this.receiver = receiver;
        this.saleId = saleId;
        this.product = product;
    }

    @Override
    public void execute() {
        receiver.returnProduct(saleId, product);
    }

    @Override
    public void undo() {
        System.out.println("No se puede deshacer una devolución de producto.");
    }
}