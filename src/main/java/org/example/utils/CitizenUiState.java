package org.example.utils;

/**
 * Etat UI partage pour les vues citoyen.
 */
public final class CitizenUiState {

    private static String placeholderTitle = "Section";
    private static Integer selectedDeclarationId;
    private static boolean returnFromDetailToMyDeclarations;

    private CitizenUiState() {
    }

    public static void setPlaceholderTitle(String title) {
        placeholderTitle = title != null && !title.isBlank() ? title : "Section";
    }

    public static String getPlaceholderTitle() {
        return placeholderTitle;
    }

    public static Integer getSelectedDeclarationId() {
        return selectedDeclarationId;
    }

    public static void setSelectedDeclarationId(Integer id) {
        selectedDeclarationId = id;
    }

    public static void setReturnFromDetailToMyDeclarations(boolean value) {
        returnFromDetailToMyDeclarations = value;
    }

    public static boolean consumeReturnFromDetailToMyDeclarations() {
        boolean value = returnFromDetailToMyDeclarations;
        returnFromDetailToMyDeclarations = false;
        return value;
    }

    public static void clear() {
        placeholderTitle = "Section";
        selectedDeclarationId = null;
        returnFromDetailToMyDeclarations = false;
    }
}
