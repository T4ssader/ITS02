import java.io.*;
import java.nio.file.*;
import java.util.Arrays;

public class SecureFile {
    private TripleDES tripleDES;
    private byte[] iv;

    public SecureFile(byte[] key, byte[] iv) {
        if (key.length != 24 || iv.length != 8) {
            throw new IllegalArgumentException("Invalid key or IV length");
        }
        byte[] keyPart1 = Arrays.copyOfRange(key, 0, 8);
        byte[] keyPart2 = Arrays.copyOfRange(key, 8, 16);
        byte[] keyPart3 = Arrays.copyOfRange(key, 16, 24);
        this.tripleDES = new TripleDES(keyPart1, keyPart2, keyPart3);
        this.iv = iv;
    }

    public void processFile(String inputFilePath, String outputFilePath, String operation) throws IOException {
        byte[] inputBytes = Files.readAllBytes(Paths.get(inputFilePath));
        byte[] outputBytes = new byte[inputBytes.length];

        byte[] feedback = iv.clone();

        for (int i = 0; i < inputBytes.length; i += 8) {
            byte[] block = Arrays.copyOfRange(inputBytes, i, Math.min(i + 8, inputBytes.length));
            byte[] encryptedFeedback = tripleDES.encryptBytes(feedback);

            if (operation.equalsIgnoreCase("encrypt")) {
                byte[] ciphertextBlock = xor(block, encryptedFeedback);
                System.arraycopy(ciphertextBlock, 0, outputBytes, i, ciphertextBlock.length);
                feedback = ciphertextBlock;
            } else if (operation.equalsIgnoreCase("decrypt")) {
                byte[] plaintextBlock = xor(block, encryptedFeedback);
                System.arraycopy(plaintextBlock, 0, outputBytes, i, plaintextBlock.length);
                feedback = block;
            } else {
                throw new IllegalArgumentException("Invalid operation: " + operation);
            }
        }

        Files.write(Paths.get(outputFilePath), outputBytes);
    }

    private byte[] xor(byte[] a, byte[] b) {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage: java SecureFile <inputFile> <keyFile> <outputFile> <encrypt|decrypt>");
            return;
        }

        String inputFile = args[0];
        String keyFile = args[1];
        String outputFile = args[2];
        String operation = args[3];

        try {
            byte[] keyAndIv = Files.readAllBytes(Paths.get(keyFile));
            if (keyAndIv.length != 32) {
                throw new IllegalArgumentException("Key file must be exactly 32 bytes long");
            }
            byte[] key = Arrays.copyOfRange(keyAndIv, 0, 24);
            byte[] iv = Arrays.copyOfRange(keyAndIv, 24, 32);

            SecureFile secureFile = new SecureFile(key, iv);
            secureFile.processFile(inputFile, outputFile, operation);
            System.out.println(operation + "ion completed successfully.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
