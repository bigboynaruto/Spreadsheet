/**
 * Created by sakura on 11/1/16.
 */
public class SuperInvalidCharacterException extends Exception {
    SuperInvalidCharacterException(char c, int pos) {
        super("Ой, а у вас тут символ какой-то странный: " + c + ":" + pos);
    }
}
