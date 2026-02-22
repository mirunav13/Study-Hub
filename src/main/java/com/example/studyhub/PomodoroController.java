package com.example.studyhub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
        for (String taskName : DatabaseManager.loadTasksByStatus("TODO")) createTaskCard(taskName, todoColumn, "TODO");
        for (String taskName : DatabaseManager.loadTasksByStatus("IN_PROGRESS")) createTaskCard(taskName, inProgressColumn, "IN_PROGRESS");
        for (String taskName : DatabaseManager.loadTasksByStatus("DONE")) createTaskCard(taskName, doneColumn, "DONE");
    }

    @FXML
    public void addTask() {
        String taskText = taskInput.getText().trim();

        if (!taskText.isEmpty()) {
            boolean alreadyExists = checkIfTaskExists(taskText);

            if (!alreadyExists) {
                createTaskCard(taskText, todoColumn, "TODO");
                DatabaseManager.updateTaskStatus(taskText, "TODO");
                taskInput.clear();
                taskInput.setPromptText("Enter a new task...");
            } else {
                taskInput.clear();
                taskInput.setPromptText("Task already exists!");
            }
        }
    }

    private boolean checkIfTaskExists(String taskName) {
        for (Node node : todoColumn.getChildren()) {
            if (((Button) node).getText().equals(taskName)) return true;
        }
        for (Node node : inProgressColumn.getChildren()) {
            if (((Button) node).getText().equals(taskName)) return true;
        }
        for (Node node : doneColumn.getChildren()) {
            if (((Button) node).getText().equals(taskName)) return true;
        }
        return false;
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
        } else if (inProgressColumn.getChildren().contains(taskCard)) {
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
        }

        if (tasksCompleted > 0) {
            int taskXp = tasksCompleted * 20;
            addXp(taskXp);

            DatabaseManager.addCompletedTaskCount(tasksCompleted);
            checkAchievements();

            return taskXp;
        }
        return 0;
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
        } catch (NumberFormatException ignored) {}
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
    public void pauseTimer() { if (timeline != null) timeline.pause(); }

    @FXML
    public void resetTimer() {
        if (timeline != null) timeline.stop();
        timeLeft = currentSessionMinutes * 60;
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        timerLabel.setText(String.format("%02d:%02d", timeLeft / 60, timeLeft % 60));
    }

    private void grantReward(int taskXp) {
        int timeXp = currentSessionMinutes * 5;
        addXp(timeXp);

        DatabaseManager.addStudiedMinutes(currentSessionMinutes);
        checkAchievements();

        System.out.println("Session complete! Earned " + (timeXp + taskXp) + " XP.");
    }

    private void addXp(int amount) {
        totalXp += amount;
        currentLevel = (totalXp / XP_FOR_NEW_LEVEL) + 1;
        DatabaseManager.saveXP(totalXp);
        updateGamificationDisplay();
    }

    private void updateGamificationDisplay() {
        int currentLevelXp = totalXp % XP_FOR_NEW_LEVEL;
        if (xpLabel != null) xpLabel.setText("XP: " + currentLevelXp + " / " + XP_FOR_NEW_LEVEL);
        if (levelLabel != null) levelLabel.setText("Level: " + currentLevel);
        if (levelProgressBar != null) levelProgressBar.setProgress((double) currentLevelXp / XP_FOR_NEW_LEVEL);
    }

    private void playNotificationSound() {
        try {
            URL resource = getClass().getResource("notification.mp3");
            if (resource != null) new AudioClip(resource.toString()).play();
        } catch (Exception ignored) {}
    }


    private void checkAchievements() {
        int totalMins = DatabaseManager.getStat("total_minutes");
        int totalTasks = DatabaseManager.getStat("total_tasks");

        if (totalMins > 0 && !DatabaseManager.isAchievementUnlocked("First Study Session")) {
            unlockAndNotify("First Study Session", "You completed your very first Pomodoro session!");
        }
        if (totalMins >= 120 && !DatabaseManager.isAchievementUnlocked("Marathon")) {
            unlockAndNotify("Marathon", "You have studied for over 2 hours in total. Incredible focus!");
        }
        if (totalMins >= 500 && !DatabaseManager.isAchievementUnlocked("Deep Work")) {
            unlockAndNotify("Deep Work", "500 minutes of pure focus. Outstanding!");
        }
        if (totalMins >= 1000 && !DatabaseManager.isAchievementUnlocked("Time Lord")) {
            unlockAndNotify("Time Lord", "1000 minutes! You control time itself.");
        }

        if (totalTasks >= 10 && !DatabaseManager.isAchievementUnlocked("Task Master")) {
            unlockAndNotify("Task Master", "You completed 10 tasks. You are a productivity machine!");
        }
        if (totalTasks >= 50 && !DatabaseManager.isAchievementUnlocked("Productivity Ninja")) {
            unlockAndNotify("Productivity Ninja", "50 tasks completed. So fast, so quiet.");
        }
        if (totalTasks >= 100 && !DatabaseManager.isAchievementUnlocked("Task Annihilator")) {
            unlockAndNotify("Task Annihilator", "100 tasks done. Nothing stands in your way.");
        }

        if (currentLevel >= 5 && !DatabaseManager.isAchievementUnlocked("Rising Star")) {
            unlockAndNotify("Rising Star", "You reached Level 5! Keep growing.");
        }
        if (currentLevel >= 10 && !DatabaseManager.isAchievementUnlocked("Scholar")) {
            unlockAndNotify("Scholar", "Level 10 achieved. A true master of study.");
        }
    }

    private void unlockAndNotify(String title, String description) {
        DatabaseManager.unlockAchievement(title);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Achievement Unlocked!");
        alert.setHeaderText("🏆 " + title);
        alert.setContentText(description);
        alert.show();
    }

    @FXML
    public void showAchievementsDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Your Achievements");
        alert.setHeaderText("🏆 Badges & Milestones");
        alert.setContentText(DatabaseManager.getAllAchievementsText());
        alert.showAndWait();
    }
}