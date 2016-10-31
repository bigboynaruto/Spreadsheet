import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Created by sakura on 10/27/16.
 */
public class SuperLexer extends SuperProcessingStrategy {
    static boolean DBG = false;
    static boolean GUI = true;

    public static String[] tokenize(String expr) {
        String cell = GUI ? expr.split("\n")[1] : "";
        expr = expr.split("\n")[0];
        expr.trim();
        expr += " ";
        ArrayList<String> tokens = new ArrayList<String>();
        Stack<String> brackets = new Stack<String>();
        String fixedExpr = "", currToken = "";
        boolean lastIsOperator = true;
        int len = expr.length(), i = 0;

        while (i < len) {
            char currChar = expr.charAt(i);

            if (!isValidIdentifierChar(currChar) && !currToken.equals("") && !isOperator(currToken + currChar)) {
                boolean currIsOperator = isOperator(currToken);
                if (currIsOperator && lastIsOperator) {
                    fixedExpr += "0";
                    tokens.add("0");
                } else if (!currIsOperator && !lastIsOperator) {
                    fixedExpr += "*";
                    tokens.add("*");
                }
                fixedExpr += currToken;
                tokens.add(currToken);
                currToken = "";
                lastIsOperator = currIsOperator;
            }

            currToken += currChar;
            char nextChar = expr.charAt(i + 1);
//            char nextChar = i + 1 < len ? expr.charAt(i + 1) : '1';
            if (lastIsOperator && (currChar == '+' || currChar == '-') && isValidIdentifierChar(nextChar)) {
//                currToken += currChar;
            } else if (isOpenBracket(currToken)) {
                if (!lastIsOperator) {
                    fixedExpr += "*";
                    tokens.add("*");
                }
                brackets.push(currToken);
                fixedExpr += currToken;
                tokens.add(currToken);
                currToken = "";
                lastIsOperator = true;
            } else if (isCloseBracket(currToken)) {
                if (lastIsOperator) {
                    fixedExpr += "0";
                    tokens.add("0");
                }

                while (!brackets.empty() && !brackets.peek().equals(getReverseBracket(currToken))) {
                    fixedExpr += getReverseBracket(brackets.peek());
                    tokens.add(getReverseBracket(brackets.pop()));
                }
                if (!brackets.empty()) {
                    brackets.pop();
//                    fixedExpr = getReverseBracket(currToken) + fixedExpr;
                } else {
                    fixedExpr = getReverseBracket(currToken) + fixedExpr;
                    tokens.add(0, getReverseBracket(currToken));
                }

                fixedExpr += currToken;
                tokens.add(currToken);
                currToken = "";
                lastIsOperator = false;
            } else if (isOperator(currToken) && !isOperator(currToken + nextChar)) {
                if (lastIsOperator){
                    fixedExpr += "0";
                    tokens.add("0");
                }
                fixedExpr += currToken;
                tokens.add(currToken);
                currToken = "";
                lastIsOperator = true;
            }

            // skip whitespaces
            if (++i < len && Character.isWhitespace(expr.charAt(i))) {
                if (!currToken.equals("")) {
                    boolean currIsOperator = isOperator(currToken);
                    if (currIsOperator && lastIsOperator){
                        fixedExpr += "0";
                        tokens.add("0");
                    } else if (!currIsOperator && !lastIsOperator) {
                        fixedExpr += "*";
                        tokens.add("*");
                    }
                    fixedExpr += currToken;
                    tokens.add(currToken);
                    currToken = "";
                    lastIsOperator = currIsOperator;
                }
                while (i < len && Character.isWhitespace(expr.charAt(i))) {
                    fixedExpr += expr.charAt(i++);
                }
            }
//            fixedExpr += currChar;
        }

        if (!currToken.equals("")) {
            fixedExpr += currToken;
            tokens.add(currToken);
        }

        if (lastIsOperator) {
            tokens.add("0");
            fixedExpr += "0";
        }

        while (!brackets.empty()) {
            fixedExpr += getReverseBracket(brackets.peek());
            tokens.add(getReverseBracket(brackets.pop()));
        }


        fixedExpr = fixedExpr.replace(" ", "");
        /*if (GUI && !fixedExpr.equals(expr.replace(" ", ""))) {
//            JOptionPane.showMessageDialog(null, fixedExpr);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибочка...");
            alert.setHeaderText("Кажется, вы что-то не так написали. Но я исправил!");
            alert.setContentText(fixedExpr);
            alert.initStyle(StageStyle.UNDECORATED);

            alert.showAndWait();
        }*/

        if (DBG && false) {
            System.out.println("\t\"" + fixedExpr + "\"");
            System.out.println("\t" + Arrays.toString(tokens.toArray()));
        }

        /*try {
            throw new Exception(cell);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        SuperCell.setCellExpression(cell, fixedExpr);
        return tokens.toArray(new String[tokens.size()]);
    }
}
