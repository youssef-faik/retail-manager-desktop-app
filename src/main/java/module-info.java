module com.example.gestioncommercial {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires tradukisto;
    requires jasperreports;


    opens com.example.gestioncommercial to javafx.fxml;
    exports com.example.gestioncommercial;
    opens com.example.gestioncommercial.client to javafx.fxml;
    exports com.example.gestioncommercial.client;
    opens com.example.gestioncommercial.product to javafx.fxml;
    exports com.example.gestioncommercial.product;
    opens com.example.gestioncommercial.invoice to javafx.fxml;
    exports com.example.gestioncommercial.invoice;
    exports com.example.gestioncommercial.report;
    opens com.example.gestioncommercial.report to javafx.fxml;
}