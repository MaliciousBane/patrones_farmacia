package patrones_farmacia.command.controller;

import patrones_farmacia.command.model.Command;
import java.util.ArrayList;
import java.util.List;

public class CashierInvoker {

    private List<Command> history = new ArrayList<>();

    public void executeCommand(Command c) {
        c.execute();
        history.add(c);
    }

    public void undoLast() {
        if (!history.isEmpty()) {
            Command last = history.remove(history.size() - 1);
            last.undo();
        } else {
            System.out.println("No hay operaciones para deshacer.");
        }
    }

    public void showHistory() {
        System.out.println("Historial de operaciones ejecutadas: " + history.size());
    }
}