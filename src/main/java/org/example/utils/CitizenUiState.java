package org.example.utils;

/**
 * État UI partagé pour les vues citoyen.
 */
public class CitizenUiState {

    private static Integer selectedDeclarationId;
    private static boolean returnFromDetailToMyDeclarations = false;

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
        boolean val = returnFromDetailToMyDeclarations;
        returnFromDetailToMyDeclarations = false;
        return val;
    }

    public static void clear() {
        selectedDeclarationId = null;
        returnFromDetailToMyDeclarations = false;
    }
}
