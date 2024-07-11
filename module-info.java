module com.example.saltodecavalo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.saltodecavalo to javafx.fxml;
    exports com.example.saltodecavalo;
}