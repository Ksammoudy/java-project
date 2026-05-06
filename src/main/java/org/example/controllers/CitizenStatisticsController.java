package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.example.Main;
import org.example.entities.DeclarationDechet;
import org.example.entities.Transaction;
import org.example.entities.Wallet;
import org.example.models.User;
import org.example.services.DeclarationDechetJdbcService;
import org.example.services.SessionManager;
import org.example.services.TransactionJdbcService;
import org.example.services.WalletJdbcService;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CitizenStatisticsController {

    private final DeclarationDechetJdbcService declarationService = new DeclarationDechetJdbcService();
    private final WalletJdbcService walletService = new WalletJdbcService();
    private final TransactionJdbcService transactionService = new TransactionJdbcService();

    @FXML
    private Button navHome;
    @FXML
    private Button navDeclare;
    @FXML
    private Button navMyDeclarations;
    @FXML
    private Button navStatistics;
    @FXML
    private Button navNews;
    @FXML
    private Button navAir;
    @FXML
    private Button navWithdraw;
    @FXML
    private Button navSettings;

    @FXML
    private Label citizenNameLabel;
    @FXML
    private Label headerEmailLabel;

    @FXML
    private Label walletBalanceLabel;
    @FXML
    private Label totalDeclarationsLabel;
    @FXML
    private Label transactionCountLabel;

    @FXML
    private BarChart<String, Number> typeBarChart;
    @FXML
    private PieChart statusPieChart;
    @FXML
    private LineChart<String, Number> trendLineChart;

    @FXML
    private TableView<TransactionRow> transactionsTable;
    @FXML
    private TableColumn<TransactionRow, String> txDateColumn;
    @FXML
    private TableColumn<TransactionRow, String> txMotifColumn;
    @FXML
    private TableColumn<TransactionRow, String> txTypeColumn;
    @FXML
    private TableColumn<TransactionRow, String> txMontantColumn;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        CitizenSidebarHelper.applyActive(navStatistics,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        configureTxTable();
        loadData();
    }

    private void configureTxTable() {
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        txDateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));
        txMotifColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMotif()));
        txTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        txMontantColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMontant()));
    }

    private void loadData() {
        Integer cid = CitizenSession.resolveCitizenDatabaseId();
        if (cid == null) {
            walletBalanceLabel.setText("—");
            totalDeclarationsLabel.setText("0");
            transactionCountLabel.setText("0");
            clearCharts();
            transactionsTable.setItems(FXCollections.observableArrayList());
            return;
        }

        try {
            List<DeclarationDechet> declarations = declarationService.findByCitoyenId(cid);
            totalDeclarationsLabel.setText(String.valueOf(declarations.size()));

            Wallet wallet = walletService.findByUtilisateurId(cid).orElse(null);
            int balance = wallet != null && wallet.getSoldeActuel() != null ? wallet.getSoldeActuel() : 0;
            walletBalanceLabel.setText(balance + " pts");

            if (wallet != null) {
                List<Transaction> txs = transactionService.findByWalletId(wallet.getId(), 40);
                transactionCountLabel.setText(String.valueOf(txs.size()));
                transactionsTable.setItems(FXCollections.observableArrayList(
                        txs.stream().map(TransactionRow::from).toList()
                ));
            } else {
                transactionCountLabel.setText("0");
                transactionsTable.setItems(FXCollections.observableArrayList());
            }

            populateTypeBar(declarations);
            populateStatusPie(declarations);
            populateTrend(declarations);
        } catch (SQLException | RuntimeException e) {
            walletBalanceLabel.setText("Erreur");
            clearCharts();
        }
    }

    private void clearCharts() {
        typeBarChart.getData().clear();
        statusPieChart.setData(FXCollections.observableArrayList());
        trendLineChart.getData().clear();
    }

    private void populateTypeBar(List<DeclarationDechet> list) {
        Map<String, Long> byType = list.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTypeDechetLibelle() == null ? "Autre" : d.getTypeDechetLibelle(),
                        Collectors.counting()
                ));
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        for (Map.Entry<String, Long> e : byType.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        typeBarChart.getData().setAll(series);
    }

    private void populateStatusPie(List<DeclarationDechet> list) {
        long ap = list.stream().filter(d -> "APPROUVEE".equals(normalizeStatus(d.getStatut()))).count();
        long att = list.stream().filter(d -> "EN_ATTENTE".equals(normalizeStatus(d.getStatut()))).count();
        long ref = list.stream().filter(d -> "REFUSEE".equals(normalizeStatus(d.getStatut()))).count();
        if (ap + att + ref == 0) {
            statusPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Aucune donnee", 1)
            ));
        } else {
            statusPieChart.setData(FXCollections.observableArrayList(
                    new PieChart.Data("Approuvees", ap),
                    new PieChart.Data("En attente", att),
                    new PieChart.Data("Refusees", ref)
            ));
        }
    }

    private void populateTrend(List<DeclarationDechet> list) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Declarations");
        YearMonth now = YearMonth.now();
        Locale fr = Locale.FRENCH;
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            String label = ym.getMonth().getDisplayName(TextStyle.SHORT, fr) + " " + ym.getYear();
            long count = list.stream()
                    .filter(d -> d.getCreatedAt() != null && YearMonth.from(d.getCreatedAt()).equals(ym))
                    .count();
            series.getData().add(new XYChart.Data<>(label, count));
        }
        trendLineChart.getData().setAll(series);
    }

    private static String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "EN_ATTENTE";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    public static class TransactionRow {
        private final String date;
        private final String motif;
        private final String type;
        private final String montant;

        public TransactionRow(String date, String motif, String type, String montant) {
            this.date = date;
            this.motif = motif;
            this.type = type;
            this.montant = montant;
        }

        static TransactionRow from(Transaction t) {
            String d = t.getDateTransaction() == null ? "—" : t.getDateTransaction().toString();
            String m = t.getMontant() == null ? "—" : String.valueOf(t.getMontant());
            return new TransactionRow(
                    d,
                    t.getMotif() != null ? t.getMotif() : "—",
                    t.getType() != null ? t.getType() : "—",
                    m
            );
        }

        public String getDate() {
            return date;
        }

        public String getMotif() {
            return motif;
        }

        public String getType() {
            return type;
        }

        public String getMontant() {
            return montant;
        }
    }

    @FXML
    public void handleDashboard() {
        Main.showDashboardCitizen();
    }

    @FXML
    public void handleDeclareWaste() {
        Main.showDeclarationCitizenFormPage();
    }

    @FXML
    public void handleMyDeclarations() {
        Main.showCitizenMyDeclarationsPage();
    }

    @FXML
    public void handleStatistics() {
        Main.showCitizenStatisticsPage();
    }

    @FXML
    public void handleNews() {
        Main.showCitizenNewsPage();
    }

    @FXML
    public void handleAirQuality() {
        Main.showCitizenAirQualityPage();
    }

    @FXML
    public void handleWithdraw() {
        Main.showCitizenWithdrawPage();
    }

    @FXML
    public void handleProfile() {
        Main.showCitizenSettingsPage();
    }

    @FXML
    public void handleLogout() {
        SessionManager.clearSession();
        Main.showLoginPage();
    }
}
