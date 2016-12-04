import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/**
 * Created by Mashiro Shiina on 12/04/16.
 */
public class SuperAlert extends Alert {
    public SuperAlert(AlertType type) {
        super(type);
        initStyle(StageStyle.UNDECORATED);
        if (type == AlertType.NONE)
            getButtonTypes().add(ButtonType.OK);
    }

    public SuperAlert owner(Window w) {
        initOwner(w);
        return this;
    }

    public SuperAlert header(String text) {
        setHeaderText(text);
        return this;
    }

    public SuperAlert content(String text) {
        setContentText(text);
        return this;
    }
}
