package sample;

public class BankruptcyException extends Exception {
    Player shark;
    public BankruptcyException(Player shark) {
        super();
        this.shark = shark;
    }
}
