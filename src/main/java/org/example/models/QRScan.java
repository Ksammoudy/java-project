package org.example.models;

import java.time.LocalDateTime;

public class QRScan {
    private int id;
    private int zoneId;
    private LocalDateTime scannedAt;
    private String ipAddress;
    private String country;

    public QRScan() {}

    public QRScan(int zoneId, String ipAddress, String country) {
        this.zoneId = zoneId;
        this.scannedAt = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.country = country;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getZoneId() { return zoneId; }
    public void setZoneId(int zoneId) { this.zoneId = zoneId; }

    public LocalDateTime getScannedAt() { return scannedAt; }
    public void setScannedAt(LocalDateTime scannedAt) { this.scannedAt = scannedAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}