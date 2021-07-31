package socialnetwork.repository.database;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashing {
    public static String PasswordHashing (String password) {
        try{
            MessageDigest messageDigest= MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes());
            byte[] resultBytes = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for( byte b : resultBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
