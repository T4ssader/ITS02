import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        LCG lcg = new LCG(4, 256);
        System.out.println(lcg.getNumberStore());
        lcg.testQuality();


        HC1 hc1 = new HC1(4, Path.of("src/main/resources/test.txt"));
        hc1.deEnCrypt();

        hc1 = new HC1(4, Path.of("src/main/resources/test.txt.enc"));
        hc1.deEnCrypt();

    }
}