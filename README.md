# Smart Study Hub

A gamified productivity and task management application built with JavaFX. This desktop app combines a customizable Pomodoro timer with an integrated Kanban board to help users stay focused and track their progress over time.

## Key Features

* **Integrated Kanban Board**: Manage tasks through "To Do", "In Progress", and "Done" columns.
* **Smart Pomodoro Timer**: Tasks placed in the "In Progress" column are automatically marked as "Done" when the focus timer successfully completes.
* **Gamification System**: Earn Experience Points (XP) for every minute of focus and every task completed. Level up as you accumulate XP.
* **Achievement Badges**: Unlock persistent milestones based on total study time and total tasks completed (e.g., "Marathon", "Task Annihilator", "Scholar").
* **Audio Notifications**: Audio alerts notify you exactly when a study session concludes.
* **Local Data Persistence**: All tasks, XP, levels, and achievements are securely saved locally using an embedded SQLite database.

## Tech Stack

* **Language**: Java
* **GUI Framework**: JavaFX
* **Database**: SQLite (via JDBC)
* **Build Tool**: Maven

## Prerequisites

To run this project locally, you need:
* Java Development Kit (JDK) 17 or higher
* Maven installed and configured
