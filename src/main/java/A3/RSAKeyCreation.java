package A3;

import java.security.spec.*;
import java.security.*;
import java.io.*;


public class RSAKeyCreation {
    public static void createKey(String[] args) {
        if( args.length != 1){
            System.out.println(String.format("%d arguments given. Only input 1 owner name", args.length));
            return;
        }
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(4096);
            KeyPair keypair = keyGen.genKeyPair();
            PrivateKey privateKey = keypair.getPrivate();
            PublicKey publicKey = keypair.getPublic();
            System.out.println("Public key: " + publicKey.toString());
            System.out.println("Private key: " + privateKey.toString());

            byte[] privateKeyBytes = privateKey.getEncoded();
            byte[] publicKeyBytes = publicKey.getEncoded();

            System.out.println("Public key format" + publicKey.getFormat());
            System.out.println("Private key format" + privateKey.getFormat());

            DataOutputStream pub = new DataOutputStream(new FileOutputStream(owner + ".pub"));
            DataOutputStream priv = new DataOutputStream(new FileOutputStream(owner + ".prv"));

            //writing public key data
            pub.writeInt(owner.length());
            pub.writeBytes(owner);
            pub.writeInt(publicKeyBytes.length);
            pub.write(publicKeyBytes);

            //writing private key data
            priv.writeInt(owner.length());
            priv.writeBytes(owner);
            priv.writeInt(privateKeyBytes.length);
            priv.write(privateKeyBytes);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void ReadKey(String owner) {
        try {
            DataInputStream pub = new DataInputStream(new FileInputStream(owner + ".pub"));
            DataInputStream priv = new DataInputStream(new FileInputStream(owner + ".prv"));

            int ownerLength = pub.readInt();
            System.out.println("Owner length: " + ownerLength);
            byte[] ownerBytes = new byte[ownerLength];
            pub.read(ownerBytes, 0, ownerLength);
            String ownerName = new String(ownerBytes);
            System.out.println("Owner: " + ownerName);

            int publicKeyLength = pub.readInt();
            System.out.println("Public key length: " + publicKeyLength);
            byte[] publicKeyBytes = new byte[publicKeyLength];
            pub.read(publicKeyBytes, 0, publicKeyLength);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            System.out.println("Public key: " + publicKey.toString());

            //read private key data
            ownerLength = priv.readInt();
            ownerBytes = new byte[ownerLength];
            priv.read(ownerBytes, 0, ownerLength);
            ownerName = new String(ownerBytes);
            int privateKeyLength = priv.readInt();
            byte[] privateKeyBytes = new byte[privateKeyLength];
            priv.read(privateKeyBytes, 0, privateKeyLength);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
            System.out.println("Private key: " + privateKey.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        createKey(args);
    }

}
