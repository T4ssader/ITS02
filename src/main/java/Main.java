import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        int randomInt;

        for(int i = 0; i <= 4; i++) {
            randomInt = (int) ((Math.random() * (100 - 1)) + 1);
            System.out.println("Startwert: " + randomInt);
            LCG lcg = new LCG(randomInt, 256);
//            System.out.println(lcg.getNumberStore());
            lcg.testQuality();
        }

        randomInt = (int) ((Math.random() * (100 - 1)) + 1);
        HC1 hc1 = new HC1(randomInt, Path.of("src/main/resources/test.txt"));
        hc1.deEnCrypt();

        hc1 = new HC1(randomInt, Path.of("src/main/resources/test.txt.enc"));
        hc1.deEnCrypt();

    }
}
