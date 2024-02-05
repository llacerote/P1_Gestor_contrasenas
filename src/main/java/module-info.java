module com.github.albertollacer.passwordmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    

    opens com.github.albertollacer.passwordmanager to javafx.fxml;
    exports com.github.albertollacer.passwordmanager;
}
