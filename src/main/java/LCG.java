import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LCG {
    private int a = 214013;
    private int b = 13523655;
    private int m = 16777216;
    private long x;
    private int numberCount = 0;
    private List<Integer> numberStore;
    public List<Integer> getNumberStore() {
        return numberStore;
    }

    public LCG(int start) {
        this.x = start;
    }
    public LCG(int start, int numberCount) {
        this.x = start;
        this.numberCount = numberCount;
        this.numberStore = new ArrayList<>(numberCount);

        for(int i = 0; i < numberCount; i++){
            numberStore.add(nextInt());
        }
    }
    public int nextInt(){
        this.x = (a * this.x + b) % m;
        long longX = this.x;
        return (int) longX;
    }

    public void testQuality(){
        HashSet<Integer> testSet = new HashSet<>();
        for(int i = 0; i < this.numberCount; i++){
            testSet.add(this.numberStore.get(i) & 0x000000FF);
        }

        System.out.println("Integer in HashSet: " + testSet.size());
    }

}
