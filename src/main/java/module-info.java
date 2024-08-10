module com.example.gestioncommercial {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires tradukisto;
    requires jasperreports;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;


    opens com.example.gestioncommercial to javafx.fxml;
    exports com.example.gestioncommercial;
    opens com.example.gestioncommercial.client to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.client;
    opens com.example.gestioncommercial.product to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.product;
    opens com.example.gestioncommercial.category to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.category;
    opens com.example.gestioncommercial.taxrate to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.taxrate;
    opens com.example.gestioncommercial.invoice to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.invoice;
    opens com.example.gestioncommercial.report to javafx.fxml;
    exports com.example.gestioncommercial.report;
    opens com.example.gestioncommercial.payment to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.payment;
    opens com.example.gestioncommercial.configuration to javafx.fxml, org.hibernate.orm.core;
    exports com.example.gestioncommercial.configuration;

}