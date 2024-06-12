package A3;


import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSF {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java RSF <private key file> <public key file> <input .ssf file> <output file>");
            return;
        }

        try {
            // Step a: Read the RSA public key
            PublicKey publicKey = readPublicKey(args[1]);

            // Step b: Read the RSA private key
            PrivateKey privateKey = readPrivateKey(args[0]);

            // Step c: Read the .ssf file and decrypt the secret key and file data
            try (DataInputStream inputStream = new DataInputStream(new FileInputStream(args[2]))) {
                int encryptedKeyLength = inputStream.readInt();
                byte[] encryptedAesKey = new byte[encryptedKeyLength];
                inputStream.readFully(encryptedAesKey);

                int signatureLength = inputStream.readInt();
                byte[] aesKeySignature = new byte[signatureLength];
                inputStream.readFully(aesKeySignature);

                int ivLength = inputStream.readInt();
                byte[] iv = new byte[ivLength];
                inputStream.readFully(iv);

                byte[] encryptedFileData = new byte[inputStream.available()];
                inputStream.readFully(encryptedFileData);

                // Decrypt the AES key with the private RSA key
                Cipher rsaCipher = Cipher.getInstance("RSA");
                rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] aesKeyBytes = rsaCipher.doFinal(encryptedAesKey);

                SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");

                // Verify the signature of the AES key
                Signature signature = Signature.getInstance("SHA512withRSA");
                signature.initVerify(publicKey);
                signature.update(aesKey.getEncoded());
                boolean isValid = signature.verify(aesKeySignature);

                if (!isValid) {
                    System.err.println("The signature of the AES key is invalid.");
                    return;
                }

                // Decrypt the file data with the AES key
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                Cipher aesCipher = Cipher.getInstance("AES/CTR/NoPadding");
                aesCipher.init(Cipher.DECRYPT_MODE, aesKey, ivSpec);
                byte[] decryptedFileData = aesCipher.doFinal(encryptedFileData);

                // Write the decrypted data to the output file
                Files.write(new File(args[3]).toPath(), decryptedFileData);
                System.out.println("File decrypted and written to " + args[3]);
            }
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
