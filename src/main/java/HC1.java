import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HC1 {
    int start;
    Path path;
    public HC1(int start, Path path) {
        this.start = start;
        this.path = path;
    }

    public void deEnCrypt() throws IOException {
        path = Paths.get(path.toUri());
        byte[] data = Files.readAllBytes(path);
        byte[] coded = new byte[data.length];
        LCG lcg = new LCG(start);

        for(int i=0; i < data.length; i++){
            coded[i] = (byte) (lcg.nextInt() ^ data[i]);
        }

        Path out;
        if(path.toString().endsWith(".enc")){
            out = Path.of(path.getParent() + "/copyOf_" + (path.getFileName().toString().replace(".enc", "")));
        }else {
            out = Path.of(path + ".enc");
        }
        Files.write(out, coded);
    }
}
