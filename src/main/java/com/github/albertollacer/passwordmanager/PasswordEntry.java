package com.github.albertollacer.passwordmanager;

import java.io.Serializable;

public class PasswordEntry implements Serializable {
    private String service;
    private String encryptedPassword;

    public PasswordEntry(String service, String encryptedPassword) {
        this.service = service;
        this.encryptedPassword = encryptedPassword;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    private boolean showPlainPassword = false;

    public void togglePasswordVisibility() {
        showPlainPassword = !showPlainPassword;
    }

    public String getPasswordDisplay(PasswordFileManager manager) {
        if(showPlainPassword) {
            return manager.decrypt(this.encryptedPassword);
        } else {
            return this.encryptedPassword;
        }
    }

    public boolean isPasswordVisible() {
        return showPlainPassword;
    }

}