package ch.fhnw.cuie.advancedimagepicker;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	public void start(Stage primaryStage) throws Exception {

		// Image View
		AdvancedImageView advancedImageView = new AdvancedImageView("Taj Mahal");
		advancedImageView.setFitHeight(600);
		advancedImageView.setFitWidth(400);

		// Demo fields
		Label lblName = new Label("Name");
		lblName.setPadding(new Insets(23d));

		TextField buildingName = new TextField("Taj Mahal");
		buildingName.setMinWidth(360d);

        Button button = new Button("OK");
        button.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                String newValue = buildingName.getText();
                if (newValue != null && !newValue.isEmpty()) {
                    advancedImageView.setSearchTerm(newValue);
                    advancedImageView.loadPreviewImage();
                }
            }
        });

		HBox hBox = new HBox();
		hBox.setMargin(buildingName, new Insets(20d));
		hBox.setMargin(button, new Insets(20d));
		hBox.getChildren().addAll(lblName, buildingName, button);

		// Main pane
		BorderPane rootPanel = new BorderPane();
		rootPanel.setCenter(advancedImageView);
		rootPanel.setTop(hBox);
		rootPanel.setCenter(advancedImageView);

		Scene scene = new Scene(rootPanel);
		primaryStage.setTitle("AdvancedImagePicker");
		primaryStage.setWidth(600);
		primaryStage.setHeight(900);

		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
