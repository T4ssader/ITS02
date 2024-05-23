import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class SecureFile {
    private TripleDES des;
    private byte[] init_vector = new byte[8];
    private String operation;

    public SecureFile(String key_file, String _operation){
        try {
            readKey(key_file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        operation = _operation;
    }

    private void readKey(String key_file) throws IOException {
        FileInputStream key_stream = new FileInputStream(key_file);
        System.out.println("Key file: " + key_file);

        byte[] key_bytes;
        key_bytes = key_stream.readAllBytes();
        System.out.println("Key bytes: " + key_bytes.length);

        if(key_bytes.length != 32){
            throw new IOException("Key file must contain 32 bytes.");
        }

        byte[] key1 = new byte[8];
        byte[] key2 = new byte[8];
        byte[] key3 = new byte[8];

        System.arraycopy(key_bytes, 0, key1, 0, 8);
        System.arraycopy(key_bytes, 8, key2, 0, 8);
        System.arraycopy(key_bytes, 16, key3, 0, 8);
        System.arraycopy(key_bytes, 24, init_vector, 0, 8);


        des = new TripleDES(key1, key2, key3);
    }

    public void processFile(String inputFilePath, String outputFilePath) throws IOException {

        FileInputStream in = new FileInputStream(inputFilePath);
        FileOutputStream out = new FileOutputStream(outputFilePath);
        byte[] buffer = new byte[8];
        int len;
        byte[] feedback = init_vector.clone();

        while((len = in.read(buffer)) > 0) {
            byte[] block = buffer.clone();
            if (len < 8) {
                block = Arrays.copyOf(block, 8);
            }
            byte[] encrypted_feedback = des.encryptBytes(feedback);

            if (operation.equals("encrypt")) {
                byte[] cyber_block = myxor(block, encrypted_feedback);
                out.write(cyber_block, 0, len);
                feedback = cyber_block;
            }
            else if (operation.equals("decrypt")){
                byte[] plain_block = myxor(block, encrypted_feedback);
                out.write(plain_block, 0, len);
                feedback = block;
            }
            else{
                throw new IOException("Invalid operation: " + operation);
            }
        }
        in.close();
        out.close();

    }

    private byte[] myxor (byte[] a, byte[] b){
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++){
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
    public static void main(String[] args) throws IOException {
        if(args.length < 4){
            System.err.printf("Not enough arguments. %d arguments given, 4 arguments needed.%n", args.length);
            return;
        }

        String input_file = args[0];
        String key_file = args[1];
        String output_file = args[2];
        String operation = args[3];


        SecureFile sf = new SecureFile(key_file, operation);
        sf.processFile(input_file, output_file);
    }
}
