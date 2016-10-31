import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.spreadsheet.*;

import java.util.ArrayList;

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

        int rowCount = 10;
        int columnCount = 10;
        grid = new GridBase(rowCount, columnCount);
        normalGrid(grid);

        spreadSheetView = new SpreadsheetView(grid);
        spreadSheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadSheetView.getSelectionModel().getSelectedCells().addListener(new ListChangeListener<TablePosition>() {
            @Override
            public void onChanged(Change<? extends TablePosition> change) {
                tf.setText(SuperCell.getCellExpression(SuperCell.getCellName(spreadSheetView.getSelectionModel().getSelectedCells().get(0).getRow(), spreadSheetView.getSelectionModel().getSelectedCells().get(0).getColumn())));
            }
        });
        for (SpreadsheetColumn c : spreadSheetView.getColumns()) {
            c.setPrefWidth(90);
        }

        tf = new TextField("hui");
        Button addr = new Button("Add row");
        Button addc = new Button("Add col");
        tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (!keyEvent.getCode().toString().equals("ENTER") || spreadSheetView.getSelectionModel().getSelectedCells().isEmpty())
                    return;
                String text = tf.getText();
                int row = spreadSheetView.getSelectionModel().getFocusedCell().getRow();
                int column = spreadSheetView.getSelectionModel().getFocusedCell().getColumn();
                try {
                    String prev = SuperCell.getCellValue(SuperCell.getCellName(row, column));
                    SuperCell.setItem(row, column, SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(text + "\n" + SuperCell.getCellName(row, column))), SuperCell.getCellName(row, column)).toString());
                    for (String tok : SuperCell.getCellExpression(SuperCell.getCellName(row, column)).split(" ")) {
                        if (SuperCell.isCellLink(tok)) {
                            SuperCell.removeLink(SuperCell.getCellName(row, column), tok);
                        }
                    }
//                    SuperCell.updateCells(SuperCell.getCellName(row, column));
                } catch (SuperLoopException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());
                    alert.initStyle(StageStyle.UNDECORATED);

                    alert.showAndWait();
                }
            }
        });
        addr.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                copySpreadsheetView(1, 0);
            }
        });
        addc.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                copySpreadsheetView(0, 1);
            }
        });
        HBox hbox = new HBox(tf, addr, addc);
        hbox.setHgrow(tf, Priority.ALWAYS);
        borderPane.setTop(hbox);

        borderPane.setCenter(spreadSheetView);

        borderPane.setLeft(buildCommonControlGrid(spreadSheetView, borderPane,"Both"));

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

        final Scene scene = new Scene((Parent) getPanel(primaryStage), 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void normalGrid(GridBase grid) {
        SuperCell.initRows(new ArrayList<>(grid.getRowCount()));
        for (int row = 0; row < grid.getRowCount(); ++row) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                dataRow.add(createEmptyCell(row, column, 1, 1)/*SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "")*/);
            }
            SuperCell.add(dataRow);
        }
        grid.setRows(SuperCell.rows);

        grid.addEventHandler(GridChange.GRID_CHANGE_EVENT, new EventHandler<GridChange>() {
            @Override
            public void handle(GridChange change) {
//                System.out.println("OBSVAL: '" + observableValue.getValue() + "' '" + s + "' '" + s2 + "'");
                try {
                    if (change.getNewValue() != null && !((String)change.getNewValue()).trim().equals("")) {
                        SuperCell.setItem(change.getRow(), change.getColumn(), SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize((String)change.getNewValue() + "\n" + SuperCell.getCellName(change.getRow(), change.getColumn()))), SuperCell.getCellName(change.getRow(), change.getColumn())).toString());
                    } else {
                        SuperCell.setItem(change.getRow(), change.getColumn(), "");
                    }
                    // /s/s/expr/
                    for (String tok : ((String)change.getOldValue()).split(" +-/^&|<>=!")) {
                        if (SuperCell.isCellLink(tok)) {
                            SuperCell.removeLink(SuperCell.getCellName(change.getRow(), change.getColumn()), tok);
                        }
                    }
//                    SuperCell.updateCells(SuperCell.getCellName(change.getRow(), change.getColumn()));
                } catch (SuperLoopException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());
                    alert.initStyle(StageStyle.UNDECORATED);

                    alert.showAndWait();
                    SuperCell.setItem(change.getRow(), change.getColumn(), (String)change.getOldValue());
                }
            }
        });
    }

    private void copySpreadsheetView(int drow, int dcol) {
        for (int row = 0; row < grid.getRowCount(); ++row) {
            for (int column = grid.getColumnCount(); column < grid.getColumnCount() + dcol; column++)
                SuperCell.add(row, createEmptyCell(row, column, 1, 1));
        }

        for (int row = grid.getRowCount(); row < grid.getRowCount() + drow; row++) {
            final ObservableList<SpreadsheetCell> dataRow = FXCollections.observableArrayList();
            for (int column = 0; column < grid.getColumnCount() + dcol; ++column) {
                dataRow.add(createEmptyCell(row, column, 1, 1)/*SpreadsheetCellType.STRING.createCell(row, column, 1, 1, "")*/);
            }
            SuperCell.add(dataRow);
        }
        grid.setRows(SuperCell.rows);

        SpreadsheetView spreadSheetView1 = new SpreadsheetView(grid);
        for (int i = 0; i < spreadSheetView.getColumns().size(); i++) {
            spreadSheetView1.getColumns().get(i).setPrefWidth(spreadSheetView.getColumns().get(i).getWidth());
        }
        for (int i = spreadSheetView.getColumns().size(); i < spreadSheetView.getColumns().size() + dcol; i++) {
            spreadSheetView1.getColumns().get(spreadSheetView1.getColumns().size() - 1).setPrefWidth(90);
        }
        spreadSheetView = spreadSheetView1;

        borderPane.getChildren().remove(borderPane.getCenter());
        borderPane.setCenter(spreadSheetView);
    }

    private SpreadsheetCell createEmptyCell(int row, int column, int rowSpan, int colSpan) {
        SpreadsheetCell cell = SpreadsheetCellType.STRING.createCell(row, column, rowSpan, colSpan, "");
        /*cell.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String s2) {
                // изменяет, когда вызывается setItem с новым значением, а не только при вводе текста
                System.out.println("OBSVAL: '" + observableValue.getValue() + "' '" + s + "' '" + s2 + "'");
                try {
                    cell.setItem(SuperEvaluator.evaluate(SuperParser.parse(SuperLexer.tokenize(s2 + "\n" + SuperCell.getCellName(row, column))), SuperCell.getCellName(row, column)).toString());
                    // /s/s/expr/
                    for (String tok : SuperCell.getCellExpression(SuperCell.getCellName(row, column)).split(" +-/^&|<>=!")) {
                        if (SuperCell.isCellLink(tok)) {
                            SuperCell.removeLink(SuperCell.getCellName(row, column), tok);
                        }
                    }
                    //SuperCell.updateCells(SuperCell.getCellName(row, column));
                } catch (SuperLoopException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Ошибочка...");
                    alert.setHeaderText(e.getMessage());
                    alert.initStyle(StageStyle.UNDECORATED);

                    alert.showAndWait();
                    cell.setItem(s);
                }
            }
        });*/

        return cell;
    }
}
