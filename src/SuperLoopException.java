/**
 * Created by sakura on 10/30/16.
 */
public class SuperLoopException extends Exception {
    public SuperLoopException(String c1, String c2) {
        super("Ой, а у вас тут зацикленность образовалась: " + c1 + " " + c2);
    }
}
