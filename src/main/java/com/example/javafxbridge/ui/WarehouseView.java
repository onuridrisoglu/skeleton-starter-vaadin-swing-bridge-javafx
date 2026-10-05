package com.example.javafxbridge.ui;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.swingbridge.javafx.JavaFXBridge;

@Route(value = "")
public class WarehouseView extends VerticalLayout {
    private static final String MAIN_CLASS = "com.example.warehouse.fx.WarehouseApp";

    public WarehouseView() {
        setSizeFull();
        JavaFXBridge app = new JavaFXBridge(MAIN_CLASS);
        add(app);
        expand(app);
    }

}
