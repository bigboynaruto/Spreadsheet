/**
 * Created by sakura on 10/27/16.
 */
class SuperExpressionOperator {
    private String operator;
    private int priority;
    private boolean associative;
    SuperBinaryFunction<SuperBigInteger> func;

    public SuperExpressionOperator(String operator, int priority, boolean associative, SuperBinaryFunction<SuperBigInteger> func) {
        this.operator = operator;
        this.priority = priority;
        this.associative = associative;
        this.func = func;
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

    public SuperBigInteger apply(SuperBigInteger arg1, SuperBigInteger arg2) {
        return func.call(arg1, arg2);
    }
}
