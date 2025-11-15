package view;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import view.middleware.frontend.JackarooController;

import java.io.IOException;
import java.util.function.Consumer;

public final class JackarooManager {

    static {
        Font.loadFont(JackarooManager.class.getResourceAsStream("/fonts/Bauhaus93.ttf"), 10);
    }

    /**
     * Loads an FXML and returns a Scene, binding size for non-Board roots.
     */
    public static Scene getSceneFrom(final String url) throws IOException {
        final Parent root = new FXMLLoader(
                JackarooManager.class.getResource("/scenes/" + url + ".fxml")
        ).load();

        Scene scene = new Scene(root);

        if (!url.equals("Board") && !url.equals("Settings") && root instanceof Region regionRoot) {
            regionRoot.prefWidthProperty().bind(scene.widthProperty());
            regionRoot.prefHeightProperty().bind(scene.heightProperty());
            bindImageViewToScene(regionRoot, scene);
        }

        return scene;
    }

    public static void goToScene(final Stage stage,
                                 final String fxmlName,
                                 final Consumer<Parent> consumer) {
        if (!fxmlName.equals("Board")) {
            JackarooController.disableChatBox();
        }

        if (stage.getScene() == null) {
            try {
                Scene scene = getSceneFrom(fxmlName);
                if (consumer != null) consumer.accept(scene.getRoot());
                stage.setScene(scene);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(80, 80);

        Parent currentRoot = stage.getScene().getRoot();
        StackPane wrapper;
        if (currentRoot instanceof StackPane) {
            wrapper = (StackPane) currentRoot;
        } else {
            wrapper = new StackPane(currentRoot);
            stage.getScene().setRoot(wrapper);
        }

        StackPane overlay = new StackPane(spinner);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        wrapper.getChildren().add(overlay);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), spinner);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.play();

        Task<Scene> loadTask = new Task<>() {
            @Override
            protected Scene call() throws Exception {
                return getSceneFrom(fxmlName);
            }
        };

        loadTask.setOnSucceeded(_ -> {
            rotate.stop();
            FadeTransition fadeOverlay = new FadeTransition(Duration.millis(300), overlay);
            fadeOverlay.setFromValue(1.0);
            fadeOverlay.setToValue(0.0);
            fadeOverlay.setOnFinished(_ -> {
                wrapper.getChildren().remove(overlay);
                Scene newScene = loadTask.getValue();
                Parent newRoot = newScene.getRoot();
                newRoot.setOpacity(0.0);
                stage.setScene(newScene);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                fadeIn.setOnFinished(_ -> {
                    if (consumer != null) {
                        Platform.runLater(() -> consumer.accept(loadTask.getValue().getRoot()));
                    }
                });
                fadeIn.play();
            });
            fadeOverlay.play();

        });

        loadTask.setOnFailed(_ -> {
            rotate.stop();
            wrapper.getChildren().remove(overlay);
            System.err.println("Failed to load scene: " + loadTask.getException());
        });

        new Thread(loadTask, "Scene-Loader").start();
    }

    private static void bindImageViewToScene(final Parent parent, final Scene scene) {
        parent.getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof ImageView imageView) {
                imageView.fitWidthProperty().bind(scene.widthProperty());
                imageView.fitHeightProperty().bind(scene.heightProperty());
            } else if (node instanceof Parent) {
                bindImageViewToScene((Parent) node, scene);
            }
        });
    }




}
