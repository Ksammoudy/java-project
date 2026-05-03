package org.example.services;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.util.Utils;

public class TwoFactorService {

    private static final String ISSUER = "WasteWise";

    // =========================
    // Générer secret
    // =========================
    public static String generateSecret() {
        return new DefaultSecretGenerator().generate();
    }

    // =========================
    // Vérifier code (avec tolérance)
    // =========================
    public static boolean verifyCode(String secret, String code) {

        if (secret == null || code == null) {
            return false;
        }

        // 🔥 Tolérance de temps (important si horloge décalée)
        DefaultCodeVerifier verifier = new DefaultCodeVerifier(
                new DefaultCodeGenerator(),
                new SystemTimeProvider()
        );

        // accepte +/- 1 intervalle (30s)
        verifier.setAllowedTimePeriodDiscrepancy(1);

        return verifier.isValidCode(secret, code);
    }

    // =========================
    // Générer QR Code (bytes)
    // =========================
    public static byte[] generateQrCode(String email, String secret) throws Exception {

        QrData data = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        return generator.generate(data);
    }

    // =========================
    // Générer QR Code en base64 (pour JavaFX ImageView)
    // =========================
    public static String generateQrCodeBase64(String email, String secret) throws Exception {
        byte[] bytes = generateQrCode(email, secret);
        return Utils.getDataUriForImage(bytes, generatorImageType());
    }

    private static String generatorImageType() {
        return "image/png";
    }

    // =========================
    // Générer URL OTP (si pas de QR)
    // =========================
    public static String generateOtpAuthUrl(String email, String secret) {

        return "otpauth://totp/" + ISSUER + ":" + email +
                "?secret=" + secret +
                "&issuer=" + ISSUER +
                "&algorithm=SHA1&digits=6&period=30";
    }
}