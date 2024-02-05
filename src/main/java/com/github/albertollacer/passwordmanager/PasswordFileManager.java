package com.github.albertollacer.passwordmanager;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class PasswordFileManager {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES";
    private SecretKey secretKey;

    public PasswordFileManager() {
        // Generar llave secreta AES
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256, new SecureRandom());
            secretKey = keyGenerator.generateKey();
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Error al inicializar el gestor de contraseñas", e);
        }
    }



    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace(); 
            return null;
        }
    }
    



    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedBytes);
        } catch (GeneralSecurityException e) {
            e.printStackTrace(); 
            return null;
        }
    }

    public void savePassword(String password, String filePath) throws GeneralSecurityException, IOException {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(password.getBytes());

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] encodedKey = Base64.getEncoder().encode(secretKey.getEncoded());
            outputStream.write(encodedKey);
            outputStream.write("\n".getBytes());
            outputStream.write(Base64.getEncoder().encode(encryptedBytes));
        }
    }

    public String loadPassword(String filePath) throws GeneralSecurityException, IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        String[] lines = content.split("\n");

        byte[] decodedKey = Base64.getDecoder().decode(lines[0]);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
        byte[] encryptedBytes = Base64.getDecoder().decode(lines[1]);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, originalKey);

        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes);
    }

    private List<PasswordEntry> passwordEntries = new ArrayList<>();

    public void savePasswordList(File file) throws GeneralSecurityException, IOException {
        // Convertir la lista de entradas a un formato cifrado
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
    
        for (PasswordEntry entry : passwordEntries) {
            objectStream.writeObject(entry);
        }
    
        objectStream.close();
        byteStream.close();
    
        String encryptedList = encrypt(byteStream.toString());
        Files.write(file.toPath(), encryptedList.getBytes());
    }
    
    public void loadPasswordList(File file) throws GeneralSecurityException, IOException, ClassNotFoundException {
        String encryptedContent = new String(Files.readAllBytes(file.toPath()));
        String decryptedContent = decrypt(encryptedContent);
    
        ByteArrayInputStream byteStream = new ByteArrayInputStream(decryptedContent.getBytes());
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
    
        passwordEntries.clear();
        while (byteStream.available() > 0) {
            PasswordEntry entry = (PasswordEntry) objectStream.readObject();
            passwordEntries.add(entry);
        }
    
        objectStream.close();
        byteStream.close();
    }


    public List<PasswordEntry> getPasswordEntries() {
        return passwordEntries;
    }




}