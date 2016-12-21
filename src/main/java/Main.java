import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application{

    public void start(Stage primaryStage) throws Exception {

        Parent rootPanel = new AdvancedImagePicker();

        Scene scene = new Scene(rootPanel);

        primaryStage.setTitle("AdvancedImagePicker");
        primaryStage.setWidth(300);
        primaryStage.setHeight(300);

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
