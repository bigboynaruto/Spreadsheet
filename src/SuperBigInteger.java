import java.math.BigInteger;

/**
 * Created by sakura on 10/28/16.
 */
public class SuperBigInteger extends BigInteger {
    public static final SuperBigInteger PINF = new SuperBigInteger("1");
    public static final SuperBigInteger NINF = new SuperBigInteger("-1");
    public static final SuperBigInteger NAN = new SuperBigInteger("0");

    public SuperBigInteger(String val) {
        super(val);
    }

    public SuperBigInteger(long n) {
        super(String.valueOf(n));
    }

    public SuperBigInteger(BigInteger n) {
        super(n.toString());
    }

    public static SuperBigInteger tryParse(String val) {
        try {
            return parse(val);
        } catch (NumberFormatException e) {
            return NAN;
        }
    }

    public static SuperBigInteger parse(String val) {
        val.trim();
        if (val.equals(""))
            return new SuperBigInteger("0");
        if (val.equalsIgnoreCase("NAN"))
            return NAN;
        if (val.equals("-∞") || val.equalsIgnoreCase("-inf"))
            return NINF;
        if (val.equals("∞") || val.equals("+∞") ||
                val.equalsIgnoreCase("INF") || val.equalsIgnoreCase("+INF"))
            return PINF;
        if (val.equalsIgnoreCase("ANIME"))
            return new SuperBigInteger(42);
        if (val.equalsIgnoreCase("HUI"))
            return new SuperBigInteger(23);

        return new SuperBigInteger(val);
    }

    public boolean isFinite() {
        return this != PINF && this != NINF;
    }

    public boolean isNaN() {
        return this == NAN;
    }

    public boolean sign() {
        if (isNaN() || this == PINF || compareTo(BigInteger.valueOf(0)) >= 0)
            return false;

        return true;
    }

    public SuperBigInteger getSignedInf(boolean b) {
        return b ? NINF : PINF;
    }

    @Override
    public SuperBigInteger negate() {
        if (isNaN())
            return NAN;

        if (this == PINF)
            return NINF;
        if (this == NINF)
            return PINF;

        return new SuperBigInteger(super.negate());
    }

    public SuperBigInteger add(SuperBigInteger n) {
        if (this.isNaN() || n.isNaN())
            return NAN;

        if (!this.isFinite() && !n.isFinite()) {
            if (sign() != n.sign())
                return NAN;

            return this;
        }

        if (!isFinite())
            return this;
        if(!n.isFinite())
            return n;

        return new SuperBigInteger(super.add(n));
    }

    public SuperBigInteger substract(SuperBigInteger n) {
        return add(n.negate());
    }

    public SuperBigInteger multiply(SuperBigInteger n) {
        if (this.isNaN() || n.isNaN())
            return NAN;

        if (!this.isFinite() && !n.isFinite()) {
            if (sign() != n.sign())
                return NINF;

            return PINF;
        }

        if (!isFinite() || !n.isFinite())
            return sign() == n.sign() ? PINF : NINF;

        return new SuperBigInteger(super.multiply(n));
    }

    public SuperBigInteger divide(SuperBigInteger n) {
        if (this.isNaN() || n.isNaN())
            return NAN;
        if (n.equals(0))
            return equals(0) ? NAN : getSignedInf(sign());

        if (!isFinite() && !n.isFinite())
            return NAN;

        if (!isFinite())
                return sign() != n.sign() ? NINF : PINF;

        if (!n.isFinite())
            return new SuperBigInteger(0);

        if (!isFinite() || !n.isFinite())
            return sign() == n.sign() ? PINF : NINF;

        return new SuperBigInteger(super.multiply(n));
    }

    public SuperBigInteger mod(SuperBigInteger n) {
        if (this.isNaN() || n.isNaN())
            return NAN;
        if (n.equals(0))
            return getSignedInf(sign());
        if (!isFinite())
            return this;
        if (!n.isFinite())
            return n;

        return new SuperBigInteger(super.mod(n));
    }

    public SuperBigInteger pow(SuperBigInteger n) {
        if (this.isNaN() || n.isNaN())
            return NAN;
        if (!isFinite() && !n.isFinite()) {
            if (sign() != n.sign())
                return NAN;
            if (sign())
                return new SuperBigInteger(0);

            return PINF;
        }

        if (!isFinite()) {
            if (n.sign())
                return new SuperBigInteger(0);
            if (n.equals(0) || sign())
                return NAN;

            return PINF;
        } else if (!n.isFinite()) {
            if (equals(1))
                return new SuperBigInteger(1);

            if (n.sign()) {
                if (equals(0))
                    return PINF;
                else
                    return new SuperBigInteger(0);
            }

            if (equals(0))
                return new SuperBigInteger(this);

            return sign() ? NAN : PINF;
        }

        return new SuperBigInteger(super.pow(n.intValue()));
    }

    public SuperBigInteger shiftLeft(SuperBigInteger n) {
        if (isNaN() || n.isNaN() || sign() || n.sign())
            return NAN;

        if (!n.isFinite() || !isFinite())
            return PINF;

        return new SuperBigInteger(super.shiftLeft(n.intValue()));
    }

    public SuperBigInteger shiftRight(SuperBigInteger n) {
        if (isNaN() || n.isNaN())
            return NAN;
        if (sign() || n.sign())
            return NAN;
        if (!n.isFinite() && !isFinite())
            return NAN;

        if (!isFinite())
            return PINF;
        if (!n.isFinite())
            return new SuperBigInteger(0);

        return new SuperBigInteger(super.shiftRight(n.intValue()));
    }

    public boolean isGreater(SuperBigInteger n) {
        /*if (isNaN() || n.isNaN())
            return false;*/
        return compareTo(n) > 0;
    }

    public boolean isGreaterEq(SuperBigInteger n) {
        return equals(n) || isGreater(n);
    }

    public boolean isSmaller(SuperBigInteger n) {
        /*if (isNaN() || n.isNaN())
            return false;*/
        return compareTo(n) < 0;
    }

    public boolean isSmallerEq(SuperBigInteger n) {
        return equals(n) || isSmaller(n);
    }

    public int compareTo(SuperBigInteger n) {
        if (!isFinite()) {
            if (n.isFinite())
                return sign() ? -1 : 1;
        }
        if (!n.isFinite()) {
            if (isFinite())
                return n.sign() ? 1 : -1;
        }
        return super.compareTo(n);
    }

    public boolean equals(SuperBigInteger n) {
        if (isNaN() || n.isNaN())
            return false;
        if (!isFinite() || !n.isFinite())
            return sign() == n.sign();

        return super.equals(n);
    }

    public boolean equals(int n) {
        return equals(new SuperBigInteger(n));
    }

    @Override
    public String toString() {
        if (isNaN())
            return "NaN";
        if (!isFinite())
            return (sign() ? "-" : "+") + "∞";
        return super.toString();
    }
}
