module org.example.wastewise {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.net.http;
    requires jdk.httpserver;
    requires jbcrypt;
    requires jakarta.mail;
    requires jakarta.activation;
    requires mysql.connector.j;

    exports org.example;

    opens org.example to javafx.graphics, javafx.fxml;
    opens org.example.controllers to javafx.fxml;
    opens org.example.models to javafx.base, javafx.fxml;
    opens org.example.entities to javafx.base, javafx.fxml;
}
