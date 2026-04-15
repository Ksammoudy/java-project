package org.example.Controllers;

import org.example.entities.Evenement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class UserController {

    @FXML private TableView<Evenement> tableEvents;

    @FXML private TableColumn<Evenement, Integer> colId;
    @FXML private TableColumn<Evenement, String> colTitre;
    @FXML private TableColumn<Evenement, String> colLieu;
    @FXML private TableColumn<Evenement, String> colDate;

    private ObservableList<Evenement> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {

        // 🔥 data test (بعد تربطها DB)
        list.add(new Evenement(1, "Conference", "AI Talk", "Tunis",
                java.sql.Date.valueOf("2026-04-14"), 1));

        list.add(new Evenement(2, "Hackathon", "Coding", "Sfax",
                java.sql.Date.valueOf("2026-05-01"), 2));

        colId.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getId()).asObject());

        colTitre.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getTitre()));

        colLieu.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLieu()));

        colDate.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getDate().toString()));

        tableEvents.setItems(list);
    }

    // زر Events (اختياري)
    @FXML
    private void showEvents() {
        tableEvents.setItems(list);
    }
}