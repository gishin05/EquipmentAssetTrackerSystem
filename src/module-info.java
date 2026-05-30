module EquipmentAssetTrackerSystem {
    requires java.sql;
    requires javafx.controls;
    requires transitive javafx.graphics;
    requires javafx.base;

    opens tracker to javafx.graphics;
    opens tracker.ui to javafx.base;
    opens tracker.models to javafx.base;

    exports tracker;
    exports tracker.ui;
    exports tracker.models;
}