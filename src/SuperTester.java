import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by sakura on 10/29/16.
 */
public class SuperTester {
    public static void main(String[] args) throws Exception {
        for (int row = 0; row < 30; row++) {
            for (int col = 0; col < 5; col++) {
                System.out.print(SuperCell.getCellColumn(SuperCell.getCellName(col, row)) + " ");
            }
            System.out.println();
        }

        for (int row = 0; row < 10000; row += 'Z' - 'A' + 1) {
            System.out.println(SuperCell.getCellColumn(SuperCell.getCellName(0, row)) + " " + row);
        }

        System.out.println(SuperCell.getCellColumn("AA1"));

//        System.out.println(Arrays.toString(SuperParser.parse("1 + 2 * 4".split(" ")).toArray()));
        /*System.out.println("---------LEXER TEST---------");
        testLexer("( 1 + 2 ) * 4");
        testLexer("( 1 + 2 )) * 4");
        testLexer("(( 1 + 2 ) * 4");
        testLexer("( 1 + 2 ) * (4");
        testLexer("( 1 + 2  * (4");
        testLexer("( 1+2 4");
        testLexer("( 1++(24");
        System.out.println("---------PARSER TEST---------");
        testParser("( 1 + 2 ) * 4");
        testParser("1 + ( 2 * 4 )");
        testParser("1 + 2 * 4");
        System.out.println("---------EVALUATOR TEST---------");
        testEvaluator("2 4 ^");
        testEvaluator("1 4 <<");
        testEvaluator("3 4 +");
        testEvaluator("1 inf -");
        testEvaluator("inf -inf +");
        testEvaluator("1 4 *");
        testEvaluator("NAN NaN *");
        testEvaluator("nan Nan +");
        testEvaluator("-1 -inf /");
        testEvaluator("inf -inf *");
        testEvaluator("-inf -inf *");
        testEvaluator("inf -inf /");
        System.out.println("---------ALL TEST---------");
        testAll("( 1 + 2 ) * 4");
        testAll("( 1 + 2 )) * 4");
        testAll("(( 1 + 2 ) * 4");
        testAll("( 1 + 2 ) * (4");
        testAll("( 1 + 2  * (4");
        testAll("( 1+2 4");
        testAll("( 1+2 4*");
        testAll("( 1+2 4(");
        testAll("1+2 4)");
        testAll("( 1+2 4");
        testAll("+1");
        testAll("+1+2");
        testAll("+1-5");
        testAll("+1--5");
        testAll("+1++7");
        testAll("+1*+5");
        testAll("1*-3");
        testAll("1+3");
        testAll("1+2-4+5");*/
    }

    static void testLexer(String args) throws Exception {
        System.out.print(args);
        System.out.println(" = " + String.join(" ", SuperLexer.tokenize(args)));
    }

    static void testEvaluator(String args) throws SuperLoopException {
        System.out.print(args);

        System.out.println(" = " + SuperEvaluator.evaluate(new ArrayList(Arrays.asList(args.split(" "))), ""));
    }

    static void testParser(String args) {
        System.out.print(args + " = ");
        ArrayList<String> s = SuperParser.parse(args.split(" "));
        System.out.println(String.join(" ", SuperParser.parse(args.split(" ")).toArray(new String[s.size()])));
    }

    static void testAll(String args) throws Exception {
        System.out.print(args);
        System.out.println(" = " + SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(args)), ""));
//        System.out.println(args);
//        SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(args)), "");
    }
}
