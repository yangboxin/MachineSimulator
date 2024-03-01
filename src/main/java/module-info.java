module panel2.panel1 {
    requires javafx.controls;
    requires javafx.fxml;


    opens panel2.panel1 to javafx.fxml;
    exports panel2.panel1;
}