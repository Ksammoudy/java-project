package controllers.reponseoffre;

public final class ReponseOffreFlowState {

    private static Integer selectedReponseId;
    private static String flashMessage;

    private ReponseOffreFlowState() {
    }

    public static void setSelectedReponseId(Integer id) {
        selectedReponseId = id;
    }

    public static Integer consumeSelectedReponseId() {
        Integer id = selectedReponseId;
        selectedReponseId = null;
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
