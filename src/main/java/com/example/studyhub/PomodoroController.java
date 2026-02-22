package com.example.studyhub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.media.AudioClip;
import java.net.URL;

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
        DatabaseManager.initDB();

        totalXp = DatabaseManager.loadXP();
        currentLevel = (totalXp / XP_FOR_NEW_LEVEL) + 1;

        updateTimerLabel();
        updateGamificationDisplay();

        loadSavedTasks();
    }


    private void loadSavedTasks() {
        for (String taskName : DatabaseManager.loadTasksByStatus("TODO")) {
            createTaskCard(taskName, todoColumn, "TODO");
        }
        for (String taskName : DatabaseManager.loadTasksByStatus("IN_PROGRESS")) {
            createTaskCard(taskName, inProgressColumn, "IN_PROGRESS");
        }
        for (String taskName : DatabaseManager.loadTasksByStatus("DONE")) {
            createTaskCard(taskName, doneColumn, "DONE");
        }
    }

    @FXML
    public void addTask() {
        String taskText = taskInput.getText().trim();
        if (!taskText.isEmpty()) {
            createTaskCard(taskText, todoColumn, "TODO");
            DatabaseManager.saveTask(taskText, "TODO");
            taskInput.clear();
        }
    }

    private void createTaskCard(String text, VBox column, String status) {
        Button taskCard = new Button(text);

        if (status.equals("TODO")) {
            taskCard.setStyle("-fx-background-color: white; -fx-border-color: #cfd8dc; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
            taskCard.setOnAction(event -> moveTask(taskCard));
        } else if (status.equals("IN_PROGRESS")) {
            taskCard.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #ffb74d; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
            taskCard.setOnAction(event -> moveTask(taskCard));
        } else if (status.equals("DONE")) {
            taskCard.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #81c784; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left;");
            taskCard.setDisable(true);
        }

        column.getChildren().add(taskCard);
    }

    private void moveTask(Button taskCard) {
        String taskName = taskCard.getText();

        if (todoColumn.getChildren().contains(taskCard)) {
            todoColumn.getChildren().remove(taskCard);
            inProgressColumn.getChildren().add(taskCard);
            taskCard.setStyle("-fx-background-color: #fff9c4; -fx-border-color: #ffb74d; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
            DatabaseManager.updateTaskStatus(taskName, "IN_PROGRESS");
        }
        else if (inProgressColumn.getChildren().contains(taskCard)) {
            inProgressColumn.getChildren().remove(taskCard);
            todoColumn.getChildren().add(taskCard);
            taskCard.setStyle("-fx-background-color: white; -fx-border-color: #cfd8dc; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left; -fx-cursor: hand;");
            DatabaseManager.updateTaskStatus(taskName, "TODO");
        }
    }

    private int completeInProgressTasks() {
        List<Node> tasksToComplete = new ArrayList<>(inProgressColumn.getChildren());
        int tasksCompleted = 0;

        for (Node node : tasksToComplete) {
            Button taskCard = (Button) node;
            String taskName = taskCard.getText();

            inProgressColumn.getChildren().remove(taskCard);
            doneColumn.getChildren().add(taskCard);

            taskCard.setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #81c784; -fx-border-radius: 3; -fx-padding: 10; -fx-pref-width: 230; -fx-alignment: center-left;");
            taskCard.setDisable(true);

            DatabaseManager.updateTaskStatus(taskName, "DONE");

            tasksCompleted++;
            System.out.println("Task automatically completed: " + taskName);
        }

        int taskXp = tasksCompleted * 20;
        if (taskXp > 0) {
            addXp(taskXp);
        }

        return taskXp;
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
                int taskXp = completeInProgressTasks();
                grantReward(taskXp);
                playNotificationSound();
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

    private void updateTimerLabel() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void grantReward(int taskXp) {
        int timeXp = currentSessionMinutes * 5;
        addXp(timeXp);

        int totalSessionXp = timeXp + taskXp;

        System.out.println("Session complete! You received a total of " + totalSessionXp + " XP (" + timeXp + " for time, " + taskXp + " for tasks).");
    }

    private void playNotificationSound() {
        try {
            URL resource = getClass().getResource("notification.mp3");

            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                clip.play();
            } else {
                System.out.println("Audio file not found! Make sure it is in the correct folder.");
            }
        } catch (Exception e) {
            System.out.println("Error playing sound: " + e.getMessage());
        }
    }

    private void addXp(int amount) {
        totalXp += amount;
        currentLevel = (totalXp / XP_FOR_NEW_LEVEL) + 1;

        DatabaseManager.saveXP(totalXp);

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