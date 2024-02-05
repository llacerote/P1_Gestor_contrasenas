package com.github.albertollacer.passwordmanager;

import java.util.Random;

public class PasswordGenerator {
    public String generatePassword(int length, boolean useNumbers, boolean useLetters, boolean useUppercase, boolean useSpecialChars) {
        String numberChars = "0123456789";
        String letterChars = "abcdefghijklmnopqrstuvwxyz";
        String upperCaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String specialChars = "!@#$%^&*()_+";
        StringBuilder characterSet = new StringBuilder();
        if (useNumbers) characterSet.append(numberChars);
        if (useLetters) characterSet.append(letterChars);
        if (useUppercase) characterSet.append(upperCaseChars);
        if (useSpecialChars) characterSet.append(specialChars);

        Random random = new Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(characterSet.length());
            password.append(characterSet.charAt(randomIndex));
        }
        return password.toString();
    }
}
