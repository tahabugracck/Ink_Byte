import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class UserManager {
    private ArrayList<User> users; // Kullanıcı listesi

    public UserManager() {
        loadUsersFromFile(); // Kullanıcılar dosyadan yüklenir.
    }

    public void addUser(User user) {
        users.add(user); // Kullanıcı eklenir.
    }

    public boolean authenticateUser(String username, String password) {
        try {
            String hashedPassword = User.hashPassword(password); // Şifre hashlenir.
            for (User user : users) {
                if (user.getUsername().equals(username) && user.getPassword().equals(hashedPassword)) {
                    return true; // Kullanıcı doğrulanır.
                }
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(); // Hata durumunda istisna yazdırılır.
        }
        return false; // Kullanıcı doğrulanamazsa false döner.
    }

    public boolean isUsernameTaken(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true; // Kullanıcı adı alınmışsa true döner.
            }
        }
        return false; // Kullanıcı adı alınmamışsa false döner.
    }

    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user; // Kullanıcı bulunursa döner.
            }
        }
        return null; // Kullanıcı bulunamazsa null döner.
    }

    public void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {
            oos.writeObject(users); // Kullanıcı listesi dosyaya yazılır.
        } catch (IOException e) {
            e.printStackTrace(); // Hata durumunda istisna yazdırılır.
        }
    }

    public void loadUsersFromFile() {
        File file = new File("users.dat"); // Kullanıcı dosyası belirlenir.
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (ArrayList<User>) ois.readObject(); // Kullanıcılar dosyadan okunur.
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace(); // Hata durumunda istisna yazdırılır.
            }
        } else {
            users = new ArrayList<>(); // Dosya yoksa boş kullanıcı listesi oluşturulur.
        }
    }
}
