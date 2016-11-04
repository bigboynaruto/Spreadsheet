import java.math.BigInteger;
import java.util.ArrayList;
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
                SuperBigInteger b = SuperBigInteger.parse(val);
                return CONSTANT;
            } catch (NumberFormatException e) {}

            if (SuperCell.isCellLink(val))
                return LINK;

            return UNKNOWN;
        }
    }

    public static SuperBigInteger evaluate(ArrayList<String> rpn, String cell) throws SuperLoopException {
        Stack<String> operands = new Stack<String>();
//        System.out.println(String.join(" ", rpn.toArray(new String[rpn.size()])));
        if (rpn.size() == 1 && rpn.get(0).equals("0")) {
            Thread.dumpStack();
        }
        while (!rpn.isEmpty()) {
            String head = rpn.remove(0);
            OperandType ot = OperandType.getType(head);
            switch (ot) {
                case OPERATOR:
                    // if is binary
                    operands.push(processOperator(head, operands.pop(), operands.pop()).toString());
                    // else operands.push(processOperator(head, operands.pop());
                    break;
                case LINK:
                    // linkExists();
                    // isLoop();
                    // getCellValue();
                    // addLink();
                    if (SuperCell.hasLink(head, cell)) {
                        throw new SuperLoopException(head, cell);
                    }
                    SuperCell.addLink(cell, head);
//                    System.out.println(SuperCell.getCellValue(head));
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

    private static BigInteger processOperator(String o, String strO2, String strO1) {
        o.trim();
        SuperBigInteger o1 = SuperBigInteger.tryParse(strO1), o2 = SuperBigInteger.tryParse(strO2);
        switch (o) {
            case "+":
            case "add":
                return o1.add(o2);
            case "-":
            case "sub":
                return o1.substract(o2);
            case "*":
            case "mul":
                return o1.multiply(o2);
            case "/":
            case "div":
                return o1.divide(o2);
            case "%":
            case "mod":
                return o1.mod(o2);
            case "^":
            case "pow":
                return o1.pow(o2);
            case "&":
            case "and":
                return o1.and(o2);
            case "|":
            case "or":
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
            case "eqv":
                return new SuperBigInteger(o1.equals(o2) ? 1 : 0);
            case "!=":
            case "<>":
            case "neq":
                return new SuperBigInteger(!o1.equals(o2) ? 1 : 0);
            case ">>":
            case "shr":
                return o1.shiftRight(o2);
            case "<<":
            case "shl":
                return o1.shiftLeft(o2);
            default:
                return SuperBigInteger.NAN;
        }
    }
}
