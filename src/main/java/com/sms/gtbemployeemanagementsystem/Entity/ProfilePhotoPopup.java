package com.sms.gtbemployeemanagementsystem.Entity;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.File;

public class ProfilePhotoPopup {

    public static void show(Window owner, File photoFile, ProfileInfo info, Runnable onEditRequested) {
        VBox contentBox = new VBox(14);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(30));

        // Photo + edit "+" indicator, overlapping bottom-right of the circle
        StackPane photoStack = new StackPane();
        photoStack.setPrefSize(160, 160);
        photoStack.setMaxSize(160, 160);

        if (photoFile != null) {
            ImageView largeView = new ImageView(new Image(photoFile.toURI().toString()));
            largeView.setFitWidth(160);
            largeView.setFitHeight(160);
            largeView.setPreserveRatio(false);
            Circle clip = new Circle(80, 80, 80);
            largeView.setClip(clip);
            photoStack.getChildren().add(largeView);
        } else {
            Circle placeholder = new Circle(80, Color.web("#c4b5fd"));
            photoStack.getChildren().add(placeholder);
        }

        Stage popup = new Stage();
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);

        // "+" edit indicator built from Circle + Label
        StackPane plusBtn = new StackPane();
        plusBtn.setMaxSize(40, 40);
        plusBtn.setMinSize(40, 40);
        plusBtn.setStyle("-fx-cursor: hand;");

        Circle plusCircle = new Circle(20);
        plusCircle.setFill(Color.web("#6d28d9"));
        plusCircle.setStroke(Color.WHITE);
        plusCircle.setStrokeWidth(2);

        Label plusText = new Label("+");
        plusText.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        plusBtn.getChildren().addAll(plusCircle, plusText);

        StackPane.setAlignment(plusBtn, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(plusBtn, new Insets(0, 4, 4, 0));

        plusBtn.setOnMouseClicked(e -> {
            popup.close();
            if (onEditRequested != null) onEditRequested.run();
        });
        photoStack.getChildren().add(plusBtn);

        // Name
        Label nameLabel = new Label(info.getFullName());
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Detail rows
        VBox detailsBox = new VBox(10);
        detailsBox.setAlignment(Pos.CENTER);
        detailsBox.setPadding(new Insets(10, 0, 0, 0));
        detailsBox.getChildren().addAll(
                buildDetailRow("Username", info.getUsername()),
                buildDetailRow("Department", info.getDepartment()),
                buildDetailRow("Role", info.getRole()),
                buildDetailRow("Salary", info.getSalary())
        );

        contentBox.getChildren().addAll(photoStack, nameLabel, detailsBox);

        // The visible card itself
        StackPane card = new StackPane(contentBox);
        card.setStyle("-fx-background-color: rgba(0,0,0,0.85); -fx-background-radius: 16px;");
        card.setMaxSize(420, 480);
        card.setPrefSize(420, 480);

        // Full-window transparent overlay so clicks anywhere outside the card are caught
        StackPane overlay = new StackPane(card);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.35);");

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) {
                popup.close();
            }
        });

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);
        popup.setScene(scene);

        // Size and position the popup to exactly cover the owner window
        popup.setX(owner.getX());
        popup.setY(owner.getY());
        popup.setWidth(owner.getWidth());
        popup.setHeight(owner.getHeight());

        popup.show();
    }

    private static VBox buildDetailRow(String label, String value) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        Label labelText = new Label(label);
        labelText.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 11px;");
        Label valueText = new Label(value);
        valueText.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        box.getChildren().addAll(labelText, valueText);
        return box;
    }
}