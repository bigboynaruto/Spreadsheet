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

        operators.add(new SuperExpressionOperator("+", 7, true, (a, b) -> a.add(b)));
        operators.add(new SuperExpressionOperator("-", 7, true, (a, b) -> a.substract(b)));
        operators.add(new SuperExpressionOperator("/", 8, true, (a, b) -> a.divide(b)));
        operators.add(new SuperExpressionOperator("*", 8, true, (a, b) -> a.multiply(b)));
        operators.add(new SuperExpressionOperator("%", 8, true, (a, b) -> a.mod(b)));
        operators.add(new SuperExpressionOperator("^", 9, false, (a, b) -> a.pow(b)));
        operators.add(new SuperExpressionOperator("&", 4, true, (a, b) -> a.and(b)));
        operators.add(new SuperExpressionOperator("|", 3, true, (a, b) -> a.or(b)));
        operators.add(new SuperExpressionOperator("<", 6, true,
                (a, b) -> new SuperBigInteger(a.isSmaller(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator("<=", 6, true,
                (a, b) -> new SuperBigInteger(a.isSmallerEq(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator(">", 6, true,
                (a, b) -> new SuperBigInteger(a.isGreater(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator(">=", 6, true,
                (a, b) -> new SuperBigInteger(a.isGreaterEq(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator("=", 5, true,
                (a, b) -> new SuperBigInteger(a.equals(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator("!=", 5, true,
                (a, b) -> new SuperBigInteger(!a.equals(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator("<>", 5, true,
                (a, b) -> new SuperBigInteger(!a.equals(b) ? 1 : 0)));
        operators.add(new SuperExpressionOperator("(", -2, false, null));
        operators.add(new SuperExpressionOperator("[", -2, false, null));
        operators.add(new SuperExpressionOperator("{", -2, false, null));
        operators.add(new SuperExpressionOperator(")", -2, false, null));
        operators.add(new SuperExpressionOperator("]", -2, false, null));
        operators.add(new SuperExpressionOperator("}", -2, false, null));
        operators.add(new SuperExpressionOperator(">>", 6, true, (a, b) -> a.shiftRight(b)));
        operators.add(new SuperExpressionOperator("<<", 6, true, (a, b) -> a.shiftLeft(b)));
    }

    static int getPriority(String val) {
        for (SuperExpressionOperator op : operators) {
            if (op.toString().equals(val)) return op.getPriority();
        }

        return 100;
    }

    static SuperExpressionOperator[] getOperators() {
        return operators.toArray(new SuperExpressionOperator[operators.size()]);
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
        return !Arrays.asList("+-/*%^&|<=>!({[)}] ".split("")).contains("" + c);
    }

    static boolean isValidCharacter(char c) {
        return !Arrays.asList("\\#$@:;\"'_`~?,.".split("")).contains("" + c);
    }
}
