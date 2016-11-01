import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.spreadsheet.*;

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

        tf = new TextField("hui");
        Button addr = new Button("Add row");
        Button addc = new Button("Add col");
        tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
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
                    for (String tok : SuperCell.getCellExpression(cell).split(" ")) {
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
                } catch (SuperLoopException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                    SuperCell.setCellExpression(cell, oldExpr);
                }
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
        HBox hbox = new HBox(tf, addr, addc);
        hbox.setHgrow(tf, Priority.ALWAYS);
        borderPane.setTop(hbox);

        borderPane.setCenter(spreadSheetView);
        spreadSheetView.getSelectionModel().select(0, spreadSheetView.getColumns().get(0));

//        borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane,"Both"));

        return borderPane;
    }

    /**
     * Build a common control Grid with some options on the left to control the
     * SpreadsheetViewInternal
     * @param gridType
     *
     * @return
     */
    private GridPane buildCommonControlGrid(final SpreadsheetView spv,final BorderPane borderPane, String gridType) {
        /*final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        return grid;*/
        final CheckBox rowHeader = new CheckBox();
        final CheckBox columnHeader = new CheckBox();
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(5, 5, 5, 5));

        int row = 0;

        // row header
        Label rowHeaderLabel = new Label("Row header: ");
        rowHeaderLabel.getStyleClass().add("property");
        grid.add(rowHeaderLabel, 0, row);
        rowHeader.setSelected(true);
        spreadSheetView.setShowRowHeader(true);
        grid.add(rowHeader, 1, row++);
        rowHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowRowHeader(arg2);
            }
        });

        // column header
        Label columnHeaderLabel = new Label("Column header: ");
        columnHeaderLabel.getStyleClass().add("property");
        grid.add(columnHeaderLabel, 0, row);
        columnHeader.setSelected(true);
        spreadSheetView.setShowColumnHeader(true);
        grid.add(columnHeader, 1, row++);
        columnHeader.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
                spreadSheetView.setShowColumnHeader(arg2);
            }
        });

        //Row Header width
        Label rowHeaderWidth = new Label("Row header width: ");
        rowHeaderWidth.getStyleClass().add("property");
        grid.add(rowHeaderWidth, 0, row);
        Slider slider = new Slider(15, 100, 30);
//        spreadSheetView.rowHeaderWidthProperty().bind(slider.valueProperty());
        slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                spreadSheetView.setRowHeaderWidth(number2.doubleValue());
            }
        });
        grid.add(slider, 1, row++);

        return grid;
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
                SuperCell.writeToFile("file.txt");
            }
        });
        openItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                SuperCell.readFromFile("file.txt");
            }
        });
        exitItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (!SuperCell.isSaved()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ваш помощник на страже вашей безопасности!");
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

        addrItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
        addcItem.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
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

        menuView.getItems().addAll(addrItem, addcItem);

        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, getPanel(primaryStage));

        primaryStage.setScene(scene);
        primaryStage.show();
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
//                Thread.dumpStack();
                String oldExpr = SuperCell.getCellExpression(SuperCell.getCellName(change.getRow(), change.getColumn()));
                String cell = SuperCell.getCellName(change.getRow(), change.getColumn());
                try {
                    if (change.getNewValue() != null && !((String)change.getNewValue()).replace(" ", "").equals("")) {
                        String[] tokens = SuperLexer.tokenize((String)change.getNewValue());
                        SuperCell.setCellExpression(cell, String.join("", tokens));
                        SuperCell.setItem(change.getRow(), change.getColumn(), SuperEvaluator.evaluate(SuperParser.parse(tokens), cell).toString());
                    } else {
                        SuperCell.setCellExpression(SuperCell.getCellName(change.getRow(), change.getColumn()), "0");
                    }

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
                } catch (SuperLoopException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.initStyle(StageStyle.UNDECORATED);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());

                    alert.showAndWait();
                    SuperCell.setCellExpression(cell, oldExpr);
                    SuperCell.setItem(change.getRow(), change.getColumn(), (String)change.getOldValue());
                }
            }
        });
    }

    private void addRowCol(int drow, int dcol) {
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
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

    private SpreadsheetCell createEmptyCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan, "");

        return cell;
    }
}
