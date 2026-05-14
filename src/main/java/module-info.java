module org.example.wastewise {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;    // required for netscape.javascript.JSObject
    requires java.net.http;
    requires jdk.httpserver;
    requires jbcrypt;
    requires jakarta.mail;
    requires java.sql;
    requires java.desktop;
    requires jakarta.activation;
    requires mysql.connector.j;
    requires com.google.gson;
    requires opencv;
    requires totp;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires okhttp3;
    requires kernel;

    exports org.example;

    opens org.example to javafx.graphics, javafx.fxml;
    opens org.example.controllers to javafx.fxml;
    opens org.example.controllers.gestionevent to javafx.fxml;
    opens main.controllers to javafx.fxml;
    opens controllers.admin to javafx.fxml;
    opens controllers.appeloffre to javafx.fxml;
    opens controllers.reponseoffre to javafx.fxml;
    opens org.example.models to javafx.base, javafx.fxml;
    opens org.example.entities to javafx.base, javafx.fxml;
}
