import javafx.collections.ObservableList;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by sakura on 10/30/16.
 */
public class SuperCell {
    private static HashMap<String, HashSet<String>> links = new HashMap<String, HashSet<String>>();
    private static ArrayList<ObservableList<SpreadsheetCell>> rows;
    private static HashMap<String, String> expressions = new HashMap<String, String>();

    private static boolean saved = false;
    private static String movingExpr = null;

    static {
        rows = new ArrayList<ObservableList<SpreadsheetCell>>();
    }

    public static void addRow(ObservableList<SpreadsheetCell> data) {
        saved = false;
        rows.add(data);
    }

    public static void emptyCell(String cell) {
        if (expressions.containsKey(cell))
            expressions.remove(cell);

        removeLinks(cell);

        setItem(getCellRow(cell), getCellColumn(cell), "");
    }

    public static void emptyCell(int row, int col) {
        emptyCell(getCellName(row, col));
    }

    public static void updateAll() {

        for (String c : new HashSet<String>(expressions.keySet())) {
            updateCells(c);
        }

        for (String c : new HashSet<String>(expressions.keySet())) {
            updateCells(c);
        }
    }

    public static void addCell(int row, SpreadsheetCell data) {
        saved = false;
        rows.get(row).add(data);
    }

    public static void setItem(int row, int column, String data) {
        if (row >= rows.size() || column >= rows.get(row).size())
            return;
        saved = false;
        rows.get(row).get(column).setItem(data);
        SuperCell.updateCells(getCellName(row, column));
    }

    public static void setItem(String cell, String data) {
        saved = false;
        rows.get(getCellRow(cell)).get(getCellColumn(cell)).setItem(data);
        SuperCell.updateCells(cell);
    }

    public static void initRows(int size) {
        rows = new ArrayList<ObservableList<SpreadsheetCell>>(size);
    }

    public static void setRows(ArrayList<ObservableList<SpreadsheetCell>> list) {
        rows = list;
    }

    public static void setExpressions(HashMap<String, String> exprs) {
        expressions = exprs;
    }

    public static ArrayList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    public static HashMap<String, HashSet<String>> getLinks() {
        return links;
    }

    public static void setLinks(HashMap<String, HashSet<String>> newLinks) {
        links = newLinks;
    }

    public static String getCellValue(String c) {
        int row = getCellRow(c);
        int col = getCellColumn(c);

        if (row >= rows.size() || col >= rows.get(row).size())
            return "";

        return rows.get(row).get(col).getText();
    }

    public static void setCellExpression(String c, String expr) {
        saved = false;
        if (expr.trim().equals("")) {
            if (expressions.containsKey(c))
                expressions.remove(c);
            return;
        }
        expressions.put(c, expr);
    }

    public static String getCellExpression(String c) {
        if (!expressions.containsKey(c))
            return "";

        return expressions.get(c);
    }

    public static void updateCells(String cell) {
        saved = false;
        ArrayList<String> toRemove = new ArrayList<String>();
        for (String c : new HashSet<String>(links.keySet())) {
            if (c.equals(cell) || !links.get(c).contains(cell))
                continue;

            if (expressions.get(c) == null) {
                toRemove.add(c);
                continue;
            }

            String expr = expressions.get(c);

            try {
                if (expr == null || expr.trim().equals(""))
                    throw new Exception();
                setItem(getCellRow(c), getCellColumn(c), SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(expr)),SuperCell.getCellName(getCellRow(c), getCellColumn(c))).toString());
            } catch (/*SuperLoopException | SuperInvalidCharacterException | */Exception e) {
                e.printStackTrace();
                emptyCell(c);
            }
        }

        for (String c : toRemove)
            expressions.remove(c);
    }

    public static boolean hasLink(String c1, String c2) {
        if (c1.equals(c2))
            return true;
        if (!links.containsKey(c1))
            return false;

//        return links.get(c1).contains(c2);
        for (String c : links.get(c1)) {
            if (hasLink(c, c2))
                return true;
        }

        return false;
    }

    public static void addLink(String c1, String c2) {
        saved = false;
        if (!links.containsKey(c1)) {
            links.put(c1, new HashSet<String>());
        }

        links.get(c1).add(c2);
//        printLinks();
    }

    public static void removeLink(String c1, String c2) {
        if (!links.containsKey(c1))
            return;

        links.get(c1).remove(c2);
    }

    public static void removeLinks(String c) {
        if (links.containsKey(c))
            links.remove(c);
    }

    public static String getCellName(int row, int column) {
        row++;
        String res = "";
        int aNum = 'Z' - 'A' + 1;
        while (column > 0) {
            res += (char)('A' + column % aNum);
            column /= aNum;
        }

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

    public static int getCellRow(String c) {
        String rowStr;
        int i = 0;

        while (Character.isLetter(c.charAt(i++)))
            ;
        rowStr = c.substring(i - 1);
        int row = Integer.parseInt(rowStr);

        return --row;
    }

    public static int getCellColumn(String c) {
        String colStr;
        int i = 0;

        while (Character.isLetter(c.charAt(i++)))
            ;
        colStr = c.substring(0, i - 1);
        int col = 0;

        i = 1;
        while (!colStr.isEmpty()) {
            col += (colStr.charAt(colStr.length() - 1) - 'A') * i;
            i *= 'Z' - 'A' + 1;
            colStr = colStr.substring(0, colStr.length() - 1);
        }

        return col;
    }

    public static void readFromFile(String filename) {
        HashMap<String, String> newExprs = new HashMap<String, String>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(new File(filename)))) {
            String line = null;
            while ((line = fileReader.readLine()) != null) {
                String[] tok = line.split(" ");
                int row = Integer.parseInt(tok[0]);
                int column = Integer.parseInt(tok[1]);
                String expression = String.join("\t", Arrays.copyOfRange(tok, 2, tok.length));
                newExprs.put(getCellName(row, column), expression);
            }


            expressions.clear();
            for (ObservableList<SpreadsheetCell> r : rows) {
                for (SpreadsheetCell c : r)
                    c.setItem("");
            }

            for (String cell : newExprs.keySet()) {
                setCellExpression(cell, newExprs.get(cell));

                String[] tokens = SuperLexer.tokenize(newExprs.get(cell));
                setItem(getCellRow(cell), getCellColumn(cell), SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());

//                setItem(cell, newExprs.get(cell));
            }
        } catch (SuperLoopException e) {

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    public static void writeToFile(String filename) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            for(String c : expressions.keySet()) {
                int row = getCellRow(c), col = getCellColumn(c);
                fileWriter.write(row + " " + col + " " + getCellExpression(c) + "\n");
                fileWriter.write(rows.get(row).get(col).getStyle());
            }
            saved = true;
        } catch (Exception e) {
            System.err.println("Ой, что-то не так с файлом... Попробуйте в другой раз.");
        }
    }

    public static boolean isSaved() {
        return saved;
    }

    public static void moveCell(String cell) {
        movingExpr = getCellExpression(cell);
        emptyCell(cell);
    }

    public static void pasteCell(String cell) throws SuperCellNotSelectedException, SuperInvalidCharacterException, SuperLoopException {
        if (movingExpr == null)
            throw new SuperCellNotSelectedException("Сначала нужно что-то вырезать...");
        expressions.get(cell);
        String[] tokens = SuperLexer.tokenize(movingExpr);
        SuperCell.setItem(getCellRow(cell), getCellColumn(cell), SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
        setCellExpression(cell, movingExpr);
        movingExpr = null;
    }
}
