package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application{

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Group group = new Group();
        Scene scene = new Scene(group);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setWidth(500);
        primaryStage.setHeight(500);
        primaryStage.setTitle("Snake");

        Stage stage = new Stage();
        VBox vBox = new VBox();
        HBox hBox = new HBox();
        HBox hBox2 = new HBox();
        Scene scene1 = new Scene(vBox);
        stage.setScene(scene1);
        stage.setWidth(380);
        stage.setHeight(150);
        stage.setResizable(false);
        stage.setTitle("Welcome");
        stage.show();
        Label label = new Label("Name:                            ");
        Label label1 = new Label("Direction (W-A-S-D):     ");
        TextField textField = new TextField();
        TextField textField1 = new TextField();
        Button button = new Button("OK");
        hBox.getChildren().addAll(label, textField);
        hBox2.getChildren().addAll(label1, textField1);
        vBox.getChildren().addAll(hBox, new Separator(), hBox2, new Separator(), button);

        button.setOnAction(e -> {
            if (textField.getText() != null&& textField1.getText() != null&&
                    (textField1.getText().equals("w")|| textField1.getText().equals("a")||
                    textField1.getText().equals("s")|| textField1.getText().equals("d"))) {
                stage.close();
                e.consume();
                primaryStage.show();
                primaryStage.setOnCloseRequest(event -> System.exit(0));
                new Snake(textField.getText(), textField1.getText(), scene, group);
            }
        });

    }
}
