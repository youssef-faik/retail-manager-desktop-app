module com.example.salesmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires tradukisto;
    requires jasperreports;
    requires de.jensd.fx.glyphs.fontawesome;
    requires jakarta.persistence;
    requires org.hibernate.orm.core;
    requires java.naming;
    requires java.desktop;


    opens com.example.salesmanagement to javafx.fxml;
    exports com.example.salesmanagement;
    opens com.example.salesmanagement.client to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.client;
    opens com.example.salesmanagement.supplier to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.supplier;
    opens com.example.salesmanagement.product to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.product;
    opens com.example.salesmanagement.category to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.category;
    opens com.example.salesmanagement.taxrate to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.taxrate;
    opens com.example.salesmanagement.document to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.document;
    opens com.example.salesmanagement.report to javafx.fxml;
    exports com.example.salesmanagement.report;
    opens com.example.salesmanagement.payment to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.payment;
    opens com.example.salesmanagement.configuration to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.configuration;
    opens com.example.salesmanagement.stockmouvement to javafx.fxml, org.hibernate.orm.core;
    exports com.example.salesmanagement.stockmouvement;

}