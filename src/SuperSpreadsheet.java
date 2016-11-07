import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.spreadsheet.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

/**
 * Created by sakura on 10/26/16.
 */
public class SuperSpreadsheet extends Application {
    GridBase grid;
    SpreadsheetView spreadSheetView;
    BorderPane borderPane;
    SuperCell sc;
    TextField tf;

    public static void main(String[] args) {
        launch(args);
    }

    public Node getPanel(Stage stage) {
        sc = new SuperCell();
        borderPane = new BorderPane();

        int rowCount = 9;
        int columnCount = 9;
        grid = new GridBase(rowCount, columnCount);
        normalGrid(grid);

        setUpSpreadsheetView(grid);
        tf = new TextField("Добро пожаловать");

        Button addr = new Button("Add row");
        addr.setStyle("-fx-background-color: null;");
        Button addc = new Button("Add col");
        addc.setStyle("-fx-background-color: null;");
        /*tf.setOnKeyPressed(keyEvent -> {
                if (!keyEvent.getCode().toString().equals("ENTER") || spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                    return;
                int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
                int column = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
                String oldExpr = SuperCell.getCellExpression(SuperCell.getCellName(row, column));
                String text = tf.getText();
                String cell = SuperCell.getCellName(row, column);
                text = text.replace(" ", "");
                try {
                    String prev = SuperCell.getCellValue(cell);
                    String[] tokens = SuperLexer.tokenize(text);
                    SuperCell.setCellExpression(cell, String.join("", tokens));
                    SuperCell.setItem(row, column, SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
                    for (String tok : oldExpr.split(" ")) {
                        if (SuperCell.isCellLink(tok)) {
                            SuperCell.removeLink(cell, tok);
                        }
                    }

                    if (!SuperCell.getCellExpression(cell).replace(" ", "").equals(text.replace(" ", ""))) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.UNDECORATED);
                        alert.setTitle("Ошибочка...");
                        alert.setHeaderText("Кажется, вы что-то не так написали. Но я исправил!");
                        alert.setContentText(SuperCell.getCellExpression(cell));

                        alert.showAndWait();
                    }
                    tf.setText(SuperCell.getCellExpression(cell));
//                    SuperCell.updateCells(SuperCell.getCellName(row, column));
                } catch (SuperLoopException | SuperInvalidCharacterException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                    SuperCell.setCellExpression(cell, oldExpr);
                }
        });*/
        tf.setOnAction(keyEvent -> {
            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int column = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            String oldExpr = SuperCell.getCellExpression(SuperCell.getCellName(row, column));
            String text = tf.getText();
            String cell = SuperCell.getCellName(row, column);
            text = text.replace(" ", "");
            try {
                String prev = SuperCell.getCellValue(cell);
                String[] tokens = SuperLexer.tokenize(text);
                SuperCell.setCellExpression(cell, String.join("", tokens));
                SuperCell.setItem(row, column, SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
                for (String tok : oldExpr.split(" ")) {
                    if (SuperCell.isCellLink(tok)) {
                        SuperCell.removeLink(cell, tok);
                    }
                }

                if (!SuperCell.getCellExpression(cell).replace(" ", "").equals(text.replace(" ", ""))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText("Кажется, вы что-то не так написали. Но я исправил!");
                    alert.setContentText(SuperCell.getCellExpression(cell));

                    alert.showAndWait();
                }
                tf.setText(SuperCell.getCellExpression(cell));
//                    SuperCell.updateCells(SuperCell.getCellName(row, column));
            } catch (SuperLoopException | SuperInvalidCharacterException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setTitle("Ошибочка...");
                alert.setHeaderText(e.getMessage());

                alert.showAndWait();
                SuperCell.setCellExpression(cell, oldExpr);
            }
        });
        addr.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                addRowCol(1, 0);
            }
        });
        addc.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                addRowCol(0, 1);
            }
        });

        final SuperFontChooser fontChooser = new SuperFontChooser();
        fontChooser.setOnAction(handler -> {
            spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
                SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                setStyleProperty(cell.styleProperty(), "-fx-font-size", fontChooser.getValue() + " ex");
            });
        });

        final ToggleButton boldButt = new ToggleButton("A");
        boldButt.setSelected(false);
        boldButt.setStyle("-fx-font-weight:bold;");
        boldButt.setTooltip(new Tooltip("text-bold"));
        final ToggleButton italicButt = new ToggleButton("A");
        italicButt.setSelected(false);
        italicButt.setStyle("-fx-font-style:italic;");
        italicButt.setTooltip(new Tooltip("text-italic"));
        final ToggleButton underButt = new ToggleButton("A");
        underButt.setSelected(false);
        underButt.setStyle("-fx-underline:true;");
        underButt.setTooltip(new Tooltip("text-underline"));

        boldButt.setOnAction(handler -> {
            spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
                SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                setStyleProperty(cell.styleProperty(), "-fx-font-weight", boldButt.isSelected() ? "bold" : "normal");
            });
        });
        italicButt.setOnAction(handler -> {
            spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
                SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                setStyleProperty(cell.styleProperty(), "-fx-font-style", italicButt.isSelected() ? "italic" : "normal");
            });
        });
        underButt.setOnAction(handler -> {
            spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
                SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                setStyleProperty(cell.styleProperty(), "-fx-underline", underButt.isSelected() ? "true" : "false");
            });
        });

        final TextField cellChooser = new TextField();
        cellChooser.setMaxWidth(100);
        cellChooser.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                String cell = cellChooser.getText();
                if (!SuperCell.isCellLink(cell))
                    return;

                int row = SuperCell.getCellRow(cell), col = SuperCell.getCellColumn(cell);
                if (col >= spreadSheetView.getColumns().size())
                    return;
                if (row >= SuperCell.getRows().size())
                    return;

                spreadSheetView.getSelectionModel().clearAndSelect(row, spreadSheetView.getColumns().get(col));
            }
        });

        HBox styleBox = new HBox(createColorPicker("-fx-background-color", Color.WHITE),
                createColorPicker("-fx-text-fill", Color.BLACK),
                fontChooser,
                boldButt, italicButt, underButt);
        HBox hbox = new HBox(cellChooser, tf, addr, addc);
        VBox vbox = new VBox(styleBox, hbox);
        hbox.setHgrow(tf, Priority.ALWAYS);
        borderPane.setTop(vbox);

        borderPane.setCenter(spreadSheetView);
//        spreadSheetView.getSelectionModel().select(0, spreadSheetView.getColumns().get(0));

//        borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane,"Both"));

        return borderPane;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Anime Spreadsheet");

        final Scene scene = new Scene(new VBox(), 1000, 600);

        scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                spreadSheetView.setPrefWidth(number2.doubleValue());
            }
        });
        scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                spreadSheetView.setPrefHeight(number2.doubleValue());
            }
        });

        MenuBar menuBar = new MenuBar();

        Menu menuFile = new Menu("Файл");
        Menu menuEdit = new Menu("Редактирование");
        Menu menuView = new Menu("Вид");

        MenuItem saveItem = new MenuItem("Сохранить");
        MenuItem openItem = new MenuItem("Открыть");
        MenuItem exitItem = new MenuItem("Выйти");

        saveItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        openItem.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        exitItem.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        saveItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.setContentText("Введите название файла");
                dialog.setHeaderText("Сохранить");
                Optional<String> res = dialog.showAndWait();
                if (res.isPresent()) {
                    try {
                        SuperCell.writeToFile(res.get());
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.UNDECORATED);
                        alert.setHeaderText(e.getMessage());

                        alert.showAndWait();
                    }
                }
            }
        });
        openItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.initStyle(StageStyle.UNDECORATED);
                dialog.setContentText("Введите название файла");
                dialog.setHeaderText("Открыть");
                Optional<String> res = dialog.showAndWait();
                if (res.isPresent()) {
                    try {
                        SuperCell.readFromFile(res.get());
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.UNDECORATED);
                        alert.setHeaderText(e.getMessage());

                        alert.showAndWait();
                    }
                }
            }
        });
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!SuperCell.isSaved()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Помощник <имя> на страже вашей безопасности!");
                    alert.setHeaderText("Ой, а вы не сохранили свой файлик... Точно хотите выйти?");
                    alert.setContentText("Подумайте дважды, перед тем, как соглашаться!");

                    alert.showAndWait();

                    if (alert.getResult().equals(ButtonType.OK)) {
                        Platform.exit();
                    }
                }
                else Platform.exit();
            }
        });
        menuFile.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);

        MenuItem addrItem = new MenuItem("Добавить строку");
        MenuItem addcItem = new MenuItem("Добавить колонку");
        MenuItem removerItem = new MenuItem("Удалить строку");

        addrItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        addcItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        removerItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        addrItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addRowCol(1, 0);
            }
        });
        addcItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                addRowCol(0, 1);
            }
        });
        removerItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                    return;
//                SuperCell.removeRow(spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow());
//                grid.setRows(SuperCell.getRows());
//                spreadSheetView.setGrid(grid);
                int row = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow();
                int col = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getColumn();
                removeRow(spreadSheetView, row, col);
            }
        });

        menuView.getItems().addAll(addrItem, addcItem, new SeparatorMenuItem(), removerItem);


        MenuItem cutItem = new MenuItem("Вырезать");
        MenuItem pasteItem = new MenuItem("Вставить вырезанное");
//        MenuItem bgItem = new MenuItem("Закраска");

        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));

        cutItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (spreadSheetView.getSelectionModel().getSelectedCells().size() < 1) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText("Выделите сначала ячейку...");

                    alert.showAndWait();
                    return;
                }
                int row = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow();
                int col = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getColumn();
                SuperCell.moveCell(SuperCell.getCellName(row, col));
            }
        });
        pasteItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (spreadSheetView.getSelectionModel().getSelectedCells().size() < 1) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText("Выделите сначала ячейку...");

                    alert.showAndWait();
                    return;
                }

                int row = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow();
                int col = spreadSheetView.getSelectionModel().getSelectedCells().get(0).getColumn();
                try {
                    SuperCell.pasteCell(SuperCell.getCellName(row, col));
                } catch (SuperCellNotSelectedException | SuperLoopException  e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                } catch (SuperInvalidCharacterException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                }

                tf.setText(SuperCell.getCellExpression(SuperCell.getCellName(row, col)));
            }
        });

        menuEdit.getItems().addAll(cutItem, pasteItem);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, getPanel(primaryStage));

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private ColorPicker createColorPicker(final String property, Color defaultColor) {
        final ColorPicker colpicker = new ColorPicker(defaultColor);
        colpicker.setTooltip(new Tooltip(property.replace("-fx-", "")));
        colpicker.setStyle("-fx-color-label-visible: false;-fx-background-color: null;");

        colpicker.setOnAction(actionEvent -> {
            Color color = colpicker.getValue();
            double r = color.getRed() * 256, g = color.getGreen() * 256, b = color.getBlue() * 256;

            for (TablePosition pos : spreadSheetView.getSelectionModel().getSelectedCells()) {
                SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                setStyleProperty(cell.styleProperty(), property, "rgb(" + (int)r + "," + (int)g + "," + (int)b + ")");
            }
        });

        return colpicker;
    }

    private static void setStyleProperty(StringProperty style, String property, String value) {
        String str = style == null ? "" : style.getValue();
        str = str == null ? "" : str;

        str = str.replaceAll(property + ":.+?;", "");
        str = str + property + ":" + value + ";";

        style.setValue(str);
    }

    private void normalGrid(GridBase grid) {
        SuperCell.initRows(grid.getRowCount());
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                dataRow.add(createEmptyCell(row, column, 1, 1));
            }
            SuperCell.addRow(dataRow);
        }
        grid.setRows(SuperCell.getRows());

        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, new EventHandler<GridChange>() {
            @Override
            public void handle(GridChange change) {
                String oldExpr = SuperCell.getCellExpression(SuperCell.getCellName(change.getRow(), change.getColumn()));
                String cell = SuperCell.getCellName(change.getRow(), change.getColumn());
                try {
                    if (change.getNewValue() != null && !((String)change.getNewValue()).replace(" ", "").equals("")) {
                        String[] tokens = SuperLexer.tokenize((String)change.getNewValue());
                        SuperCell.setCellExpression(cell, String.join("", tokens));
                        SuperCell.setItem(change.getRow(), change.getColumn(), SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
                    } else {
                        SuperCell.emptyCell(change.getRow(), change.getColumn());
//                        SuperCell.setCellExpression(SuperCell.getCellName(change.getRow(), change.getColumn()), "0");
//                        SuperCell.setItem(change.getRow(), change.getColumn(), "");
                    }

                    if ((String)change.getOldValue() != null)
                    for (String tok : ((String)change.getOldValue()).split(" +-/^&|<>=!")) {
                        if (SuperCell.isCellLink(tok)) {
                            SuperCell.removeLink(cell, tok);
                        }
                    }

                    if ((String)change.getNewValue() != null && !SuperCell.getCellExpression(cell).replace(" ", "").equals(((String)change.getNewValue()).replace(" ", ""))) {
                            for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
                            if (s.getMethodName().equals("showAndWait"))
                                return;
                        }
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.initStyle(StageStyle.UNDECORATED);
                        alert.setTitle("Ошибочка...");
                        alert.setHeaderText("Кажется, вы что-то не так написали. Но я исправил!");
                        alert.setContentText(SuperCell.getCellExpression(cell));

                        alert.showAndWait();
                    }

//                    SuperCell.updateCells(SuperCell.getCellName(change.getRow(), change.getColumn()));
                } catch (SuperLoopException | SuperInvalidCharacterException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                    SuperCell.setCellExpression(cell, oldExpr);
                    SuperCell.setItem(change.getRow(), change.getColumn(), (String) change.getOldValue());
                }
            }
        });
    }

    private void addRowCol(int drow, int dcol) {
        if (drow < 0 || dcol < 0) return;

        for (int row = 0; row < grid.getRowCount(); ++row) {
            for (int column = grid.getColumnCount(); column < grid.getColumnCount() + dcol; column++)
                SuperCell.addCell(row, createEmptyCell(row, column, 1, 1));
        }

        for (int row = grid.getRowCount(); row < grid.getRowCount() + drow; row++) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount() + dcol; ++column) {
                dataRow.add(createEmptyCell(row, column, 1, 1));
            }
            SuperCell.addRow(dataRow);
        }
        grid.setRows(SuperCell.getRows());
        spreadSheetView.setGrid(grid);
        for (int i = spreadSheetView.getColumns().size(); i < spreadSheetView.getColumns().size() + dcol; i++) {
            spreadSheetView.getColumns().get(spreadSheetView.getColumns().size() - 1).setPrefWidth(90);
        }
    }

    private void setUpSpreadsheetView(GridBase grid) {
        spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        spreadSheetView.getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>() {
            @Override
            public void onChanged(Change<? extends TablePosition> change) {
                if (!spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                    tf.setText(SuperCell.getCellExpression(SuperCell.getCellName(spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow(), spreadSheetView.getSelectionModel().getSelectedCells().get(0).getColumn())));
            }
        });

        for (SpreadsheetColumn c : spreadSheetView.getColumns()) {
            c.setPrefWidth(90);
        }

        addRowCol(1, 1);
    }

    public void removeRow(SpreadsheetView table, int selectedRow, int selectedCol) {
        if (table == null || selectedRow < 0 || !table.isEditable()) {
            return;
        }

        Grid oldGrid = table.getGrid();
        ObservableList<ObservableList<SpreadsheetCell>> rowsOld = oldGrid.getRows();
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<ObservableList<SpreadsheetCell>>();
        int newRows = 0;

        if (selectedRow >= rowsOld.size() || selectedRow < 0) {
            return;
            // Can not remove the only row a table has. Cleans it.
        } else if (oldGrid.getRowCount() == 1) {
            for (int column = 0; column < oldGrid.getColumnCount(); column++) {
                SuperCell.emptyCell(0, column);
//                SuperCell.setItem(0, column, "");
//                SuperCell.setCellExpression(SuperCell.getCellName(0, column), "0");
//                oldGrid.setCellValue(0, column, "");
            }
            return;
        }

        SpreadsheetCell newElem;
        final HashMap<String, String> expressions = new HashMap<String, String>();
        final HashMap<String, HashSet<String>> newLinks = new HashMap<String, HashSet<String>>();
        // Loops throw each row not to be removed, adding them to a array that contains the old row set,
        // but without the removed row.
        for (int i = 0; i < rowsOld.size(); i++) {
            if (i == selectedRow) {
                continue;
            }

            // Copies a row, cell by cell, it's type and value
            final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
            for (int column = 0; column < oldGrid.getColumnCount(); ++column) {

                SpreadsheetCellType cellType = rowsOld.get(i).get(column).getCellType();
                Class<? extends SpreadsheetCellType> cls = cellType.getClass();
                int rowSpan = rowsOld.get(i).get(column).getRowSpan();
                int columnSpan = rowsOld.get(i).get(column).getColumnSpan();

                //get the current cell value
                Object item = rowsOld.get(i).get(column).getItem();
                newElem = createEmptyCell(newRows, column, rowSpan, columnSpan);//SpreadsheetCellType.STRING.createCell(newRows, column, rowSpan, columnSpan, null);

                if (newElem != null) {
                    //Update the new cell with the original value
                    newElem.setItem(item);
                    String currCell = SuperCell.getCellName(i, column), nextCell = SuperCell.getCellName(i > selectedRow? i - 1 : i, column);
//                    System.out.println(nextCell + "     " + SuperCell.getCellExpression(currCell));
                    String currExpression = SuperCell.getCellExpression(currCell);
                    if (!currExpression.equals("")) {
                        expressions.put(nextCell, SuperCell.getCellExpression(currCell));

                        try {
                            newLinks.put(nextCell, new HashSet<String>());
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
                            expressions.put(nextCell, String.join("", tokens));
                        } catch (Exception e) {
                            System.out.println(">" + e.getMessage() + " " + e.getClass() + " " + e.getCause());
                            newLinks.put(nextCell, new HashSet<String>());
                            expressions.remove(nextCell);
//                          newExpressions.put(c, "");
                        }

                        if (newLinks.get(nextCell).isEmpty())
                            newLinks.remove(nextCell);
                    } else {

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

    public void removeColumn(SpreadsheetView table, int selectedRow, int selectedCol) {
        if (table == null || selectedCol < 0 || !table.isEditable()) {
            return;
        }

        Grid oldGrid = table.getGrid();
        ObservableList<ObservableList<SpreadsheetCell>> rowsOld = oldGrid.getRows();
        ArrayList<ObservableList<SpreadsheetCell>> rows = new ArrayList<ObservableList<SpreadsheetCell>>();
        int newRows = 0;

        if (selectedRow >= rowsOld.size() || selectedCol >= rowsOld.get(selectedRow).size() || selectedCol < 0) {
            return;
            // Can not remove the only row a table has. Cleans it.
        } else if (oldGrid.getColumnCount() == 1) {
            for (int row = 0; row < oldGrid.getRowCount(); row++) {
                SuperCell.emptyCell(row, 0);
//                SuperCell.setItem(row, 0, "");
//                SuperCell.setCellExpression(SuperCell.getCellName(row, 0), "0");
//                oldGrid.setCellValue(0, column, "");
            }
            return;
        }

        SpreadsheetCell newElem;
        final HashMap<String, String> expressions = new HashMap<String, String>();
        // Loops throw each row not to be removed, adding them to a array that contains the old row set,
        // but without the removed row.
        for (int i = 0; i < rowsOld.size(); i++) {

            // Copies a row, cell by cell, it's type and value
            final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
            for (int column = 0; column < oldGrid.getColumnCount(); ++column) {
                if (column == selectedCol) {
                    continue;
                }

                SpreadsheetCellType cellType = rowsOld.get(i).get(column).getCellType();
                Class<? extends SpreadsheetCellType> cls = cellType.getClass();
                int rowSpan = rowsOld.get(i).get(column).getRowSpan();
                int columnSpan = rowsOld.get(i).get(column).getColumnSpan();

                //get the current cell value
                Object item = rowsOld.get(i).get(column).getItem();
                newElem = createEmptyCell(newRows, column, rowSpan, columnSpan);//SpreadsheetCellType.STRING.createCell(newRows, column, rowSpan, columnSpan, null);

                if (newElem != null) {
                    //Update the new cell with the original value
                    newElem.setItem(item);
                    String currCell = SuperCell.getCellName(i, column), nextCell = SuperCell.getCellName(i, column > selectedCol ? column - 1 : column);
                    expressions.put(nextCell, SuperCell.getCellExpression(currCell));
                    list.add(newElem);
                }
            }

            // Adds a valid row to the set of remaining rows.
            rows.add(list);
            newRows++;
        }

        // Updates the grid.
        SuperCell.setRows(rows);
        SuperCell.updateAll();
        oldGrid.setRows(rows);
        oldGrid.getColumnHeaders().addAll(oldGrid.getColumnHeaders());
        SuperCell.setExpressions(expressions);
        table.setGrid(oldGrid);

        // Fixes the selection
        if (selectedCol >= 0 && selectedCol < table.getColumns().size()) {
            table.getSelectionModel().clearSelection();
            table.getSelectionModel().select(selectedRow, table.getColumns().get(selectedCol > 0 ? selectedCol - 1 : selectedCol));
        }
    }

    private SpreadsheetCell createEmptyCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan, "");

        return cell;
    }
}
