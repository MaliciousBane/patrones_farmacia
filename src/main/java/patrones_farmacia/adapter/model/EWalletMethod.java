package patrones_farmacia.adapter.model;

public class EWalletMethod {

    private String accountNumber;
    private double balance;

    public EWalletMethod(String accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public boolean transferCash(double amount) {
        if (balance >= amount) {
            balance -= amount;
            System.out.println("Transferencia desde E-Wallet exitosa.");
            return true;
        }
        System.out.println("Saldo insuficiente en billetera digital.");
        return false;
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
}