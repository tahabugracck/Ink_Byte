import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User implements Serializable {
    private final String username; // Kullanıcı adı
    private final String password; // Şifre (hashlenmiş)

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256"); // SHA-256 algoritması kullanılır.
        byte[] hashedBytes = md.digest(password.getBytes()); // Şifre hashlenir.
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b)); // Hashlenmiş baytlar hex formatına çevrilir.
        }
        return sb.toString();
    }
}
