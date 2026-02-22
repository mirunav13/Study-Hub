module com.example.studyhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.media;


    opens com.example.studyhub to javafx.fxml;
    exports com.example.studyhub;
}