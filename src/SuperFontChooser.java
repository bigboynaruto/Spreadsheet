import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;

/**
 * Created by sakura on 11/5/16.
 */
public class SuperFontChooser extends ComboBox<String> {
    public SuperFontChooser() {
        super();

        ObservableList<String> values = FXCollections.observableArrayList();
        values.addAll("6 7 8 9 10 10.5 11 12 13 14 15 16 18 20 22 24 26 28 32 36 40 44 48 54 60 66 72 80 88 96".split(" "));
        setValue("13");

        setStyle("-fx-background-color: null;");

        this.setItems(values);
        this.setTooltip(new Tooltip("font-size"));
    }
}
