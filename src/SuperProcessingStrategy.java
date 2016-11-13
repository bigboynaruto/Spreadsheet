import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by sakura on 10/27/16.
 */
abstract class SuperProcessingStrategy {
    private static final String FILE_NAME = "data/operators.txt";
    private static HashSet<SuperExpressionOperator> operators = null;
    static {
        operators = new HashSet<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(" ");
                operators.add(new SuperExpressionOperator(tokens[0], Integer.parseInt(tokens[1]), tokens[2].equals("1")));
            }
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load file " + FILE_NAME + ". Terminating...");
            Platform.exit();
        } catch (IOException e) {
            System.out.println("Something wrong with IO. Terminating...");
            Platform.exit();
        }
    }

    static int getPriority(String val) {
        for (SuperExpressionOperator op : operators) {
            if (op.toString().equals(val)) return op.getPriority();
        }

        return 100;
    }

    static SuperExpressionOperator getOperator(String val) {
        for (SuperExpressionOperator op : operators) {
            if (op.toString().equals(val))
                return op;
        }

        return null;
    }

    static boolean isOperator(String val) {
        for (SuperExpressionOperator op : operators) {
            if (op.toString().equals(val)) return true;
        }

        return false;
    }

    static boolean isOpenBracket(String val) {
        return val.equals("(") || val.equals("[") || val.equals("{");
    }

    static boolean isCloseBracket(String val) {
        return val.equals(")") || val.equals("]") || val.equals("}");
    }

    static String getReverseBracket(String val) {
        switch (val) {
            case "(": return ")";
            case "[": return "]";
                case "{": return "}";
                case ")": return "(";
            case "]": return "[";
            case "}": return "{";
            default: return null;
        }
    }

    static boolean isValidIdentifierCharacter(char c) {
        return !Arrays.asList("+-/*%^&|<=>!({[)}] ".split("")).contains("" + c)/*c != '+' && c != '-' && c != '/' && c != '*'
                && c != '%' && c != '^' && c != '&' && c != '|'
                && c != '<' && c != '=' && c != '>' && c != '!'
                && c != '(' && c != '{' && c != '[' && c != ')'
                && c != '}' && c != ']' && c != ' '*/;
    }

    static boolean isValidCharacter(char c) {
        return !Arrays.asList("\\#$@:;\"'_`~?,.".split("")).contains("" + c);
    }
}
