package com.example.studyhub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;

public class PomodoroController {

    @FXML private Label timerLabel;
    @FXML private Label xpLabel;
    @FXML private Label levelLabel;
    @FXML private ProgressBar levelProgressBar;
    @FXML private TextField inputMinute;

    @FXML private VBox todoColumn;
    @FXML private VBox inProgressColumn;
    @FXML private VBox doneColumn;
    @FXML private TextField taskInput;

    private Timeline timeline;
    private int timeLeft = 25 * 60;
    private int currentSessionMinutes = 25;

    private int totalXp = 0;
    private int currentLevel = 1;
    private final int XP_FOR_NEW_LEVEL = 100;

    @FXML
    public void initialize() {
        updateTimerLabel();
        updateGamificationDisplay();
    }


    @FXML
    public void addTask() {
        String taskText = taskInput.getText().trim();

        if (!taskText.isEmpty()) {
            Button taskCard = new Button(taskText);

            taskCard.setStyle("-fx-background-color: white; -fx-border-color: #cfd8dc; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");

            taskCard.setOnAction(event -> moveTask(taskCard));

            todoColumn.getChildren().add(taskCard);

            taskInput.clear();
        }
    }

    private void moveTask(Button taskCard) {
        if (todoColumn.getChildren().contains(taskCard)) {
            todoColumn.getChildren().remove(taskCard);
            inProgressColumn.getChildren().add(taskCard);
            taskCard.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #ffb74d; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
        }
        else if (inProgressColumn.getChildren().contains(taskCard)) {
            inProgressColumn.getChildren().remove(taskCard);
            todoColumn.getChildren().add(taskCard);
            taskCard.setStyle("-fx-background-color: white; -fx-border-color: #cfd8dc; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
        }
    }

    private void completeInProgressTasks() {
        List<Node> tasksToComplete = new ArrayList<>(inProgressColumn.getChildren());

        for (Node node : tasksToComplete) {
            Button taskCard = (Button) node;

            inProgressColumn.getChildren().remove(taskCard);
            doneColumn.getChildren().add(taskCard);

            taskCard.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #81c784; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left;");
            taskCard.setDisable(true);

            addXp(20);
            System.out.println("Task automatically completed: " + taskCard.getText());
        }
    }

    @FXML
    public void setTime() {
        if (timeline != null) timeline.stop();
        try {
            int inputMinutes = Integer.parseInt(inputMinute.getText());
            if (inputMinutes > 0) {
                currentSessionMinutes = inputMinutes;
                timeLeft = currentSessionMinutes * 60;
                updateTimerLabel();
                inputMinute.clear();
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Please enter a valid number!");
        }
    }

    @FXML
    public void startTimer() {
        if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) return;

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            timeLeft--;
            updateTimerLabel();

            if (timeLeft <= 0) {
                timeline.stop();
                completeInProgressTasks();
                grantReward();
            }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    public void pauseTimer() {
        if (timeline != null) timeline.pause();
    }

    @FXML
    public void resetTimer() {
        if (timeline != null) timeline.stop();
        timeLeft = currentSessionMinutes * 60;
        updateTimerLabel();
    }

    private void grantReward() {
        int earnedXp = currentSessionMinutes * 5;

        addXp(earnedXp);

        System.out.println("Session complete! You received " + earnedXp + " XP for the time spent.");
    }

    private void updateTimerLabel() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void addXp(int amount) {
        totalXp += amount;
        currentLevel = (totalXp / XP_FOR_NEW_LEVEL) + 1;
        updateGamificationDisplay();
    }

    private void updateGamificationDisplay() {
        int currentLevelXp = totalXp % XP_FOR_NEW_LEVEL;
        double barProgress = (double) currentLevelXp / XP_FOR_NEW_LEVEL;

        if (xpLabel != null) xpLabel.setText("XP: " + currentLevelXp + " / " + XP_FOR_NEW_LEVEL);
        if (levelLabel != null) levelLabel.setText("Level: " + currentLevel);
        if (levelProgressBar != null) levelProgressBar.setProgress(barProgress);
    }
}