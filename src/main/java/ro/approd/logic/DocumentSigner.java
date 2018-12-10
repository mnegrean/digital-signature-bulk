package ro.approd.logic;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.*;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import sun.security.pkcs11.SunPKCS11;
import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
import sun.security.pkcs11.wrapper.PKCS11;
import sun.security.pkcs11.wrapper.PKCS11Exception;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;

public class DocumentSigner {

    private final String rootPath;
    private final String execPath;
    private final String pass;
    private final String contact;

    public DocumentSigner(String rootPath, String execPath, String pass, String contact) {
        this.rootPath = rootPath;
        this.execPath = execPath;
        this.pass = pass;
        this.contact = contact;
    }

    public void sign(File fileToBeSigned) throws IOException, DocumentException, GeneralSecurityException {
        String initialRootPath = rootPath.replaceAll("signed", "");

        System.out.println("Initial root path: " + initialRootPath);

        String absolutePath = fileToBeSigned.getAbsolutePath();
        System.out.println("Absolute path: " + absolutePath);

        Path pathBase = Paths.get(initialRootPath);
        Path absolute = Paths.get(absolutePath);
        Path relativize = pathBase.relativize(absolute);

        String destinationFileAbsolutePath = initialRootPath + "signed" + File.separator + relativize;

        File destinationFileSigned = new File(destinationFileAbsolutePath);
        File parent = destinationFileSigned.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        boolean created = destinationFileSigned.createNewFile();
        if (!created) {
            throw new IllegalStateException("Couldn't create file: " + destinationFileAbsolutePath);
        }

        Provider providerPKCS11 = buildProviderConfig();

        char[] pass = this.pass.toCharArray();
        BouncyCastleProvider providerBC = new BouncyCastleProvider();
        Security.addProvider(providerBC);
        KeyStore ks = KeyStore.getInstance("PKCS11");
        ks.load(null, pass);
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, pass);
        Certificate[] chain = ks.getCertificateChain(alias);

        PdfReader reader = new PdfReader(fileToBeSigned.getAbsolutePath());
        FileOutputStream os = new FileOutputStream(destinationFileSigned);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0');

        PdfSignatureAppearance appearance = createSignatureAppearance(stamper);

        Rectangle rectangle = createSignatureRectangle(reader);
        appearance.setVisibleSignature(rectangle, 1, "sig");

        createSignature(providerPKCS11, pk, chain, appearance);
    }

    private Provider buildProviderConfig() throws IOException {
        String config = "name=ikey4000\n"
                + "library=" + execPath + "\n"
                + "slotListIndex = " + getSlotsWithTokens(execPath)[0];

        ByteArrayInputStream bais = new ByteArrayInputStream(config.getBytes());
        Provider providerPKCS11 = new SunPKCS11(bais);
        Security.addProvider(providerPKCS11);
        return providerPKCS11;
    }

    private void createSignature(Provider providerPKCS11, PrivateKey pk, Certificate[] chain, PdfSignatureAppearance appearance) throws IOException, DocumentException, GeneralSecurityException {
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, DigestAlgorithms.SHA256, providerPKCS11.getName());
        MakeSignature.signDetached(appearance, digest, signature, chain, null, null, null, 0, MakeSignature.CryptoStandard.CMS);
    }

    private Rectangle createSignatureRectangle(PdfReader reader) {
        Rectangle pageSize = reader.getPageSizeWithRotation(1);
        float top = pageSize.getTop();
        if (top == 0) {
            top = pageSize.getHeight();
        }

        Rectangle rectangle = new Rectangle(20, top - 60, 250, top - 10);
        rectangle.setBorder(Rectangle.BOX);
        rectangle.setBorderWidth(1);
        rectangle.setBorderColor(BaseColor.BLACK);
        return rectangle;
    }

    private PdfSignatureAppearance createSignatureAppearance(PdfStamper stamper) {
        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
        appearance.setContact(contact);
        appearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);
        return appearance;
    }

    private static long[] getSlotsWithTokens(String libraryPath) throws IOException {
        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
        String functionList = "C_GetFunctionList";
        initArgs.flags = 0;
        PKCS11 tmpPKCS11 = null;
        long[] slotList = null;
        try {
            try {
                tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList, initArgs, false);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw ex;
            }
        } catch (PKCS11Exception e) {
            try {
                initArgs = null;
                tmpPKCS11 =
                        PKCS11.getInstance(libraryPath, functionList, initArgs, true);
            } catch (IOException | PKCS11Exception ex) {
                ex.printStackTrace();
            }
        }
        try {
            slotList = tmpPKCS11.C_GetSlotList(true);
            for (long slot : slotList) {
                CK_TOKEN_INFO tokenInfo = tmpPKCS11.C_GetTokenInfo(slot);
                System.out.println("slot: " + slot + "\nmanufacturerID: "
                        + String.valueOf(tokenInfo.manufacturerID) + "\nmodel: "
                        + String.valueOf(tokenInfo.model));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return slotList;
    }

}
