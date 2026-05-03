package org.example.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import org.example.Main;
import org.example.models.QRScan;
import org.example.models.ZonePolluee;
import org.example.services.QRScanDAO;
import org.example.services.ZonePollueeDAO;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class QRDashboardController {

    @FXML private TableView<QRScan> scanTable;
    @FXML private TableColumn<QRScan, String> colId;
    @FXML private TableColumn<QRScan, String> colZoneName;
    @FXML private TableColumn<QRScan, String> colDate;
    @FXML private TableColumn<QRScan, String> colIp;
    @FXML private TableColumn<QRScan, String> colCountry;

    @FXML private Text totalScansText;
    @FXML private Text uniqueZonesText;
    @FXML private Text avgPerZoneText;

    private QRScanDAO scanDAO = new QRScanDAO();
    private ZonePollueeDAO zoneDAO = new ZonePollueeDAO();
    private ObservableList<QRScan> scanList = FXCollections.observableArrayList();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getId())));

        colZoneName.setCellValueFactory(cellData -> {
            ZonePolluee zone = zoneDAO.getZoneById(cellData.getValue().getZoneId());
            return new SimpleStringProperty(zone != null ? zone.getNomZone() : "Zone inconnue");
        });

        colDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getScannedAt().format(formatter)));
        colIp.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getIpAddress()));
        colCountry.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCountry()));

        loadScans();
    }

    @FXML
    private void refreshScans() {
        loadScans();
    }

    private void loadScans() {
        List<QRScan> scans = scanDAO.getAllScans();
        scanList.setAll(scans);
        scanTable.setItems(scanList);

        int total = scans.size();
        long uniqueZones = scans.stream().map(QRScan::getZoneId).distinct().count();
        double avg = uniqueZones > 0 ? (double) total / uniqueZones : 0;

        totalScansText.setText(String.valueOf(total));
        uniqueZonesText.setText(String.valueOf(uniqueZones));
        avgPerZoneText.setText(String.format("%.1f", avg));
    }

    @FXML private void goToDashboard() { Main.showDashboardAdmin(); }
    @FXML private void goToZones() { Main.showZonePollueeListPage(); }
    @FXML private void goToIndicateurs() { Main.showIndicateurImpactListPage(); }
}