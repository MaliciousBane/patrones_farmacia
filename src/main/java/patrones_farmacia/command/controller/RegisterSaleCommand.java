package patrones_farmacia.command.controller;

import patrones_farmacia.command.model.Command;
import patrones_farmacia.command.model.SaleReceiver;
import patrones_farmacia.facade.model.Sale;

public class RegisterSaleCommand implements Command {

    private SaleReceiver receiver;
    private Sale sale;

    public RegisterSaleCommand(SaleReceiver receiver, Sale sale) {
        this.receiver = receiver;
        this.sale = sale;
    }

    @Override
    public void execute() {
        receiver.registerSale(sale);
    }

    @Override
    public void undo() {
        receiver.cancelSale(sale.getId());
    }
}