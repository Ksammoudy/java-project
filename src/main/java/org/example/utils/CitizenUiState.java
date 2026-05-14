package org.example.utils;

/**
 * Etat transitoire pour les ecrans citoyen (placeholder, retour navigation detail).
 */
public final class CitizenUiState {

    private static String placeholderTitle = "Section";
    private static boolean detailBackToMyDeclarations;

    private CitizenUiState() {
    }

    public static void setPlaceholderTitle(String title) {
        placeholderTitle = title != null && !title.isBlank() ? title : "Section";
    }

    public static String getPlaceholderTitle() {
        return placeholderTitle;
    }

    public static void setReturnFromDetailToMyDeclarations(boolean value) {
        detailBackToMyDeclarations = value;
    }

    public static boolean consumeReturnFromDetailToMyDeclarations() {
        boolean v = detailBackToMyDeclarations;
        detailBackToMyDeclarations = false;
        return v;
    }
}
