import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;

/**
 * Created by sakura on 11/5/16.
 */
public class SuperFontChooser extends ComboBox<String> {
    public SuperFontChooser() {
        super();

        ObservableList<String> values = FXCollections.observableArrayList();
        values.addAll("7.5 9 10 11 12 13 14 16 18 20 24 28 32 36 42 48 64 72 96".split(" "));
        setValue("13");

        setStyle("-fx-background-color: null;");

        this.setItems(values);
    }
}
