module com.example.gestioncommercial {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.gestioncommercial to javafx.fxml;
    exports com.example.gestioncommercial;
    opens com.example.gestioncommercial.client to javafx.fxml;
    exports com.example.gestioncommercial.client;
    opens com.example.gestioncommercial.product to javafx.fxml;
    exports com.example.gestioncommercial.product;
}