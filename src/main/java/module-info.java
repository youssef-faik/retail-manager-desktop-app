module com.example.gestioncommercial {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.gestioncommercial to javafx.fxml;
    exports com.example.gestioncommercial;
}