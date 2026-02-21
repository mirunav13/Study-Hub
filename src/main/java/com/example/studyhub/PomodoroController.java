package com.example.studyhub;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.util.Duration;

public class PomodoroController {

    @FXML private Label timerLabel;
    @FXML private Label xpLabel;
    @FXML private Label levelLabel;
    @FXML private ProgressBar levelProgressBar;
    @FXML private TextField inputMinute;

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
        totalXp += earnedXp;
        currentLevel = (totalXp / XP_FOR_NEW_LEVEL) + 1;

        updateGamificationDisplay();
        System.out.println("Session complete! You received " + earnedXp + " XP.");
    }

    private void updateTimerLabel() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateGamificationDisplay() {
        int currentLevelXp = totalXp % XP_FOR_NEW_LEVEL;
        double barProgress = (double) currentLevelXp / XP_FOR_NEW_LEVEL;

        if (xpLabel != null) xpLabel.setText("XP: " + currentLevelXp + " / " + XP_FOR_NEW_LEVEL);
        if (levelLabel != null) levelLabel.setText("Level: " + currentLevel);
        if (levelProgressBar != null) levelProgressBar.setProgress(barProgress);
    }
}