import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by sakura on 10/30/16.
 */
public class SuperCell {
    static final HashMap<String, HashSet<String>> links = new HashMap<String, HashSet<String>>();
    static ArrayList<ObservableList<SpreadsheetCell>> rows;
    static HashMap<String, String> expressions = new HashMap<String, String>();

    public static boolean hasLink(int r1, int c1, int r2, int c2) {
        return hasLink(getCellName(r1, c1), getCellName(r2, c2));
    }

    public static void add(ObservableList<SpreadsheetCell> data) {
        rows.add(data);
    }

    public static void add(int row, SpreadsheetCell data) {
        rows.get(row).add(data);
    }

    public static void setItem(int row, int column, String data) {
        rows.get(row).get(column).setItem(data);
        SuperCell.updateCells(getCellName(row, column));
    }

    public static void initRows(ArrayList<ObservableList<SpreadsheetCell>> list) {
        rows = list;
    }

    public static ArrayList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    public static String getCellValue(String c) {
        String rowStr, colStr;
        int i = 0;

        while (Character.isLetter(c.charAt(i++)))
            ;
        colStr = c.substring(0, i - 1);
        rowStr = c.substring(i - 1);
//        System.out.println(i + " : " + rowStr + " " + colStr);
//        if (i > -1) return "1";//////////////////////////////////////////////////////////////
        int row = Integer.parseInt(rowStr), col = 0;

        i = 1;
        while (!colStr.isEmpty()) {
            col += (colStr.charAt(colStr.length() - 1) - 'A') * i;
            i *= 'Z' - 'A' + 1;
            colStr = colStr.substring(0, colStr.length() - 1);
        }

//        System.out.println(i + " : " + (row - 1) + " " + col);

        return rows.get(--row).get(col).getText().equals("") ? "" : rows.get(row).get(col).getText();
    }

    public static void setCellExpression(String c, String expr) {
//        System.out.println("SETCELLEXPR: " + expr);
        /*try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        expressions.put(c, expr);
    }

    public static String getCellExpression(String c) {
        if (!expressions.containsKey(c))
            return "0";

        return expressions.get(c);
    }

    public static void updateCells(String cell) {
        for (String c : links.keySet()) {
            if (c.equals(cell) || !links.get(c).contains(cell))
                continue;

//            System.out.println("updateCell: " + c + " " + cell);
            String expr = expressions.get(c);
            String rowStr, colStr;
            int i = 0;

            while (Character.isLetter(c.charAt(i++)))
                ;
            colStr = c.substring(0, i - 1);
            rowStr = c.substring(i - 1);
//            System.out.println(i + " : " + rowStr + " " + colStr);
    //        if (i > -1) return "1";//////////////////////////////////////////////////////////////
            int row = Integer.parseInt(rowStr), col = 0;

            i = 1;
            while (!colStr.isEmpty()) {
                col += (colStr.charAt(colStr.length() - 1) - 'A') * i;
                i *= 'Z' - 'A' + 1;
                colStr = colStr.substring(0, colStr.length() - 1);
            }

//            System.out.println(i + " : " + (row - 1) + " " + col);

            try {
//                System.out.println(cell + " : " + getCellValue(cell));
//                System.out.println("EXPR: " + expr);
//                System.out.println(SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(expr + "\n" + SuperCell.getCellName(row - 1, col))), SuperCell.getCellName(row - 1, col)).toString());
                setItem(--row, col, SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(expr + "\n" + SuperCell.getCellName(row, col))), SuperCell.getCellName(row, col)).toString());
//                rows.get(--row).get(col).setItem(SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(expr + "\n" + SuperCell.getCellName(row, col))), SuperCell.getCellName(row, col)).toString());
//                System.out.println();
            } catch (SuperLoopException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean hasLink(String c1, String c2) {
        if (c1.equals(c2))
            return true;
        if (!links.containsKey(c1))
            return false;

        return links.get(c1).contains(c2);
    }

    public static void addLink(String c1, String c2) {
        if (!links.containsKey(c1)) {
            links.put(c1, new HashSet<String>());
        }

//        System.out.println("addLink: " + c1 + " " + c2);

        /*try {
            throw new Exception();
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        links.get(c1).add(c2);
//        printLinks();
    }

    public static void removeLink(String c1, String c2) {
        if (!links.containsKey(c1))
            return;

        links.get(c1).remove(c2);
    }

    public static String getCellName(int row, int column) {
        row++;
        String res = "";
        int aNum = 'Z' - 'A' + 1;
        while (column > 0) {
            res += (char)('A' + column % aNum);
            column /= aNum;
        }

//        System.out.println(row + " " + column + " " + ((res.equals("") ? "A" : res) + row));
        return (res.equals("") ? "A" : res) + row;
    }

    public static boolean isCellLink(String s) {
        return s.matches("[a-zA-Z]+[1-9]+[0-9]*");
    }

    public static void printLinks() {
        System.out.println("------------------------");
        for (String s : links.keySet()) {
            System.out.print(s + " : ");
            for (String ss : links.get(s))
                System.out.print(" " + ss);
            System.out.println();
        }
        System.out.println("------------------------");
    }
}
