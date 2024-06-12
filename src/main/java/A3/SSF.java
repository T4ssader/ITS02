package A3;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class SSF {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java SSF <private key file> <public key file> <input file> <output file>");
            return;
        }
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        try {
            // Step a: Read the RSA private key
            PrivateKey privateKey = readPrivateKey(args[0]);

            // Step b: Read the RSA public key
            PublicKey publicKey = readPublicKey(args[1]);

            // Step c: Generate a secret AES key (256-bit)
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey aesKey = keyGen.generateKey();

            // Step d: Sign the AES key with the private RSA key (SHA512withRSA)
            Signature signature = Signature.getInstance("SHA512withRSA");
            signature.initSign(privateKey);
            signature.update(aesKey.getEncoded());
            byte[] aesKeySignature = signature.sign();

            // Step e: Encrypt the AES key with the public RSA key (RSA)
            Cipher rsaCipher = Cipher.getInstance("RSA");
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());

            // Step f: Encrypt the document file with the AES key (AES/CTR/NoPadding)
            byte[] iv = new byte[16];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher aesCipher = Cipher.getInstance("AES/CTR/NoPadding");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, ivSpec);

            byte[] inputFileData = Files.readAllBytes(new File(args[2]).toPath());
            byte[] encryptedFileData = aesCipher.doFinal(inputFileData);

            // Create the output file with the specified structure
            try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(args[3]))) {
                outputStream.writeInt(encryptedAesKey.length);
                outputStream.write(encryptedAesKey);
                outputStream.writeInt(aesKeySignature.length);
                outputStream.write(aesKeySignature);
                outputStream.writeInt(iv.length);
                outputStream.write(iv);
                outputStream.write(encryptedFileData);
            }

            System.out.println("File encrypted and written to " + args[3]);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static PrivateKey readPrivateKey(String filename) throws Exception {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filename))) {
            int nameLength = inputStream.readInt();
            byte[] nameBytes = new byte[nameLength];
            inputStream.readFully(nameBytes);

            int keyLength = inputStream.readInt();
            byte[] keyBytes = new byte[keyLength];
            inputStream.readFully(keyBytes);

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }
    }

    private static PublicKey readPublicKey(String filename) throws Exception {
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filename))) {
            int nameLength = inputStream.readInt();
            byte[] nameBytes = new byte[nameLength];
            inputStream.readFully(nameBytes);

            int keyLength = inputStream.readInt();
            byte[] keyBytes = new byte[keyLength];
            inputStream.readFully(keyBytes);

            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        }
    }
}
