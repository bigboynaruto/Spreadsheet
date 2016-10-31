/**
 * Created by sakura on 10/27/16.
 */
public class SuperExpressionOperator {
    String operator;
    int priority;
    boolean associative;

    public SuperExpressionOperator(String operator, int priority, boolean associative) {
        this.operator = operator;
        this.priority = priority;
        this.associative = associative;
    }

    public String toString() {
        return operator;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isAssociative() {
        return associative;
    }

    public int compareTo(SuperExpressionOperator eo) {
        return Integer.compare(priority, eo.priority);
    }
}
