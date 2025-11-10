package patrones_farmacia.command.controller;

import patrones_farmacia.command.model.Command;
import patrones_farmacia.command.model.SaleReceiver;

public class CancelSaleCommand implements Command {

    private SaleReceiver receiver;
    private String saleId;

    public CancelSaleCommand(SaleReceiver receiver, String saleId) {
        this.receiver = receiver;
        this.saleId = saleId;
    }

    @Override
    public void execute() {
        receiver.cancelSale(saleId);
    }

    @Override
    public void undo() {
        System.out.println("No se puede deshacer una cancelación de venta.");
    }
}