package org.example.utils;

import org.example.entities.DeclarationDechet;
import org.example.entities.TypeDechet;

public final class AdminUiState {

    private static TypeDechet selectedTypeDechet;
    private static DeclarationDechet selectedDeclaration;
    private static String flashMessage;
    private static boolean flashError;

    private AdminUiState() {
    }

    public static TypeDechet getSelectedTypeDechet() {
        return selectedTypeDechet;
    }

    public static void setSelectedTypeDechet(TypeDechet selectedTypeDechet) {
        AdminUiState.selectedTypeDechet = selectedTypeDechet;
    }

    public static DeclarationDechet getSelectedDeclaration() {
        return selectedDeclaration;
    }

    public static void setSelectedDeclaration(DeclarationDechet selectedDeclaration) {
        AdminUiState.selectedDeclaration = selectedDeclaration;
    }

    public static void setFlash(String message, boolean error) {
        flashMessage = message;
        flashError = error;
    }

    public static String consumeFlashMessage() {
        String message = flashMessage;
        flashMessage = null;
        return message;
    }

    public static boolean consumeFlashError() {
        boolean error = flashError;
        flashError = false;
        return error;
    }
}
