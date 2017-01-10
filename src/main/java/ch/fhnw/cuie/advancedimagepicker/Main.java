package ch.fhnw.cuie.advancedimagepicker;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	public void start(Stage primaryStage) throws Exception {

		// Image View
		AdvancedImageView advancedImageView = new AdvancedImageView("Eiffel Tower");
		advancedImageView.setFitHeight(300);
		advancedImageView.setFitWidth(200);

		// Demo fields
		Label lblName = new Label("Name");
		lblName.setPadding(new Insets(23d));

		TextField buildingName = new TextField();
		buildingName.setMinWidth(360d);

		HBox hBox = new HBox();
		hBox.setMargin(buildingName, new Insets(20d));
		hBox.getChildren().addAll(lblName, buildingName);

		// Main pane
		BorderPane rootPanel = new BorderPane();
		rootPanel.setCenter(advancedImageView);
		rootPanel.setTop(hBox);
		rootPanel.setCenter(advancedImageView);

		Scene scene = new Scene(rootPanel);
		primaryStage.setTitle("AdvancedImagePicker");
		primaryStage.setWidth(500);
		primaryStage.setHeight(600);

		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
