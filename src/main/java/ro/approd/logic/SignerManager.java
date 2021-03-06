package ro.approd.logic;

import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;

public class SignerManager {

    private static final String SIGNED = File.separator + "signed";
    private static final String PDF = ".pdf";
    private DocumentSigner documentSigner;

    public void signDocuments(File signDirectory, File executable, String pass, String contact) throws IOException {
        String rootPath = signDirectory.getAbsolutePath() + SIGNED;
        deleteBaseFolderIfExists(rootPath);
        String execPath = executable.getAbsolutePath();
        this.documentSigner = new DocumentSigner(rootPath, execPath, pass, contact);

        signAndRecreate(signDirectory);
    }

    private void deleteBaseFolderIfExists(String rootPath) throws IOException {
        if (Files.exists(Paths.get(rootPath))) {
            FileUtils.deleteDirectory(new File(rootPath));
        }
    }

    private void signAndRecreate(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null && files.length > 0) {
                for (File f : files) {
                    signAndRecreate(f);
                }
            }
        } else {
            try {
                if (isPdf(file)) {
                    documentSigner.sign(file);
                }
            } catch (DocumentException | GeneralSecurityException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isPdf(File file) {
        return file.getName().endsWith(PDF);
    }

}
