/**
 * Created by sakura on 11/4/16.
 */
public class SuperCellNotSelectedException extends Exception {
    public SuperCellNotSelectedException() {
        super("Сначала нужно выбрать ячеечку");
    }

    public SuperCellNotSelectedException(String mess) {
        super(mess);
    }
}
