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

            if (SuperCell.isCellRef(val))
                return LINK;

            return UNKNOWN;
        }
    }

    public static SuperBigInteger evaluate(ArrayList<String> rpn, String cell) throws SuperLoopException {
        Stack<String> operands = new Stack<String>();
        HashSet<String> oldLinks = SuperCell.getRefs().get(cell);
        SuperCell.removeRefs(cell);
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
                    if (SuperCell.hasRef(head, cell)) {
                        if (oldLinks != null) {
                            for (String s : oldLinks)
                                SuperCell.addRef(cell, s);
                        }
                        throw new SuperLoopException(head, cell);
                    }
                    SuperCell.addRef(cell, head);
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
    }
}
