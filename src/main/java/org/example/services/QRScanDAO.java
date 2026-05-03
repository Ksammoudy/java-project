package org.example.services;

import org.example.models.QRScan;
import org.example.utils.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QRScanDAO {

    private DBConnection dbConnection = DBConnection.getInstance();

    public void addScan(QRScan scan) {
        String sql = "INSERT INTO qrscan (zone_id, scanned_at, ip_address, country) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, scan.getZoneId());
            pstmt.setTimestamp(2, Timestamp.valueOf(scan.getScannedAt()));
            pstmt.setString(3, scan.getIpAddress());
            pstmt.setString(4, scan.getCountry());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                scan.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<QRScan> getAllScans() {
        List<QRScan> scans = new ArrayList<>();
        String sql = "SELECT id, zone_id, scanned_at, ip_address, country FROM qrscan ORDER BY scanned_at DESC";
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                QRScan scan = new QRScan();
                scan.setId(rs.getInt("id"));
                scan.setZoneId(rs.getInt("zone_id"));
                scan.setScannedAt(rs.getTimestamp("scanned_at").toLocalDateTime());
                scan.setIpAddress(rs.getString("ip_address"));
                scan.setCountry(rs.getString("country"));
                scans.add(scan);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scans;
    }
}