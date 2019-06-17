package ro.approd.init;

import ro.approd.logic.SignerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static ro.approd.util.Constants.*;

public class AppLoader {

    private SignerManager signerManager = new SignerManager();

    public void start(String path) {
        Properties properties = new Properties();

        try {
            properties.load(new FileInputStream(CONFIG_TXT));
            String pass = properties.getProperty(PASS);
            String contact = properties.getProperty(CONTACT);
            File executable = new File(properties.getProperty(EXE));

            signerManager.signDocuments(new File(path), executable, pass, contact);
        } catch (IOException e) {
            System.out.println("Some error happened. The docs might have not been signed and you will burn in hell for eternity.");
            e.printStackTrace();
        }
    }
}
