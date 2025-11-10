package patrones_farmacia.command.model;

public interface Command {
    void execute();
    void undo();
}