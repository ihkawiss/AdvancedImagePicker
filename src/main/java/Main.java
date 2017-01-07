import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{

    public void start(Stage primaryStage) throws Exception {

//        Parent rootPanel = new AdvancedImagePicker("Berliner Mauer");

        AdvancedImageView advancedImageView = new AdvancedImageView("Eiffel Tower");
        BorderPane rootPanel = new BorderPane();
        advancedImageView.setFitHeight(300);
        advancedImageView.setFitWidth(200);
        rootPanel.setTop(advancedImageView);

        Scene scene = new Scene(rootPanel);
//        scene.setFill(Paint.valueOf("red"));

        primaryStage.setTitle("AdvancedImagePicker");
        primaryStage.setWidth(900);
        primaryStage.setHeight(600);

        primaryStage.setScene(scene);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
