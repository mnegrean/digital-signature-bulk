package ro.approd.gui;

import org.apache.commons.lang3.StringUtils;
import ro.approd.logic.SignerManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class MainView extends JFrame implements ActionListener {

    private static final String CONFIG_TXT = "config.properties";
    private static final String EXE = "exe";
    private static final String SIGN = "sign";
    private static final String PASS = "pass";
    private static final String CONTACT = "contact";
    private JPanel panel = new JPanel();

    private JMenuItem selectExecutable = new JMenuItem("Selectează executabilul");
    private JMenuItem selectFolder = new JMenuItem("Selectează folderul");
    private JButton semneazaButton = new JButton("Semnează");
    private JButton selecteazaButton = new JButton("...");
    private JTextField inputField = new JTextField();

    private final JFileChooser fileChooser = new JFileChooser();
    private final JFileChooser directoryChooser = new JFileChooser();

    private File executable = null;
    private File signDirectory = null;
    private String pass = "";
    private String contact = "";

    private SignerManager signerManager = new SignerManager();

    private Properties properties = new Properties();

    public MainView() {
        super("Semneaza documente");

        setSize(910, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel.setLayout(null);
        JMenuBar menu = new JMenuBar();
        menu.setBounds(0, 0, 910, 20);

        JMenu fileMenu = new JMenu("File");
        menu.add(fileMenu);

        fileMenu.add(selectExecutable);
        fileMenu.add(selectFolder);
        selectExecutable.addActionListener(this);
        selectFolder.addActionListener(this);

        JLabel jLabel = new JLabel();
        jLabel.setBounds(50, 50, 150, 30);
        jLabel.setText("Folderul selectat: ");
        panel.add(jLabel);

        inputField.setBounds(225, 50, 550, 30);
        panel.add(inputField);

        panel.add(semneazaButton);
        semneazaButton.setBounds(50, 100, 150, 50);
        semneazaButton.addActionListener(this);

        panel.add(selecteazaButton);
        selecteazaButton.setBounds(800, 50, 50, 30);
        selecteazaButton.addActionListener(this);

        this.add(menu);
        this.add(panel);
        setVisible(true);

        setLocationRelativeTo(null);

        try {
            properties.load(new FileInputStream(CONFIG_TXT));

            String exe = properties.getProperty(EXE);
            String sign = properties.getProperty(SIGN);
            this.pass = properties.getProperty(PASS);
            this.contact = properties.getProperty(CONTACT);

            if (StringUtils.isNotEmpty(exe)) {
                executable = new File(exe);
            }

            if (StringUtils.isNotEmpty(sign)) {
                signDirectory = new File(sign);
                inputField.setText(sign);
            }

        } catch (IOException e) {
            System.out.println("Config.txt not found. Creating it now.");
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == selectFolder || e.getSource() == selecteazaButton) {
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = directoryChooser.showOpenDialog(MainView.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // folderul unde trebuie semnat
                this.signDirectory = directoryChooser.getSelectedFile();
            }
        }
        if (e.getSource() == selectExecutable) {
            int returnVal = fileChooser.showOpenDialog(MainView.this);
            selectExecutable(returnVal);
        }
        if (e.getSource() == semneazaButton) {

            if (StringUtils.isNotEmpty(inputField.getText())) {
                signDirectory = new File(inputField.getText());
            }
            if (executable != null && signDirectory != null && pass != null && contact != null) {
                try {
                    signerManager.signDocuments(signDirectory, executable, pass, contact);
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    JOptionPane.showMessageDialog(panel, "S-a semnat cu succes.");
                }
            }

            if (executable == null) {
                fileChooser.setDialogTitle("Selecteaza executabilul.");
                int returnVal = fileChooser.showOpenDialog(MainView.this);
                selectExecutable(returnVal);
            }

            if (signDirectory == null) {
                directoryChooser.setDialogTitle("Selecteaza folderul de semnat");
                directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = directoryChooser.showOpenDialog(MainView.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    // folderul unde trebuie semnat
                    this.signDirectory = directoryChooser.getSelectedFile();
                }
            }

            if (pass == null) {
                String password = JOptionPane.showInputDialog(panel, "Introduceți parola semnăturii digitale.");
                if (password != null && !password.isEmpty()) {
                    this.pass = password;
                }
            }

            if (contact == null) {
                String contact = JOptionPane.showInputDialog(panel, "Introduceți numele și prenumele deținătorului semnăturii.");
                if (contact != null && !contact.isEmpty()) {
                    this.contact = contact;
                }
            }
        }
        //TODO: asta se apelează la fiecare action care se face pe swing; nu e chiar ok să se resalveze config file-ul de fiecare dată
        if (signDirectory != null) {
            inputField.setText(signDirectory.getAbsolutePath());
            properties.setProperty(SIGN, signDirectory.getAbsolutePath());
        }
        if (executable != null) {
            properties.setProperty(EXE, executable.getAbsolutePath());
        }
        if (pass != null) {
            properties.setProperty(PASS, pass);
        }
        if (contact != null) {
            properties.setProperty(CONTACT, contact);
        }
        try {
            properties.store(new FileOutputStream(CONFIG_TXT), null);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void selectExecutable(int returnVal) {
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            this.executable = fileChooser.getSelectedFile();
        }
    }
}