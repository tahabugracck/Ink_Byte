import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

//kullanıcı bilgileri için bu şekilde bir sınıf oluşturduk.
//Serializable arayüzünü uygulamasının nedeni, bu sınıfın nesnelerinin serileştirilebilir olmasını sağlamaktır.
// Serileştirme, bir nesnenin verilerinin bir dosyaya veya ağa yazılabilir hale getirilmesi anlamına gelir.
// Bu sayede kullanıcı nesneleri dosyaya yazılabilir ve dosyadan okunabilir hale gelir.
//Nesneleri kullanırken bir defa değil istenilen zamanda ve yerde tekrar, tekrar ve tekrar kullanabilmemiz gerekebiliyor.
// Bu noktada ise bizim “serileştirme” dediğimiz yapı devreye giriyor.

public class User implements Serializable {
    private String username;//kullanıcı değişkenini saklar.
    private String password;//kullanıcı değişkenini saklar

    //kurucu methot.
    public User(String userName, String password) {
        this.username = userName;
        this.password = password;
    }

    public String getUsername() {//kullanıcının adını döndürür.
        return username;
    }

    public String getPassword() {//kullanıcının şifresini döndürür.
        return password;
    }
}

// Bu sınıf, kullanıcıları depolamak, eklemek, doğrulamak ve dosyaya kaydetmek gibi işlevlere sahiptir.
class UserManager {
    //Bu liste, programın çalışması sırasında kullanıcıları depolamak için kullanılır.
    private ArrayList<User> users;

    public UserManager() {
        loadUsersFromFile();
        //mevcut kullanıcıları dosyadan yükler.
    }

    public void addUser(User user) {
        //addUser(User user) yöntemi, verilen bir kullanıcıyı users listesine ekler.
        users.add(user);
    }

    public boolean authenticateUser(String username, String password) {//kullanıcı verilerini kontrol etmeye sağlar.
        for (User user : users) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }

    public boolean isUsernameTaken(String username) {//kullanıcı adının var olup olmadığını kontrol etmek için kullanılır.
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }

    public void saveUsersToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("users.dat"))) {

            //.dat uzantılı dosyalar genellikle yapısal bir veri dosyası olarak kullanılır.
            // Java'da bu tür dosyalar genellikle serileştirme ile oluşturulur.
            // Serileştirme, nesnelerin yapısal verilerini byte dizilerine dönüştürerek dosyaya yazmayı sağlar.
            // Bu sayede verilerin tutarlı bir şekilde saklanması ve okunması sağlanır.
            //Nesnelerin serileştirilmesi, verilerin tutarlılığını korur.
            // Örneğin, kullanıcı nesnelerini ayrı bir dosyada saklamak yerine tek bir .dat dosyasında saklamak, veri bütünlüğünü sağlar ve dosya işlemlerini kolaylaştırır.
            //.dat dosyaları genellikle taşınabilir ve okunabilir biçimdedir.
            // Bu, dosyanın farklı platformlar arasında kolayca taşınabilir ve okunabilir olmasını sağlar.
            // Java'nın serileştirme mekanizması da bu taşınabilirlik ve okunabilirlik özelliklerini destekler.

            oos.writeObject(users);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving users: " + e.getMessage());
        }
    }

    private void loadUsersFromFile() {

        //loadUsersFromFile() yöntemi, "users.dat" dosyasından kullanıcıları yükler.
        // Bu işlem, dosyadan okunan byte dizilerini nesne haline getirerek users listesine ekler.
        // Bu işlemi yaparken Java'nın deserializasyon (deserialization) mekanizmasından yararlanır.

        File file = new File("users.dat");

        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (ArrayList<User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                JOptionPane.showMessageDialog(null, "Error loading users: " + e.getMessage());
            }
        } else {
            users = new ArrayList<>();
        }
    }
}

//.dat şeklinde kaydettiğimiz zaman.
//Kullanıcı verilerini kaydettiğiniz users.dat dosyasında gördüğünüz yazıların bu şekilde görünmesinin nedeni, Java'nın serileştirme (serialization) mekanizmasını kullanıyor olmanızdır.
// Java'da serileştirme, nesnelerin durumlarının bayt dizilerine dönüştürülerek bir dosyaya yazılması işlemidir.
// Bu, nesnelerin durumlarını saklamak ve daha sonra geri yüklemek için kullanılır.
// Bu bayt dizileri, insanlar tarafından okunabilir metinler değil, ikili (binary) veriler içerir.