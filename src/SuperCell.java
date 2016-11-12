import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.stage.StageStyle;
import org.controlsfx.control.spreadsheet.Grid;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Spreadsheet.
 * Created by sakura on 10/30/16.
 */
public class SuperCell {
    private static HashMap<String, HashSet<String>> links = new HashMap<>();
    private static ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>();
    private static HashMap<String, String> expressions = new HashMap<>();

    private static boolean saved = false;
    private static String movingExpr = null;

    static void addRow(ObservableList<SpreadsheetCell> data) {
        saved = false;
        rows.add(data);
    }

    private static void emptyCell(String cell) {
        if (expressions.containsKey(cell))
            expressions.remove(cell);

        removeLinks(cell);

        setItem(getCellRow(cell), getCellColumn(cell), "");
    }

    private static void emptyCell(int row, int col) {
        emptyCell(getCellName(row, col));
    }

    private static void updateAll() {

        new HashSet<>(expressions.keySet()).forEach(SuperCell::updateCells);

        new HashSet<>(expressions.keySet()).forEach(SuperCell::updateCells);
    }

    private static void addCell(int row, SpreadsheetCell data) {
        saved = false;
        rows.get(row).add(data);
    }

    private static void setItem(int row, int column, String data) {
        if (row >= rows.size() || column >= rows.get(row).size())
            return;
        saved = false;
        rows.get(row).get(column).setItem(data);
        SuperCell.updateCells(getCellName(row, column));
    }

    static void initRows(int size) {
        rows = new ArrayList<>(size);
    }

    private static void setRows(ArrayList<ObservableList<SpreadsheetCell>> list) {
        rows = list;
    }

    private static void setExpressions(HashMap<String, String> exprs) {
        expressions = exprs;
    }

    static ArrayList<ObservableList<SpreadsheetCell>> getRows() {
        return rows;
    }

    static HashMap<String, HashSet<String>> getLinks() {
        return links;
    }

    private static void setLinks(HashMap<String, HashSet<String>> newLinks) {
        links = newLinks;
    }

    static String getCellValue(String c) {
        int row = getCellRow(c);
        int col = getCellColumn(c);

        if (row >= rows.size() || col >= rows.get(row).size())
            return "";

        return rows.get(row).get(col).getText();
    }

    static void setCellExpression(String cell, String expr) throws SuperInvalidCharacterException, SuperLoopException {
        saved = false;
        if (expr == null || expr.trim().equals("")) {
            emptyCell(cell);
            return;
        }

        int row = getCellRow(cell), column = getCellColumn(cell);
        String[] tokens = SuperLexer.tokenize(expr);

        expressions.put(cell, String.join("", (CharSequence[]) tokens));
        setItem(row, column, SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
    }

    static String getCellExpression(String c) {
        if (!expressions.containsKey(c))
            return "";

        return expressions.get(c);
    }

    static String getCellExpression(int row, int col) {
        return getCellExpression(getCellName(row, col));
    }

    private static void updateCells(String cell) {
        saved = false;
        ArrayList<String> toRemove = new ArrayList<>();
        for (String c : new HashSet<>(links.keySet())) {
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

    static boolean hasLink(String c1, String c2) {
        if (c1.equals(c2))
            return true;
        if (!links.containsKey(c1))
            return false;

        for (String c : links.get(c1)) {
            if (hasLink(c, c2))
                return true;
        }

        return false;
    }

    static void addLink(String c1, String c2) {
        saved = false;
        if (!links.containsKey(c1)) {
            links.put(c1, new HashSet<>());
        }

        links.get(c1).add(c2);
    }

    static void removeLinks(String c) {
        if (links.containsKey(c))
            links.remove(c);
    }

    static boolean isCellLink(String s) {
        return s.matches("[a-zA-Z]+[1-9]+[0-9]*");
    }

    static String getCellName(int row, int column) {
        row++;
        String res = "";
        int aNum = 'Z' - 'A' + 1;
        column++;
        while (column > 0) {
            column--;
            res = (char)('A' + column % aNum) + res;
            column /= aNum;
        }

        return (res.equals("") ? "A" : res) + row;
    }

    static int getCellRow(String c) {
        String rowStr;
        int i = 0;

        while (Character.isLetter(c.charAt(i++)));
        rowStr = c.substring(i - 1);
        int row = Integer.parseInt(rowStr);

        return --row;
    }

    static int getCellColumn(String c) {
        String colStr;
        int i = 0;

        while (Character.isLetter(c.charAt(i++)))
            ;
        colStr = c.substring(0, i - 1);
        int col = 0;

        i = 1;
        while (!colStr.isEmpty()) {
            col += (colStr.charAt(colStr.length() - 1) - 'A' + 1) * i;
            i *= 'Z' - 'A' + 1;
            colStr = colStr.substring(0, colStr.length() - 1);
        }
        col--;

        return col;
    }

    static void readFromFile(SpreadsheetView spreadsheetView, String filename) {
        HashMap<String, String> newExprs = new HashMap<>();
        HashMap<String, String> styles = new HashMap<>();
        try (BufferedReader fileReader = new BufferedReader(new FileReader(new File(filename)))) {
            int maxRow = 0, maxCol = 0;
            String line;
            while ((line = fileReader.readLine()) != null) {
                String[] tok = line.split(" ");
                int row = Integer.parseInt(tok[0]);
                int column = Integer.parseInt(tok[1]);
                String cell = getCellName(row, column);
                String expression = String.join(" ", (CharSequence[]) Arrays.copyOfRange(tok, 2, tok.length));
                newExprs.put(cell, expression);

                // STYLE
                line = fileReader.readLine();
                styles.put(cell, line.trim());

                maxRow = maxRow > row ? maxRow : row;
                maxCol = maxCol > column ? maxCol : column;
            }

            int drow = maxRow - rows.size() < 0 ? 0 : maxRow - rows.size() + 1;
            int dcol= maxCol - rows.get(0).size() < 0 ? 0 : maxCol - rows.get(0).size() + 1;
            addRowCol(spreadsheetView, drow, dcol);


            expressions.clear();
            for (ObservableList<SpreadsheetCell> r : rows) {
                for (SpreadsheetCell c : r)
                    c.setItem("");
            }

            for (String cell : newExprs.keySet()) {
                try {
                    setCellExpression(cell, newExprs.get(cell));
                    if (!styles.get(cell).equals("none"))
                        rows.get(getCellRow(cell)).get(getCellColumn(cell)).setStyle(styles.get(cell));
                } catch (SuperLoopException ignored) {

                }
            }
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.setHeaderText("Не могу найти файл " + filename + ".");

            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.setHeaderText("Ошибочка при чтении файла " + filename + ".");

            alert.showAndWait();
        }
    }

    static void writeToFile(String filename) {
        try (FileWriter fileWriter = new FileWriter(filename)) {
            int r = rows.size();
            for (int i = 0; i < r; i++) {
                int c = rows.get(i).size();
                for (int j = 0; j < c; j++) {
                    if (expressions.containsKey(getCellName(i, j))
                            || rows.get(i).get(j).getStyle() != null
                            && !rows.get(i).get(j).getStyle().equals("")) {
                        String style = rows.get(i).get(j).getStyle();
                        fileWriter.write(i + " " + j + " " + getCellExpression(getCellName(i, j)) + "\n");
                        fileWriter.write(style == null || style.trim().equals("") ? "none" : style);
                        fileWriter.write("\n");
                    }
                }
            }
            saved = true;
        } catch (Exception e) {
            System.err.println("Ой, что-то не так с файлом... Попробуйте в другой раз.");
        }
    }

    static boolean isSaved() {
        return saved;
    }

    static void moveCell(int row, int col) {
        String cell = getCellName(row, col);
        movingExpr = getCellExpression(cell);
        emptyCell(cell);
    }

    static void pasteCell(int row, int col) throws SuperCellNotSelectedException, SuperInvalidCharacterException, SuperLoopException {
        String cell = getCellName(row, col);
        if (movingExpr == null)
            throw new SuperCellNotSelectedException("Сначала нужно что-то вырезать...");
        setCellExpression(cell, movingExpr);
        movingExpr = null;
    }

    static SpreadsheetCell getCell(String cell) {
        int row = getCellRow(cell);
        int col = getCellColumn(cell);

        return rows.get(row).get(col);
    }

    static void removeRow(SpreadsheetView table, int selectedRow, int selectedCol) {
        if (table == null || selectedRow < 0 || !table.isEditable()) {
            return;
        }

        Grid oldGrid = table.getGrid();
        ObservableList<ObservableList<SpreadsheetCell>> rowsOld = oldGrid.getRows();
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<>();
        int newRows = 0;

        if (selectedRow >= rowsOld.size() || selectedRow < 0) {
            return;
            // Can not remove the only row a table has. Cleans it.
        } else if (oldGrid.getRowCount() == 1) {
            for (int column = 0; column < oldGrid.getColumnCount(); column++) {
                SuperCell.emptyCell(0, column);
            }
            return;
        }

        SpreadsheetCell newElem;
        final HashMap<String, String> expressions = new HashMap<>();
        final HashMap<String, HashSet<String>> newLinks = new HashMap<>();
        // Loops throw each row not to be removed, adding them to a array that contains the old row set,
        // but without the removed row.
        for (int i = 0; i < rowsOld.size(); i++) {
            if (i == selectedRow) {
                continue;
            }

            // Copies a row, cell by cell, it's type and value
            final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
            for (int column = 0; column < oldGrid.getColumnCount(); ++column) {

                int rowSpan = rowsOld.get(i).get(column).getRowSpan();
                int columnSpan = rowsOld.get(i).get(column).getColumnSpan();

                //get the current cell value
                Object item = rowsOld.get(i).get(column).getItem();
                newElem = createEmptyCell(newRows, column, rowSpan, columnSpan);

                if (newElem != null) {
                    //Update the new cell with the original value
                    newElem.setItem(item);
                    String currCell = SuperCell.getCellName(i, column), nextCell = SuperCell.getCellName(i > selectedRow ? i - 1 : i, column);
//                    System.out.println(nextCell + "     " + SuperCell.getCellExpression(currCell));
                    String currExpression = SuperCell.getCellExpression(currCell);
                    if (!currExpression.equals("")) {
                        expressions.put(nextCell, SuperCell.getCellExpression(currCell));

                        try {
                            newLinks.put(nextCell, new HashSet<>());
                            String[] tokens = SuperLexer.tokenize(expressions.get(nextCell));
                            for (int k = 0; k < tokens.length; k++) {
                                if (SuperCell.isCellLink(tokens[k]) && SuperCell.getCellRow(tokens[k]) > selectedRow) {
                                    System.out.print(tokens[k] + "  ");
                                    tokens[k] = SuperCell.getCellName(SuperCell.getCellRow(tokens[k]) - 1, SuperCell.getCellColumn(tokens[k]));
                                    System.out.println(tokens[k]);
//                                    newLinks.get(nextCell).add(tokens[i]);
                                }
                                newLinks.get(nextCell).add(tokens[k]);
                            }
                            expressions.put(nextCell, String.join("", (CharSequence[]) tokens));
                        } catch (Exception e) {
                            System.out.println(">" + e.getMessage() + " " + e.getClass() + " " + e.getCause());
                            newLinks.put(nextCell, new HashSet<>());
                            expressions.remove(nextCell);
//                          newExpressions.put(c, "");
                        }

                        if (newLinks.get(nextCell).isEmpty())
                            newLinks.remove(nextCell);
                    }
                    list.add(newElem);
                }
            }

            // Adds a valid row to the set of remaining rows.
            rows.add(list);
            newRows++;
        }

        // Updates the grid.
        oldGrid.setRows(rows);
        SuperCell.setRows(rows);
        SuperCell.setLinks(newLinks);//////////////////////////////////////////////////////////////
        oldGrid.getColumnHeaders().addAll(oldGrid.getColumnHeaders());
        SuperCell.setExpressions(expressions);
        table.setGrid(oldGrid);
        SuperCell.updateAll();

        // Fixes the selection
        if (selectedCol >= 0 && selectedCol < table.getColumns().size()) {
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(selectedRow > 0 ? selectedRow - 1 : selectedRow, table.getColumns().get(selectedCol));
        }
    }

    static SpreadsheetCell createEmptyCell(int row, int column, int rowSpan, int colSpan) {
        return SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan, "");
    }

    static void addRowCol(SpreadsheetView spreadSheetView, int dRow, int dCol) {
        if (dRow < 0 || dCol < 0) return;

        Grid grid = spreadSheetView.getGrid();

        for (int row = 0; row < grid.getRowCount(); ++row) {
            for (int column = grid.getColumnCount(); column < grid.getColumnCount() + dCol; column++)
                SuperCell.addCell(row, SuperCell.createEmptyCell(row, column, 1, 1));
        }

        for (int row = grid.getRowCount(); row < grid.getRowCount() + dRow; row++) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount() + dCol; ++column) {
                dataRow.add(SuperCell.createEmptyCell(row, column, 1, 1));
            }
            SuperCell.addRow(dataRow);
        }
        grid.setRows(SuperCell.getRows());
        spreadSheetView.setGrid(grid);
        for (int i = spreadSheetView.getColumns().size(); i < spreadSheetView.getColumns().size() + dCol; i++) {
            spreadSheetView.getColumns().get(spreadSheetView.getColumns().size() - 1).setPrefWidth(90);
        }
    }
}
