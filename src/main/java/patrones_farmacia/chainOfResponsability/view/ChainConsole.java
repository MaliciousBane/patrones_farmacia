package patrones_farmacia.chainOfResponsability.view;

import patrones_farmacia.chainOfResponsability.controller.*;
import patrones_farmacia.adapter.model.*;
import patrones_farmacia.facade.model.Sale;
import patrones_farmacia.factoryMethod.controller.FCreator;
import patrones_farmacia.factoryMethod.model.*;
import java.util.*;

public class ChainConsole {

    public void run() {
        FCreator creator = new FCreator();
        Medicine med = creator.createMedicine(FCreator.Type.BRAND, "Aspirina", "Bayer", 3000);
        List<Medicine> inventory = new ArrayList<>();
        inventory.add(med);

        CashMethod cash = new CashMethod(20000);
        CreditCardMethod card = new CreditCardMethod("0000", "Andrés", "555", 50000);
        EWalletMethod wallet = new EWalletMethod("ACC-1", 10000);
        AdapterPayMethod adapter = new AdapterPayMethod(cash, card, wallet);
        adapter.setMode("CASH");

        Sale sale = new Sale("S001", "Cliente 1");
        sale.addMedicine(med);

        StockValidationHandler stock = new StockValidationHandler(inventory);
        PaymentValidationHandler pay = new PaymentValidationHandler(adapter);
        FinalizeSaleHandler finalize = new FinalizeSaleHandler();
        stock.setNext(pay);
        pay.setNext(finalize);

        stock.handle(sale);
    }

    public static void main(String[] args) {
        new ChainConsole().run();
    }
}