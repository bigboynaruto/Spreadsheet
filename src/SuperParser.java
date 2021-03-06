import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by sakura on 10/27/16.
 */
public class SuperParser extends SuperProcessingStrategy {
    public static ArrayList<String> parse(String[] tokens) {
        Stack<String> operators = new Stack<>();
        ArrayList<String> res = new ArrayList<>();
        for (String s : tokens) {
            if (isOperator(s) && !isOpenBracket(s) && !isCloseBracket(s)) {
                int priority = getPriority(s);
                while (!operators.empty() && priority <= getPriority(operators.peek())) {
                    if (!getOperator(operators.peek()).isAssociative()
                            && priority == getPriority(operators.peek())) break;
                    res.add(operators.pop());
                }
                operators.push(s);
            }
            else if (isCloseBracket(s)) {
                String rBr = getReverseBracket(s);
                while (!operators.empty() && !operators.peek().equals(rBr))
                    res.add(operators.pop());
                if (!operators.empty())
                    operators.pop();
            } else if (isOpenBracket(s)) {
                operators.push(s);
            }else res.add(s);
        }

        while (!operators.empty())
            res.add(operators.pop());

        return res;
    }
}
