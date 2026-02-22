module com.example.studyhub {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.studyhub to javafx.fxml;
    exports com.example.studyhub;
}