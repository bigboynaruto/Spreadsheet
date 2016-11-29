import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

/**
 * Created by sakura on 10/27/16.
 */
public class SuperEvaluator extends SuperProcessingStrategy {
    private enum OperandType {
        OPERATOR, LINK, CONSTANT, UNKNOWN;

        public static OperandType getType(String val) {
            if (isOperator(val))
                return OPERATOR;

            try {
                SuperBigInteger.parse(val);
                return CONSTANT;
            } catch (NumberFormatException e) {}

            if (SuperCell.isCellLink(val))
                return LINK;

            return UNKNOWN;
        }
    }

    public static SuperBigInteger evaluate(ArrayList<String> rpn, String cell) throws SuperLoopException {
        Stack<String> operands = new Stack<String>();
        HashSet<String> oldLinks = SuperCell.getLinks().get(cell);
        SuperCell.removeLinks(cell);
        while (!rpn.isEmpty()) {
            String head = rpn.remove(0);
            OperandType ot = OperandType.getType(head);
            switch (ot) {
                case OPERATOR:
                    operands.push(processOperator(getOperator(head),
                            SuperBigInteger.tryParse(operands.pop()),
                            SuperBigInteger.tryParse(operands.pop())).toString());
                    break;
                case LINK:
                    if (SuperCell.hasLink(head, cell)) {
                        if (oldLinks != null) {
                            for (String s : oldLinks)
                                SuperCell.addLink(cell, s);
                        }
                        throw new SuperLoopException(head, cell);
                    }
                    SuperCell.addLink(cell, head);
                    operands.push(SuperCell.getCellValue(head));
                    break;
                case CONSTANT:
                    operands.push(head);
                    break;
                case UNKNOWN:
                    operands.push(head);
                    break;
            }
        }

        return SuperBigInteger.tryParse(operands.empty() ? "0" : operands.pop());
    }

    private static BigInteger processOperator(SuperExpressionOperator operator, SuperBigInteger o2, SuperBigInteger o1) {
        if (operator == null)
            return SuperBigInteger.NAN;

        return operator.apply(o1, o2);
        /*o.trim();
        switch (o) {
            case "+":
            case "add":
                return o1.add(o2);
            case "-":
                return o1.substract(o2);
            case "*":
                return o1.multiply(o2);
            case "/":
                return o1.divide(o2);
            case "%":
                return o1.mod(o2);
            case "^":
                return o1.pow(o2);
            case "&":
                return o1.and(o2);
            case "|":
                return o1.or(o2);
            case ">":
                return new SuperBigInteger(o1.isGreater(o2) ? 1 : 0);
            case ">=":
                return new SuperBigInteger(o1.isGreaterEq(o2) ? 1 : 0);
            case "<":
                return new SuperBigInteger(o1.isSmaller(o2) ? 1 : 0);
            case "<=":
                return new SuperBigInteger(o1.isSmallerEq(o2) ? 1 : 0);
            case "=":
                return new SuperBigInteger(o1.equals(o2) ? 1 : 0);
            case "!=":
            case "<>":
                return new SuperBigInteger(!o1.equals(o2) ? 1 : 0);
            case ">>":
                return o1.shiftRight(o2);
            case "<<":
                return o1.shiftLeft(o2);
            default:
                return SuperBigInteger.NAN;
        }
                */
    }
}
