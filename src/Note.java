import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Note {
    private final JFrame frame; // Ana pencere
    private final UserManager userManager; // Kullanıcı yönetimi için UserManager örneği
    private User currentUser; // Şu anki oturum açmış kullanıcı

    public Note() {
        userManager = new UserManager(); // UserManager sınıfının örneğini oluşturur.
        frame = new JFrame("Ink Byte"); // Ana çerçeve oluşturulur.
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Çerçeve kapatma işlemi özelleştirilir.
        frame.setSize(400, 250); // Çerçevenin boyutu ayarlanır.
        frame.setLayout(new GridLayout(2, 2)); // Çerçeve düzeni ayarlanır.
        frame.setLocationRelativeTo(null); // Çerçevenin ekranın ortasında açılması sağlanır.
        String iconPath = "InkByte_icon.png"; // İkon dosyası yolu belirlenir.
        frame.setIconImage(new ImageIcon(iconPath).getImage()); // İkon çerçeveye eklenir.

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(frame, "You are exiting the application. Are you sure?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    userManager.saveUsersToFile(); // Kullanıcı verilerini kaydeder.
                    System.exit(0); // Uygulamayı kapatır.
                }
            }
        });

        JPanel userLoginScreen = new JPanel(new GridLayout(2, 2)); // Kullanıcı giriş ekranı paneli oluşturulur.
        JTextField userName = new JTextField(); // Kullanıcı adı alanı oluşturulur.
        JPasswordField password = new JPasswordField(); // Şifre alanı oluşturulur.
        userLoginScreen.add(new JLabel("Username: ")); // Kullanıcı adı etiketi eklenir.
        userLoginScreen.add(userName); // Kullanıcı adı alanı eklenir.
        userLoginScreen.add(new JLabel("Password: ")); // Şifre etiketi eklenir.
        userLoginScreen.add(password); // Şifre alanı eklenir.

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2)); // Buton paneli oluşturulur.
        JButton loginButton = new JButton("Login"); // Giriş butonu oluşturulur.
        JButton registerButton = new JButton("Register"); // Kayıt butonu oluşturulur.
        buttonPanel.add(loginButton); // Giriş butonu eklenir.
        buttonPanel.add(registerButton); // Kayıt butonu eklenir.

        loginButton.addActionListener(e -> {
            String usernameInput = userName.getText();
            String passwordInput = new String(password.getPassword());

            if (userManager.authenticateUser(usernameInput, passwordInput)) {
                currentUser = userManager.getUser(usernameInput); // Geçerli kullanıcı atanır.
                openNewWindow(); // Yeni pencere açılır.
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password!"); // Hata mesajı gösterilir.
            }
        });

        registerButton.addActionListener(e -> {
            String usernameInput = userName.getText();
            String passwordInput = new String(password.getPassword());

            if (userManager.isUsernameTaken(usernameInput)) {
                JOptionPane.showMessageDialog(frame, "Username already taken!"); // Hata mesajı gösterilir.
            } else {
                try {
                    String hashedPassword = User.hashPassword(passwordInput); // Şifre hashlenir.
                    userManager.addUser(new User(usernameInput, hashedPassword)); // Yeni kullanıcı eklenir.
                    userManager.saveUsersToFile(); // Kullanıcı verileri kaydedilir.
                    JOptionPane.showMessageDialog(frame, "User registered successfully!"); // Başarı mesajı gösterilir.
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error registering user: " + ex.getMessage()); // Hata mesajı gösterilir.
                }
            }
        });

        frame.add(userLoginScreen); // Giriş ekranı paneli çerçeveye eklenir.
        frame.add(buttonPanel); // Buton paneli çerçeveye eklenir.
        frame.setVisible(true); // Çerçeve görünür yapılır.
    }

    private void openNewWindow() {
        frame.dispose(); // Ana çerçeve kapatılır.
        JFrame newFrame = new JFrame("Ink Byte"); // Yeni çerçeve oluşturulur.
        newFrame.setSize(800, 600); // Yeni çerçevenin boyutu ayarlanır.
        newFrame.setLocationRelativeTo(null); // Yeni çerçevenin ekranın ortasında açılması sağlanır.
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Yeni çerçevenin kapatılma işlemi ayarlanır.
        String iconPath = "InkByte_icon.png"; // İkon dosyası yolu belirlenir.
        ImageIcon icon = new ImageIcon(iconPath); // İkon oluşturulur.
        newFrame.setIconImage(icon.getImage()); // İkon yeni çerçeveye eklenir.

        JPanel leftPanel = new JPanel(new BorderLayout()); // Sol panel oluşturulur.
        DefaultListModel<String> fileListModel = new DefaultListModel<>(); // Dosya listesi modeli oluşturulur.
        JList<String> fileList = new JList<>(fileListModel); // Dosya listesi oluşturulur.
        leftPanel.add(new JScrollPane(fileList), BorderLayout.CENTER); // Dosya listesi sol panele eklenir.

        JPanel rightPanel = new JPanel(new BorderLayout()); // Sağ panel oluşturulur.
        JTextArea noteArea = new JTextArea(); // Not alanı oluşturulur.
        rightPanel.add(new JScrollPane(noteArea), BorderLayout.CENTER); // Not alanı sağ panele eklenir.

        JPanel fileNamePanel = new JPanel(new GridLayout(1, 2)); // Dosya adı paneli oluşturulur.
        JLabel fileNameLabel = new JLabel("File Name:"); // Dosya adı etiketi oluşturulur.
        JTextField fileNameField = new JTextField(); // Dosya adı alanı oluşturulur.
        fileNamePanel.add(fileNameLabel); // Dosya adı etiketi panel eklenir.
        fileNamePanel.add(fileNameField); // Dosya adı alanı panel eklenir.
        rightPanel.add(fileNamePanel, BorderLayout.NORTH); // Dosya adı paneli sağ panele eklenir.

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2)); // Buton paneli oluşturulur.
        JButton saveButton = new JButton("Save Note"); // Kaydet butonu oluşturulur.
        JButton deleteButton = new JButton("Delete Note"); // Sil butonu oluşturulur.
        buttonPanel.add(saveButton); // Kaydet butonu buton paneline eklenir.
        buttonPanel.add(deleteButton); // Sil butonu buton paneline eklenir.
        rightPanel.add(buttonPanel, BorderLayout.SOUTH); // Buton paneli sağ panele eklenir.

        loadUserNotes(fileListModel); // Kullanıcı notları yüklenir.

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFile = fileList.getSelectedValue();
                if (selectedFile != null) {
                    try {
                        String filePath = "notes/" + currentUser.getUsername() + "/" + selectedFile;
                        String content = new String(Files.readAllBytes(Paths.get(filePath))); // Dosya içeriği okunur.
                        noteArea.setText(content); // Not alanına içerik yazılır.
                        fileNameField.setText(selectedFile.replace(".txt", "")); // Dosya adı alanı güncellenir.
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(newFrame, "Error loading note: " + ex.getMessage()); // Hata mesajı gösterilir.
                    }
                }
            }
        });

        saveButton.addActionListener(e -> {
            String fileName = fileNameField.getText().trim();
            if (!fileName.isEmpty()) {
                String noteContent = noteArea.getText();
                saveNoteToFile(currentUser.getUsername(), fileName, noteContent, fileListModel); // Not kaydedilir.
                noteArea.setText(""); // Not alanı temizlenir.
                fileNameField.setText(""); // Dosya adı alanı temizlenir.
            } else {
                JOptionPane.showMessageDialog(newFrame, "Please enter a file name!"); // Hata mesajı gösterilir.
            }
        });

        deleteButton.addActionListener(e -> {
            String fileName = fileNameField.getText().trim();
            if (!fileName.isEmpty()) {
                deleteNoteFile(currentUser.getUsername(), fileName, fileListModel); // Not silinir.
                noteArea.setText(""); // Not alanı temizlenir.
                fileNameField.setText(""); // Dosya adı alanı temizlenir.
            } else {
                JOptionPane.showMessageDialog(newFrame, "Please enter a file name!"); // Hata mesajı gösterilir.
            }
        });

        newFrame.setLayout(new GridLayout(1, 2)); // Yeni çerçeve düzeni ayarlanır.
        newFrame.add(leftPanel); // Sol panel yeni çerçeveye eklenir.
        newFrame.add(rightPanel); // Sağ panel yeni çerçeveye eklenir.

        newFrame.setVisible(true); // Yeni çerçeve görünür yapılır.
    }

    private void loadUserNotes(DefaultListModel<String> fileListModel) {
        fileListModel.clear(); // Dosya listesi temizlenir.
        String userFolder = "notes/" + currentUser.getUsername(); // Kullanıcı notları klasörü belirlenir.
        File folder = new File(userFolder);
        if (folder.exists() && folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    fileListModel.addElement(file.getName()); // Dosya adı listeye eklenir.
                }
            }
        }
    }

    private void saveNoteToFile(String username, String fileName, String content, DefaultListModel<String> fileListModel) {
        try {
            String userFolder = "notes/" + username; // Kullanıcı notları klasörü belirlenir.
            File dir = new File(userFolder);
            if (!dir.exists()) {
                dir.mkdirs(); // Klasör yoksa oluşturulur.
            }
            String filePath = userFolder + "/" + fileName + ".txt"; // Dosya yolu belirlenir.
            File file = new File(filePath);
            if (file.exists()) {
                JOptionPane.showMessageDialog(null, "Filename already exists! Please change filename."); // Hata mesajı gösterilir.
            } else {
                FileWriter writer = new FileWriter(filePath); // Dosya yazıcısı oluşturulur.
                writer.write(content); // İçerik yazılır.
                writer.close(); // Dosya yazıcısı kapatılır.
                fileListModel.addElement(fileName + ".txt"); // Dosya adı listeye eklenir.
                JOptionPane.showMessageDialog(null, "Note saved successfully!"); // Başarı mesajı gösterilir.
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving note: " + e.getMessage()); // Hata mesajı gösterilir.
        }
    }

    private void deleteNoteFile(String username, String fileName, DefaultListModel<String> fileListModel) {
        try {
            String filePath = "notes/" + username + "/" + fileName + ".txt"; // Dosya yolu belirlenir.
            Files.deleteIfExists(Paths.get(filePath)); // Dosya silinir.
            fileListModel.removeElement(fileName + ".txt"); // Dosya adı listeden kaldırılır.
            JOptionPane.showMessageDialog(null, "Note deleted successfully!"); // Başarı mesajı gösterilir.
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error deleting note: " + e.getMessage()); // Hata mesajı gösterilir.
        }
    }
}
