import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

//Note sınıfı, kullanıcıların notlarını yönetmek için arayüz sağlar.


public class Note {
    private JFrame frame;
    private UserManager userManager;//userManager adında bir UserManager nesnesi oluşturulur, bu nesne kullanıcı yönetimi için kullanılır.

    public Note() {
        userManager = new UserManager();//Kullanıcı yönetimini sağlayan nesnedir.
        frame = new JFrame("Ink Byte");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(2, 2));
        frame.setLocationRelativeTo(null);
        String iconPath = "InkByte_icon.png";
        frame.setIconImage(new ImageIcon(iconPath).getImage());

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int response = JOptionPane.showConfirmDialog(frame, "You are exiting the application. Are you sure?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    userManager.saveUsersToFile(); // Uygulama kapatıldığında kullanıcı verilerini kaydet
                    System.exit(0); // Uygulamayı kapat
                }
            }
        });
        //    Kullanıcı uygulamayı kapatmaya çalıştığında, bir onay penceresi açılır. Kullanıcı "Evet" derse, uygulama kapanır.


        JPanel userLoginScreen = new JPanel(new GridLayout(2, 2));//Kullanıcı adı ve şifre alanlarını içeren paneldir.
        JTextField userName = new JTextField();
        JPasswordField password = new JPasswordField();
        userLoginScreen.add(new JLabel("Username : "));
        userLoginScreen.add(userName);
        userLoginScreen.add(new JLabel("Password : "));
        userLoginScreen.add(password);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));//Giriş ve kayıt düğmelerini içeren paneldir.
        JButton loginButton = new JButton("Login");

        //"Login" düğmesine basıldığında, kullanıcı adı ve şifre doğrulanır.
        // Doğruysa notlar için yeni bir pencere açılır, değilse hata mesajı gösterilir.

        JButton registerButton = new JButton("Register");

        //"Register" düğmesine basıldığında, yeni bir kullanıcı oluşturulur ve kullanıcı bilgileri userManager aracılığıyla kaydedilir.

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usernameInput = userName.getText();
                String passwordInput = new String(password.getPassword());

                if (userManager.authenticateUser(usernameInput, passwordInput)) {
                    openNewWindow();
                } else {
                    JOptionPane.showMessageDialog(frame, "Invalid username or password!");
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usernameInput = userName.getText();
                String passwordInput = new String(password.getPassword());

                if (userManager.isUsernameTaken(usernameInput)) {
                    JOptionPane.showMessageDialog(frame, "Username already taken!");//Kullanıcı adı ve şifreyi doğrular. Doğruysa yeni bir pencere açar.
                } else {
                    userManager.addUser(new User(usernameInput, passwordInput));
                    userManager.saveUsersToFile();
                    JOptionPane.showMessageDialog(frame, "User registered successfully!");//Yeni bir kullanıcı kaydeder ve dosyaya kaydeder.
                }
            }
        });

        frame.add(userLoginScreen);
        frame.add(buttonPanel);

        frame.setVisible(true);
    }

    //Yeni bir pencere açar ve not alma arayüzünü yapılandırır.
    private void openNewWindow() {

        frame.dispose();
        JFrame newFrame = new JFrame("Ink Byte");
        newFrame.setSize(800, 600);
        newFrame.setLocationRelativeTo(null);
        newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        String iconPath = "InkByte_icon.png";
        ImageIcon icon = new ImageIcon(iconPath);
        newFrame.setIconImage(icon.getImage());

        JPanel leftPanel = new JPanel(new BorderLayout());//Klasörleri ve dosyaları listelemek için kullanılır.
        DefaultListModel<String> folderListModel = new DefaultListModel<>();
        folderListModel.addElement("Java");
        folderListModel.addElement("Python");
        JList<String> folderList = new JList<>(folderListModel);
        leftPanel.add(new JScrollPane(folderList), BorderLayout.CENTER);

        DefaultListModel<String> fileListModel = new DefaultListModel<>();
        JList<String> fileList = new JList<>(fileListModel);
        leftPanel.add(new JScrollPane(fileList), BorderLayout.EAST);

        JPanel rightPanel = new JPanel(new BorderLayout());
        JTextArea noteArea = new JTextArea();
        rightPanel.add(new JScrollPane(noteArea), BorderLayout.CENTER);

        JPanel fileNamePanel = new JPanel(new GridLayout(1, 2));
        JLabel fileNameLabel = new JLabel("File Name:");
        JTextField fileNameField = new JTextField();
        fileNamePanel.add(fileNameLabel);
        fileNamePanel.add(fileNameField);
        rightPanel.add(fileNamePanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton saveButton = new JButton("Save Note");
        JButton deleteButton = new JButton("Delete Note");
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);//Not alanı ve dosya adı alanı için kullanılır.

        folderList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fileListModel.clear();
                String selectedFolder = folderList.getSelectedValue();
                File folder = new File(selectedFolder);
                if (folder.exists() && folder.isDirectory()) {
                    for (File file : folder.listFiles()) {
                        if (file.isFile() && file.getName().endsWith(".txt")) {
                            fileListModel.addElement(file.getName());
                        }
                    }
                }
            }
        });

        fileList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedFolder = folderList.getSelectedValue();//Seçilen klasördeki .txt dosyalarını listelemek için kullanılır.
                String selectedFile = fileList.getSelectedValue();//Seçilen dosyanın içeriğini not alanına yükler.
                if (selectedFolder != null && selectedFile != null) {
                    try {
                        String filePath = selectedFolder + "/" + selectedFile;
                        String content = new String(Files.readAllBytes(Paths.get(filePath)));
                        noteArea.setText(content);
                        fileNameField.setText(selectedFile.replace(".txt", ""));
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(newFrame, "Error loading note: " + ex.getMessage());
                    }
                }
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFolder = folderList.getSelectedValue();
                String fileName = fileNameField.getText().trim();
                if (selectedFolder != null && !fileName.isEmpty()) {
                    String noteContent = noteArea.getText();
                    saveNoteToFile(selectedFolder, fileName, noteContent);
                } else {
                    JOptionPane.showMessageDialog(newFrame, "Please select a folder and enter a file name!");
                }
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedFolder = folderList.getSelectedValue();
                String fileName = fileNameField.getText().trim();
                if (selectedFolder != null && !fileName.isEmpty()) {
                    deleteNoteFile(selectedFolder, fileName);
                } else {
                    JOptionPane.showMessageDialog(newFrame, "Please select a folder and enter a file name!");
                }
            }
        });

        newFrame.setLayout(new GridLayout(1, 2));
        newFrame.add(leftPanel);
        newFrame.add(rightPanel);

        newFrame.setVisible(true);
    }

    //saveNoteToFile(String folder, String fileName, String content) yöntemi, belirli bir klasöre ve dosya adına sahip bir metin dosyasına notu kaydeder.
    private void saveNoteToFile(String folder, String fileName, String content) {
        try {
            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = folder + "/" + fileName + ".txt";
            FileWriter writer = new FileWriter(filePath);
            writer.write(content);
            writer.close();
            JOptionPane.showMessageDialog(null, "Note saved successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving note: " + e.getMessage());
        }
    }

    //deleteNoteFile(String folder, String fileName) yöntemi, belirtilen klasördeki belirli bir dosyayı siler.
    private void deleteNoteFile(String folder, String fileName) {
        try {
            String filePath = folder + "/" + fileName + ".txt";
            Files.deleteIfExists(Paths.get(filePath));
            JOptionPane.showMessageDialog(null, "Note deleted successfully!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error deleting note: " + e.getMessage());
        }
    }


}
