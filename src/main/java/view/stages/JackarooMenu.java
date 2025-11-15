package view.stages;

import javafx.application.Application;
import javafx.stage.Stage;
import view.JackarooManager;
import view.middleware.frontend.Jackaroosounds;

public final class JackarooMenu extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        JackarooManager.goToScene(stage, "Menu", _ -> {
            stage.setTitle("Jackaroo Menu");
            stage.show();

//            Jackaroosounds.play();

            stage.setOnCloseRequest(_ -> Jackaroosounds.stop());
        });
    }

}
