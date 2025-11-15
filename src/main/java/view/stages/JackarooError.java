package view.stages;

import javafx.scene.control.Alert;

public final class JackarooError {

    private final String errorMsg;

    public JackarooError(String errorMsg) {
        this.errorMsg = errorMsg;

        start();
    }

    public void start() {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error");
        alert.setHeaderText(errorMsg);

        alert.showAndWait();
    }

}
