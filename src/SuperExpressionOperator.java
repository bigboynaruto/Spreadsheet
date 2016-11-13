/**
 * Created by sakura on 10/27/16.
 */
class SuperExpressionOperator {
    private String operator;
    private int priority;
    private boolean associative;

    SuperExpressionOperator(String operator, int priority, boolean associative) {
        this.operator = operator;
        this.priority = priority;
        this.associative = associative;
    }

    public String toString() {
        return operator;
    }

    int getPriority() {
        return priority;
    }

    boolean isAssociative() {
        return associative;
    }
}
