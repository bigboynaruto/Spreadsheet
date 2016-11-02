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
    private static final HashMap<String, HashSet<String>> links = new HashMap<String, HashSet<String>>();
    private static ArrayList<ObservableList<SpreadsheetCell>> rows;
    private static HashMap<String, String> expressions = new HashMap<String, String>();

    private static boolean saved = false;
    private static String movingCell = null;
    private static String movingExpr = null;

    static {
        rows = new ArrayList<ObservableList<SpreadsheetCell>>();
    }

    public static void addRow(ObservableList<SpreadsheetCell> data) {
        saved = false;
        rows.add(data);
    }

    public static void rowRemoved(int row) {
        if (row >= rows.size() || row < 0) return;

        for (String c : expressions.keySet()) {
            if (getCellRow(c) == row)
                expressions.remove(c);
        }

        for (int r = row; r < rows.size(); r++) {
            for (int c = 0; c < rows.get(r).size(); c++) {
                if (!expressions.containsKey(getCellName(r + 1, c)))
                    continue;
                setCellExpression(getCellName(r, c), getCellExpression(getCellName(r + 1, c)));
                expressions.remove(getCellName(r + 1, c));
            }
        }

        for (String c : expressions.keySet()) {
            updateCells(c);
        }
    }

    public static void removeColumn(int column) {
        if (column >= rows.size() || column < 0) return;

//        rows.remove(row);
    }

    public static void addCell(int row, SpreadsheetCell data) {
        saved = false;
        rows.get(row).add(data);
    }

    public static void setItem(int row, int column, String data) {
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
//        if (i > -1) return "1";//////////////////////////////////////////////////////////////
        int row = Integer.parseInt(rowStr), col = 0;

        i = 1;
        while (!colStr.isEmpty()) {
            col += (colStr.charAt(colStr.length() - 1) - 'A') * i;
            i *= 'Z' - 'A' + 1;
            colStr = colStr.substring(0, colStr.length() - 1);
        }

        return rows.get(--row).get(col).getText().equals("") ? "" : rows.get(row).get(col).getText();
    }

    public static void setCellExpression(String c, String expr) {
        saved = false;
        expressions.put(c, expr);
    }

    public static String getCellExpression(String c) {
        if (!expressions.containsKey(c))
            return "0";

        return expressions.get(c);
    }

    public static void updateCells(String cell) {
        saved = false;
        for (String c : links.keySet()) {
            if (c.equals(cell) || !links.get(c).contains(cell))
                continue;

            String expr = expressions.get(c);

            try {
                setItem(getCellRow(c), getCellColumn(c), SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(expr)),SuperCell.getCellName(getCellRow(c), getCellColumn(c))).toString());
            } catch (/*SuperLoopException | SuperInvalidCharacterException | */Exception e) {
                e.printStackTrace();
                expressions.remove(c);
                setItem(getCellRow(c), getCellColumn(c), "");
            }
        }
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
                fileWriter.write(getCellRow(c) + " " + getCellColumn(c) + " " + getCellExpression(c) + "\n");
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
        movingCell = cell;
        movingExpr = getCellExpression(cell);
    }

    public static void pasteCell(String cell) {
        setCellExpression(cell, expressions.get(cell));
        movingCell = null;
        movingExpr = null;
    }
}
