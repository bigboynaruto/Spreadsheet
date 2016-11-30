import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.spreadsheet.*;

import java.io.File;

/**
 * Created by sakura on 10/26/16.
 */
public class SuperSpreadsheet extends Application {
    private final String EXTENSION = "dno";

    private SpreadsheetView spreadSheetView;
    private TextField tf, cellChooser;
    private ToggleButton boldButt, italicButt, underButt;
    private Button clearButt;
    private ComboBox<String> fontChooser;

    public static void main(String[] args) {
        launch(args);
    }

    private Node getPanel() {
        BorderPane borderPane = new BorderPane();

        int rowCount = 9;
        int columnCount = 9;
        GridBase grid = new GridBase(rowCount, columnCount);
        normalGrid(grid);

        setUpSpreadsheetView(grid);
        tf = new TextField("Добро пожаловать");

        Button addr = new Button("Add row");
        Button addc = new Button("Add col");
        tf.setOnAction(keyEvent -> {
            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int column = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            if (row < 0 || column < 0)
                return;
            String text = tf.getText();
            String cell = SuperCell.getCellName(row, column);
            setCellValue(spreadSheetView.getGrid().getRows().get(row).get(column), text);
            tf.setText(SuperCell.getCellExpression(cell));
        });
        addr.setOnMouseClicked( mouseEvent -> SuperCell.addRowCol(spreadSheetView, 1, 0));
        addc.setOnMouseClicked(mouseEvent -> SuperCell.addRowCol(spreadSheetView, 0, 1));

        fontChooser = new SuperComboBoxBuilder<String>()
                .setTooltip("Font size")
                .setValues(("6 7 8 9 10 10.5 11 12 13 14 15 16 18 20 22 24 " +
                        "26 28 32 36 40 44 48 54 60 66 72 80 88 96").split(" "))
                .setValue("13")
                .setOnAction(handler -> spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
                    SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
                    setStyleProperty(cell.styleProperty(), "-fx-font-size", ((ComboBox)handler.getSource()).getValue() + " ex");
                })).getInstance();

        boldButt = new ToggleButton("B");
        boldButt.setSelected(false);
        boldButt.setStyle("-fx-font-weight:bold;");
        boldButt.setTooltip(new Tooltip("text-bold"));

        italicButt = new ToggleButton("I");
        italicButt.setSelected(false);
        italicButt.setStyle("-fx-font-style:italic;");
        italicButt.setTooltip(new Tooltip("text-italic"));

        underButt = new ToggleButton("U");
        underButt.setSelected(false);
        underButt.setStyle("-fx-underline:true;");
        underButt.setTooltip(new Tooltip("text-underline"));

        clearButt = new Button("C");
        clearButt.setTooltip(new Tooltip("style-clear"));
        clearButt.setPrefWidth(30);

        boldButt.setOnAction(handler -> spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
            SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
            setStyleProperty(cell.styleProperty(), "-fx-font-weight", boldButt.isSelected() ? "bold" : "normal");
        }));
        italicButt.setOnAction(handler -> spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
            SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
            setStyleProperty(cell.styleProperty(), "-fx-font-style", italicButt.isSelected() ? "italic" : "normal");
        }));
        underButt.setOnAction(handler -> spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
            SpreadsheetCell cell = SuperCell.getRows().get(pos.getRow()).get(pos.getColumn());
            setStyleProperty(cell.styleProperty(), "-fx-underline", underButt.isSelected() ? "true" : "false");
        }));
        clearButt.setOnAction(handler -> spreadSheetView.getSelectionModel().getSelectedCells().forEach(pos -> {
            SuperCell.getRows().get(pos.getRow()).get(pos.getColumn()).setStyle("");
            boldButt.setSelected(false);
            italicButt.setSelected(false);
            underButt.setSelected(false);
        }));

        cellChooser = new TextField();
        cellChooser.setMaxWidth(100);
        cellChooser.setOnAction(actionEvent -> {
            String cell = cellChooser.getText();
            if (!SuperCell.isCellLink(cell))
                return;

            int row = SuperCell.getCellRow(cell), col = SuperCell.getCellColumn(cell);
            if (col >= spreadSheetView.getColumns().size())
                return;
            if (row >= SuperCell.getRows().size())
                return;

            spreadSheetView.getSelectionModel().clearAndSelect(row, spreadSheetView.getColumns().get(col));
        });

        HBox styleBox = new HBox(createColorPicker("-fx-background-color", Color.WHITE),
                createColorPicker("-fx-text-fill", Color.BLACK),
                fontChooser,
                boldButt, italicButt, underButt, clearButt);
        HBox hbox = new HBox(cellChooser, tf, addr, addc);
        VBox vbox = new VBox(styleBox, hbox);
        HBox.setHgrow(tf, Priority.ALWAYS);
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

        scene.widthProperty().addListener((observableValue, number, number2) ->
                spreadSheetView.setPrefWidth(number2.doubleValue())
        );
        scene.heightProperty().addListener((observableValue, number, number2) ->
                spreadSheetView.setPrefHeight(number2.doubleValue())
        );

        ((VBox) scene.getRoot()).getChildren().addAll(createMenuBar(primaryStage), getPanel());

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar(Stage primaryStage) {
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
        saveItem.setOnAction(actionEvent -> {
            File file = chooseFile(primaryStage);
            if (file == null)
                return;

            try {
                SuperCell.writeToFile(file.getName());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setHeaderText(e.getMessage());

                alert.showAndWait();
            }
        });
        openItem.setOnAction(actionEvent -> {
            File file = chooseFile(primaryStage);

            if (file == null)
                return;

            try {
                SuperCell.readFromFile(spreadSheetView, file.getName());
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setHeaderText(e.getMessage());

                alert.showAndWait();
            }
        });
        exitItem.setOnAction(actionEvent -> {
            if (!SuperCell.isSaved()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setHeaderText("Ой, а вы не сохранили свой файлик... Точно хотите выйти?");
                alert.setContentText("Подумайте дважды, перед тем, как соглашаться!");

                alert.showAndWait();

                if (alert.getResult().equals(ButtonType.OK)) {
                    Platform.exit();
                }
            }
            else Platform.exit();
        });
        menuFile.getItems().addAll(openItem, saveItem, new SeparatorMenuItem(), exitItem);

        MenuItem addrItem = new MenuItem("Добавить строку");
        MenuItem addcItem = new MenuItem("Добавить колонку");
        MenuItem removerItem = new MenuItem("Удалить строку");

        addrItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        addcItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
        removerItem.setAccelerator(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN));
        addrItem.setOnAction(actionEvent -> SuperCell.addRowCol(spreadSheetView, 1, 0));
        addcItem.setOnAction(actionEvent -> SuperCell.addRowCol(spreadSheetView, 0, 1));
        removerItem.setOnAction(actionEvent -> {
            if (spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                return;
            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int col = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            SuperCell.removeRow(spreadSheetView, row, col);
        });

        menuView.getItems().addAll(addrItem, addcItem, new SeparatorMenuItem(), removerItem);


        MenuItem cutItem = new MenuItem("Вырезать");
        MenuItem pasteItem = new MenuItem("Вставить вырезанное");

        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN, KeyCodeCombination.SHIFT_DOWN));

        cutItem.setOnAction(actionEvent -> {
            if (spreadSheetView.getSelectionModel().getSelectedCells().size() < 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setTitle("Ошибочка...");
                alert.setHeaderText("Выделите сначала ячейку...");

                alert.showAndWait();
                return;
            }
            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int col = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            SuperCell.moveCell(row, col);
        });
        pasteItem.setOnAction(actionEvent -> {
            if (spreadSheetView.getSelectionModel().getSelectedCells().size() < 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setTitle("Ошибочка...");
                alert.setHeaderText("Выделите сначала ячейку...");

                alert.showAndWait();
                return;
            }

            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int col = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            try {
                SuperCell.pasteCell(row, col);
            } catch (SuperCellNotSelectedException | SuperLoopException | SuperInvalidCharacterException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setTitle("Ошибочка...");
                alert.setHeaderText(e.getMessage());

                alert.showAndWait();
            }

            tf.setText(SuperCell.getCellExpression(row, col));
        });

        menuEdit.getItems().addAll(cutItem, pasteItem);

        Menu menuHelp = new Menu("Помощь");
        MenuItem aboutItem = new MenuItem("Справка");
        menuHelp.getItems().add(aboutItem);
        menuHelp.setOnAction((actionEvent) -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.setHeaderText("Операторы");

            String operators = "";
            for (SuperExpressionOperator operator : SuperProcessingStrategy.getOperators()) {
                operators += operator.toString() + "\n";
            }
            alert.setContentText(operators);

            alert.showAndWait();
        });

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView, menuHelp);

        return menuBar;
    }

    private File chooseFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Anime filechooser");

        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter(EXTENSION.toUpperCase() + " files (*."
                        + EXTENSION + ")", "*." + EXTENSION);
        fileChooser.getExtensionFilters().add(extFilter);

        return fileChooser.showOpenDialog(primaryStage);
    }

    private ColorPicker createColorPicker(final String property, Color defaultColor) {
        final ColorPicker colpicker = new ColorPicker(defaultColor);
        colpicker.setTooltip(new Tooltip(property.replace("-fx-", "")));
        colpicker.setStyle("-fx-color-label-visible: false;");

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

        assert style != null;
        style.setValue(str);
    }

    private static String getStyleProperty(String style, String property) {
        int index = style.indexOf(property);
        if (index == -1) return null;

        index += property.length() + 1;

        return style.substring(index, style.indexOf(";", index));
    }

    private static void setCellValue(SpreadsheetCell cell, String expr) {
        if (expr == null)
            expr = "";
        int row = cell.getRow(), column = cell.getColumn();
        String cellName = SuperCell.getCellName(row, column);
        String oldExpr = SuperCell.getCellExpression(cellName);
        try {
            SuperCell.setCellExpression(cellName, expr);

            if (!SuperCell.getCellExpression(cellName).replace(" ", "").equals(expr.replace(" ", ""))) {
                for (StackTraceElement s : Thread.currentThread().getStackTrace()) {
                    if (s.getMethodName().equals("showAndWait"))
                        return;
                }
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.initStyle(StageStyle.UNDECORATED);
                alert.setTitle("Ошибочка...");
                alert.setHeaderText("Кажется, вы что-то не так написали. Но я исправил!");
                alert.setContentText(SuperCell.getCellExpression(cellName));

                alert.showAndWait();
            }
        } catch (SuperLoopException | SuperInvalidCharacterException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initStyle(StageStyle.UNDECORATED);
            alert.setTitle("Ошибочка...");
            alert.setHeaderText(e.getMessage());

            alert.showAndWait();
            try {
                SuperCell.setCellExpression(cellName, oldExpr);
            } catch (Exception ignored) {}
        }
    }

    private void normalGrid(GridBase grid) {
        SuperCell.initRows(grid.getRowCount());
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                dataRow.add(SuperCell.createEmptyCell(row, column, 1, 1));
            }
            SuperCell.addRow(dataRow);
        }
        grid.setRows(SuperCell.getRows());

        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, change -> {
//            String cell = SuperCell.getCellName(change.getRow(), change.getColumn());
//            setCellValue(SuperCell.getCell(cell), (String) change.getNewValue());
            setCellValue(spreadSheetView.getGrid().getRows().get(change.getRow()).get(change.getColumn()),
                    (String) change.getNewValue());
        });
    }

    private void setUpSpreadsheetView(GridBase grid) {
        spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        spreadSheetView.getSelectionModel().getSelectedCells().addListener((ListChangeListener<? super TablePosition>) change -> {
            if (spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                return;

            int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
            int col = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
            if (row < 0 || col < 0)
                return;
            SpreadsheetCell cell = SuperCell.getRows().get(row).get(col);
            tf.setText(SuperCell.getCellExpression(row, col));
            cellChooser.setText(SuperCell.getCellName(row, col));

            boldButt.setSelected(cell.getStyle() != null && cell.getStyle().contains("-fx-font-weight:bold"));
            italicButt.setSelected(cell.getStyle() != null && cell.getStyle().contains("-fx-font-style:italic"));
            underButt.setSelected(cell.getStyle() != null && cell.getStyle().contains("-fx-underline:true"));

            String fontSize = getStyleProperty(cell.getStyle() == null ? "" : cell.getStyle(), "-fx-font-size");
            fontSize = fontSize == null ? "13" : fontSize.substring(0, fontSize.indexOf(" "));
            fontChooser.setValue(fontSize);
        });
        spreadSheetView.addEventHandler(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN).match(keyEvent))
                boldButt.fire();
            else if (new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN).match(keyEvent))
                italicButt.fire();
            else if (new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN).match(keyEvent))
                underButt.fire();
            else if (new KeyCodeCombination(KeyCode.DELETE, KeyCombination.CONTROL_DOWN).match(keyEvent))
                clearButt.fire();
        });

        for (SpreadsheetColumn c : spreadSheetView.getColumns()) {
            c.setPrefWidth(90);
        }

        SuperCell.addRowCol(spreadSheetView, 1, 1);
    }
}
