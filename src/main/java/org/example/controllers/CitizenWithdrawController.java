package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.example.Main;
import org.example.entities.Transaction;
import org.example.entities.Wallet;
import org.example.models.User;
import org.example.services.SessionManager;
import org.example.services.StripeService;
import org.example.services.TransactionJdbcService;
import org.example.services.WalletJdbcService;
import org.example.utils.CitizenSession;
import org.example.utils.CitizenSidebarHelper;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Retrait de points EcoPoints (Stripe test + mise a jour wallet local).
 */
public class CitizenWithdrawController {

    private final WalletJdbcService walletService = new WalletJdbcService();
    private final TransactionJdbcService transactionService = new TransactionJdbcService();
    private final StripeService stripeService = new StripeService();

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
    private Label balanceLabel;
    @FXML
    private TextField amountField;
    @FXML
    private Label messageLabel;
    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        User user = CitizenSession.ensureCitizenUser();
        citizenNameLabel.setText(CitizenSession.fullName(user));
        headerEmailLabel.setText(user.getEmail() != null ? user.getEmail() : "—");

        CitizenSidebarHelper.applyActive(navWithdraw,
                navHome, navDeclare, navMyDeclarations, navStatistics, navNews, navAir, navWithdraw, navSettings);

        refreshBalance();
        messageLabel.setText("");
    }

    private void refreshBalance() {
        Integer cid = CitizenSession.resolveCitizenDatabaseId();
        if (cid == null) {
            balanceLabel.setText("—");
            submitButton.setDisable(true);
            messageLabel.setText("Aucun citoyen resolu en base.");
            return;
        }
        try {
            Wallet w = walletService.syncCitizenWalletPoints(cid);
            if (w == null || w.getSoldeActuel() == null) {
                balanceLabel.setText("0 pts");
            } else {
                balanceLabel.setText(w.getSoldeActuel() + " pts");
            }
            submitButton.setDisable(false);
        } catch (SQLException | RuntimeException e) {
            balanceLabel.setText("Erreur");
            messageLabel.setText("DB indisponible.");
        }
    }

    @FXML
    public void handleSubmit() {
        messageLabel.setText("");
        messageLabel.getStyleClass().removeAll("error-text", "success-text");
        User currentUser = CitizenSession.ensureCitizenUser();

        Integer cid = CitizenSession.resolveCitizenDatabaseId();
        if (cid == null) {
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText("Compte citoyen introuvable.");
            return;
        }

        String raw = amountField != null ? amountField.getText() : "";
        int amount;
        try {
            amount = Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText("Saisissez un montant entier valide (points).");
            return;
        }
        if (amount <= 0) {
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText("Le montant doit etre superieur a zero.");
            return;
        }

        try {
            Wallet wallet = walletService.syncCitizenWalletPoints(cid);
            if (wallet == null) {
                messageLabel.getStyleClass().add("error-text");
                messageLabel.setText("Aucun portefeuille wallet pour ce compte.");
                return;
            }
            int balance = wallet.getSoldeActuel() != null ? wallet.getSoldeActuel() : 0;
            if (amount > balance) {
                messageLabel.getStyleClass().add("error-text");
                messageLabel.setText("Solde insuffisant.");
                return;
            }

            int amountMinor = amount; // 100 points = 1 USD => 1 point = 1 cent.
            StripeService.Result payout = stripeService.createPayout(
                    currentUser,
                    amountMinor,
                    "Retrait WasteWise de " + (amount / 100.0)
            );
            if (!payout.success()) {
                StripeService.Result onboarding = stripeService.openOnboarding(currentUser);
                if (onboarding.success()) {
                    messageLabel.getStyleClass().add("error-text");
                    messageLabel.setText("Compte Stripe a connecter. Finalisez l'onboarding puis reessayez.");
                } else {
                    messageLabel.getStyleClass().add("error-text");
                    messageLabel.setText(payout.message());
                }
                return;
            }

            wallet.setSoldeActuel(balance - amount);
            wallet.setDateMj(LocalDateTime.now());
            if (!walletService.update(wallet)) {
                messageLabel.getStyleClass().add("error-text");
                messageLabel.setText("Mise a jour du solde impossible.");
                return;
            }

            Transaction tx = new Transaction();
            tx.setWalletId(wallet.getId());
            tx.setMontant(-amount);
            tx.setType("Depense");
            tx.setMotif("Retrait Stripe #" + (payout.payoutId() == null ? "-" : payout.payoutId()));
            tx.setDateTransaction(LocalDateTime.now());
            transactionService.create(tx);

            amountField.clear();
            messageLabel.getStyleClass().add("success-text");
            messageLabel.setText("Retrait valide: " + amount + " points convertis en " + (amount / 100.0) + " " + stripeService.payoutCurrency() + ".");
            refreshBalance();
        } catch (SQLException | RuntimeException e) {
            messageLabel.getStyleClass().add("error-text");
            messageLabel.setText("Erreur : DB indisponible.");
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
