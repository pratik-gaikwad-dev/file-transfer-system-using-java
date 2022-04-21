import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
// import java.lang.module.ModuleDescriptor.Builder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

// import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

// import javax.swing.border.Border;
// import javax.swing.border.EmptyBorder;
// import java.awt.Color;
import java.awt.Component;
import java.sql.*;

/**
 * methods
 */
interface sendReceiveInterface {

    void sendFile();

    void receiveFile();

    void sendReceiveFrame();

}

/**
 * loginRegisterInterface
 */
interface loginRegisterInterface {

    void login();

    void register();
}

/**
 * main
 */
interface Main {

    void main();

}

class databaseConnection {
    private Connection con = null;
    private int result;
    private ResultSet rs = null;
    private int loginResult = 1;
    private String email, email1, fileName;
    private String pass, pass1, receiverFileName;

    public void setData(String email, String pass) {
        this.email = email;
        this.pass = pass;
    }

    public int run() {
        try {

            // Load driver
            Class.forName("com.mysql.jdbc.Driver");

            // Create connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fts", "root", "root");
            String query = "INSERT INTO auth(email, pass) VALUES(?, ?)";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, pass);
            result = stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public int getData() {
        int i = run();
        return i;
    }

    public void setLogin(String email, String pass) {
        this.email = email;
        this.pass = pass;
    }

    public int loginUser() {
        try {

            // Load driver
            Class.forName("com.mysql.jdbc.Driver");

            // Create connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fts", "root", "root");
            String query = "SELECT * FROM auth WHERE email=? and pass=?";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, pass);
            rs = stmt.executeQuery();
            if (rs.next()) {
                email1 = rs.getString("email");
                pass1 = rs.getString("pass");
                loginResult = 1;
            } else {
                loginResult = 0;
            }
            System.out.println(email1);
            System.out.println(pass1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(loginResult);
        return loginResult;
    }

    public int getLogin() {
        int i = loginUser();
        return i;
    }
    public int insertDateTime() {
        try {

            // Load driver
            Class.forName("com.mysql.jdbc.Driver");

            // Create connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fts", "root", "root");
            String query = "INSERT INTO data(filename, date_time) VALUES(?, now())";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, fileName);
            result = stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
        insertDateTime();
    }

    public int insertReceiverDateTime() {
        try {

            // Load driver
            Class.forName("com.mysql.jdbc.Driver");

            // Create connection
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/fts", "root", "root");
            String query = "INSERT INTO receivedfiledata(filename, date_time) VALUES(?, now())";
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, receiverFileName);
            result = stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void setReceverFileName(String filename) {
        this.receiverFileName = filename;
        insertReceiverDateTime();
    }

}

class myExceptions extends Exception {

    public String ipNullException() {
        return "IP Address is null";
    }
}

class swingComponent {

    JFrame sendFileFrame, receiveFileFrame, mainFrame, loginFrame;
    JPanel mainSendFilePanel, buttonSendFilePanel, receiveFilePanel, mainPanel, buttoPanel;
    JButton chooseFileButton, sendFileButton, send, receive, loginButton, registerButton, login, register;
    JLabel headerLabel, iPAddressLabel, fileNameLabel, heading, email, pass, confirmPasswordJLabel;
    JTextField emailTextField;
    JPasswordField passwordField;
    JPasswordField confirmPasswordField;
}

class Server extends Thread {

    private JLabel fileNameLabel;
    private JFrame receiveFileFrame;

    // Constructor of Server class
    public Server(JLabel fileNameLabel, JFrame receiveFileFrame) {
        this.fileNameLabel = fileNameLabel;
        this.receiveFileFrame = receiveFileFrame;
    }

    // Run() is a method used to perform action for a thread
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {

            // Strat server on port 4444
            serverSocket = new ServerSocket(4444);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(receiveFileFrame, e, "Message", 0);
            try {
                serverSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        while (true) {
            try {

                // Accept incoming connections
                Socket socket = serverSocket.accept();

                // DataInputStream allows application to read primitive data from the input
                // stream in a machine-independent way
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                // It read input bytes, It is for file name
                int fileNameLength = dataInputStream.readInt();

                // Initialize byte array of same length as file
                byte[] fileNameSize = new byte[fileNameLength];

                // Read length of byte from input straem
                dataInputStream.readFully(fileNameSize, 0, fileNameSize.length);

                // Strre file name in string
                String fileName1 = new String(fileNameSize);
                fileNameLabel.setText(fileName1);

                // Read file data
                int fileContentLength = dataInputStream.readInt();
                byte[] fileContentSize = new byte[fileContentLength];
                dataInputStream.readFully(fileContentSize, 0, fileContentLength);
                
                databaseConnection dc = new databaseConnection();
                dc.setReceverFileName(fileName1);

                // Create and write data in file
                File fileDownload = new File(fileName1);
                FileOutputStream fileOutputStream = new FileOutputStream(fileDownload);
                fileOutputStream.write(fileContentSize);
                fileOutputStream.close();

            } catch (Exception e) {
                JOptionPane.showMessageDialog(receiveFileFrame, e, "Message", 0);

            }
        }
    }
}

class sendReceive extends swingComponent implements sendReceiveInterface {

    public static File[] file = new File[1];

    @Override
    public void sendFile() {
        // Send file swing code

        sendFileFrame = new JFrame();
        mainSendFilePanel = new JPanel();
        buttonSendFilePanel = new JPanel();
        fileNameLabel = new JLabel();
        chooseFileButton = new JButton();
        sendFileButton = new JButton();

        sendFileFrame.setBounds(750, 300, 400, 200);
        sendFileFrame.setLayout(new BoxLayout(sendFileFrame.getContentPane(), BoxLayout.Y_AXIS));

        fileNameLabel.setText("Choose File");
        fileNameLabel.setFont(new Font("Serif", Font.BOLD, 20));
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        chooseFileButton.setText("Choose File");
        chooseFileButton.setSize(100, 50);
        chooseFileButton.setFont(new Font("Serif", Font.PLAIN, 15));

        sendFileButton.setText("Send File");
        sendFileButton.setSize(100, 50);
        sendFileButton.setFont(new Font("Serif", Font.PLAIN, 15));

        buttonSendFilePanel.add(chooseFileButton);
        buttonSendFilePanel.add(sendFileButton);

        mainSendFilePanel.add(fileNameLabel);
        mainSendFilePanel.add(Box.createHorizontalStrut(5));
        mainSendFilePanel.add(buttonSendFilePanel);
        mainSendFilePanel.setLayout(new BoxLayout(mainSendFilePanel, BoxLayout.Y_AXIS));

        sendFileFrame.add(mainSendFilePanel);

        sendFileFrame.setVisible(true);

        sendFileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Action listener for chooseFileButton
        chooseFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // FileChooser
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Choose a file to send");

                // Condition for choose file
                if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {

                    // Store file into fileSend variable which is chosen by FileChooser
                    file[0] = fileChooser.getSelectedFile();
                    fileNameLabel.setText(file[0].getName());

                }
            }
        });

        // Action listener for sendFileButton
        sendFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // It check file selected or not
                if (file[0] == null) {

                    JOptionPane.showMessageDialog(sendFileFrame, "Select File", "File", 0);

                } else {

                    try {

                        // Logic for send file
                        String IP = JOptionPane.showInputDialog(sendFileFrame, "Enter IP address");
                        if (IP != null) {

                            // Connect with server
                            Socket socket = new Socket(IP, 4444);

                            // FileInputStream obtain input bytes from a file
                            FileInputStream fileInputStream = new FileInputStream(file[0].getAbsolutePath());

                            // DataOutputStream allows application to write primitive data in a
                            // machine-independent way
                            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

                            // It gives file name in string
                            String fileName = file[0].getName();

                            // Encode file into sequence of byte
                            byte[] fileNameByte = fileName.getBytes();

                            // Creating byte array of same length as file
                            byte[] fileContentByte = new byte[(int) file[0].length()];

                            // It read byte of data of a file
                            fileInputStream.read(fileContentByte);

                            // Write an int to the output stream
                            outputStream.writeInt(fileNameByte.length);

                            // It write bytes
                            outputStream.write(fileNameByte);

                            outputStream.writeInt(fileContentByte.length);
                            outputStream.write(fileContentByte);
                            databaseConnection dc = new databaseConnection();
                            dc.setFileName(fileName);
                        } else {
                            try {
                                throw new myExceptions();

                            } catch (myExceptions e1) {
                                System.out.println(e1.ipNullException());
                            }

                        }
                    } catch (Exception error) {

                        JOptionPane.showMessageDialog(sendFileFrame, error, "Message", 0);
                        error.printStackTrace();

                    }

                }
            }
        });

    }

    @Override
    public void receiveFile() {

        // Receive frame swing code

        receiveFileFrame = new JFrame();
        receiveFilePanel = new JPanel();
        fileNameLabel = new JLabel();
        headerLabel = new JLabel();
        iPAddressLabel = new JLabel();

        try {
            // Getting IP of user
            InetAddress localhost = InetAddress.getLocalHost();
            iPAddressLabel.setText("Your IP Address : " + (localhost.getHostAddress()).trim());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        receiveFileFrame.setBounds(750, 300, 400, 200);
        receiveFileFrame.setLayout(new BoxLayout(receiveFileFrame.getContentPane(), BoxLayout.Y_AXIS));

        fileNameLabel.setFont(new Font("Serif", Font.PLAIN, 15));
        fileNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        iPAddressLabel.setFont(new Font("Serif", Font.PLAIN, 12));
        iPAddressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerLabel.setText("Received Files");
        headerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiveFilePanel.add(fileNameLabel);

        receiveFileFrame.add(headerLabel);
        receiveFileFrame.add(iPAddressLabel);
        receiveFileFrame.add(receiveFilePanel);

        receiveFileFrame.setVisible(true);

        receiveFileFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Calling Server class object to receive file
        Thread t = new Server(fileNameLabel, receiveFileFrame);
        t.start();

    }

    @Override
    public void sendReceiveFrame() {
        heading = new JLabel();
        send = new JButton();
        receive = new JButton();
        mainPanel = new JPanel();
        buttoPanel = new JPanel();
        mainFrame = new JFrame();

        mainFrame.setBounds(700, 250, 400, 200);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));

        heading.setText("File Transfer");
        heading.setFont(new Font("Serif", Font.BOLD, 20));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        send.setText("Send");
        send.setSize(100, 50);
        send.setFont(new Font("Serif", Font.PLAIN, 15));

        receive.setText("Receive");
        receive.setSize(100, 50);
        receive.setFont(new Font("Serif", Font.PLAIN, 15));

        buttoPanel.add(send);
        buttoPanel.add(receive);

        mainPanel.add(heading);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createHorizontalStrut(10));
        mainPanel.add(buttoPanel);

        mainFrame.add(mainPanel);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Action Listener for send button
        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Calling sendFile() method
                sendFile();

            }
        });

        receive.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Calling receive file method
                receiveFile();

            }
        });

    }

}

class loginRegister extends sendReceive implements loginRegisterInterface {

    @Override
    public void login() {
        loginFrame = new JFrame("Login");

        email = new JLabel("Enter Email");
        email.setBounds(50, 100, 200, 30);

        emailTextField = new JTextField();
        emailTextField.setBounds(50, 130, 200, 30);

        pass = new JLabel("Enter Password");
        pass.setBounds(50, 180, 200, 30);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 210, 200, 30);

        login = new JButton("Login");
        login.setBounds(50, 260, 200, 30);

        loginFrame.add(email);
        loginFrame.add(emailTextField);
        loginFrame.add(pass);
        loginFrame.add(passwordField);
        loginFrame.add(login);

        loginFrame.setSize(400, 400);
        loginFrame.setLayout(null);
        loginFrame.setVisible(true);
        loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        login.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    String email = emailTextField.getText();
                    String pass = new String(passwordField.getPassword());
                    databaseConnection dc = new databaseConnection();
                    dc.setLogin(email, pass);
                    int i = dc.getLogin();
                    if (i > 0) {
                        sendReceiveFrame();
                        loginFrame.dispose();
                    } else {
                        JOptionPane.showMessageDialog(loginFrame, "Invalid Credentials!", "Login", 0);
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

    }

    @Override
    public void register() {
        loginFrame = new JFrame("Register");

        email = new JLabel("Enter Email");
        email.setBounds(50, 100, 200, 30);

        emailTextField = new JTextField();
        emailTextField.setBounds(50, 130, 200, 30);

        pass = new JLabel("Enter Password");
        pass.setBounds(50, 180, 200, 30);

        passwordField = new JPasswordField();
        passwordField.setBounds(50, 210, 200, 30);

        confirmPasswordJLabel = new JLabel("Confirm Password");
        confirmPasswordJLabel.setBounds(50, 260, 200, 30);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(50, 290, 200, 30);

        register = new JButton("Register");
        register.setBounds(50, 340, 200, 30);

        loginFrame.add(email);
        loginFrame.add(emailTextField);
        loginFrame.add(pass);
        loginFrame.add(passwordField);
        loginFrame.add(confirmPasswordJLabel);
        loginFrame.add(confirmPasswordField);
        loginFrame.add(register);

        loginFrame.setSize(400, 400);
        loginFrame.setLayout(null);
        loginFrame.setVisible(true);
        loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        register.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    String email = emailTextField.getText();
                    String pass = new String(passwordField.getPassword());
                    databaseConnection dc = new databaseConnection();
                    dc.setData(email, pass);
                    int i = dc.getData();
                    if (i > 0) {
                        JOptionPane.showMessageDialog(loginFrame, "Account created", "Login", 1);
                        loginFrame.dispose();
                    } else {
                        System.out.println("Record not inserted");
                    }

                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

    }

}

class mainClass extends loginRegister implements Main {

    @Override
    public void main() {
        heading = new JLabel();
        loginButton = new JButton();
        registerButton = new JButton();
        mainPanel = new JPanel();
        buttoPanel = new JPanel();
        mainFrame = new JFrame();

        mainFrame.setBounds(700, 250, 400, 200);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));

        heading.setText("Login Here");
        heading.setFont(new Font("Serif", Font.BOLD, 20));
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginButton.setText("Login");
        loginButton.setSize(100, 50);
        loginButton.setFont(new Font("Serif", Font.PLAIN, 15));

        registerButton.setText("Register");
        registerButton.setSize(100, 50);
        registerButton.setFont(new Font("Serif", Font.PLAIN, 15));

        buttoPanel.add(loginButton);
        buttoPanel.add(registerButton);

        mainPanel.add(heading);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(Box.createHorizontalStrut(10));
        mainPanel.add(buttoPanel);

        mainFrame.add(mainPanel);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Calling receive file method
                login();

            }
        });
        registerButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                // Calling receive file method
                register();

            }
        });

    }

}

class App {
    public static void main(String[] args) {
        mainClass mc = new mainClass();
        mc.main();
        // loginRegister lr = new loginRegister();
        // lr.login();
        // sendReceive sr = new sendReceive();
        // sr.main();

    }
}