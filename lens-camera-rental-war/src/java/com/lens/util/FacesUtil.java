package com.lens.util;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

public class FacesUtil {

    private FacesUtil() {
    }

    public static void addFieldError(String clientId, String message) {
        FacesContext.getCurrentInstance().addMessage(clientId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public static void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }
}
