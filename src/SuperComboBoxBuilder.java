import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

/**
 * Spreadsheet.
 * Created by sakura on 11/13/16.
 */
public class SuperComboBoxBuilder<T> {
    ComboBox<T> sfc;

    SuperComboBoxBuilder() {
        sfc = new ComboBox<>();
    }

    public SuperComboBoxBuilder<T> setTooltip(String tooltip) {
        sfc.setTooltip(new Tooltip(tooltip));
        return this;
    }

    public SuperComboBoxBuilder<T> setStyle(String style) {
        sfc.setStyle(style);
        return this;
    }

    public SuperComboBoxBuilder<T> setValue(T value) {
        sfc.setValue(value);
        return this;
    }

    public SuperComboBoxBuilder<T> setValues(T[] values) {
        ObservableList<T> list = FXCollections.observableArrayList();
        list.addAll(values);
        sfc.setItems(list);
        return this;
    }

    public SuperComboBoxBuilder<T> setOnAction(EventHandler<ActionEvent> eventHandler) {
        sfc.setOnAction(eventHandler);
        return this;
    }

    public ComboBox<T> getInstance() {
        return sfc;
    }
}