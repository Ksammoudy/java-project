package controllers.appeloffre;

public final class AppelOffreFlowState {

    private static Integer selectedAppelId;
    private static String flashMessage;

    private AppelOffreFlowState() {
    }

    public static void setSelectedAppelId(Integer id) {
        selectedAppelId = id;
    }

    public static Integer consumeSelectedAppelId() {
        Integer id = selectedAppelId;
        selectedAppelId = null;
        return id;
    }

    public static void setFlashMessage(String message) {
        flashMessage = message;
    }

    public static String consumeFlashMessage() {
        String msg = flashMessage;
        flashMessage = null;
        return msg;
    }
}
