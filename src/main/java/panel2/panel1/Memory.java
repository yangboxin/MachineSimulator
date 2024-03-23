package panel2.panel1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
public class Memory {
    private static final int MEMORY_SIZE = 2048;
    private String[] memory;
    private int endAdd = 6;
    private int firstAdd = 6;
    private ObservableList<CacheEntry> cache;
    private static final int CACHE_SIZE = 16;

    public Memory() {
        memory = new String[MEMORY_SIZE];
        cache = FXCollections.observableArrayList();
        initializeMemory();
    }

    private void initializeMemory() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = "0000000000000000";
        }
        for(int i=0;i< CACHE_SIZE;i++){
            addToCache("0","0");
        }
    }

    public String getMemoryContent(int address) {
        String tag = Integer.toString(address);
        // check cache
        for (CacheEntry entry : cache) {
            if (entry.getTag().equals(tag)) {
                // cache hits
                return entry.getValue();
            }
        }
        // cache miss
        String data = this.memory[address]; // read from mem
        addToCache(tag, data);
        return data;
    }

    public void setMemoryContent(int address, String content) {
        String tag = Integer.toString(address);
        // check cache
        for (CacheEntry entry : cache) {
            if (entry.getTag().equals(tag)) {
                // cache hits
                entry.setValue(content);
                this.memory[address]=String.format("%16s",content).replace(' ','0');
                return;
            }
        }
        // cache miss
        this.memory[address]=String.format("%16s",content).replace(' ','0');
        addToCache(tag, this.memory[address]);
    }

    public int getMemorySize() {
        return MEMORY_SIZE;
    }

    public void loadInstructionsFromFile(String filePath) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String str;
            int lineCnt = 0;
            while ((str = in.readLine()) != null) {
                if (lineCnt == 0) {
                    firstAdd = Integer.parseInt(str.split(" ")[0], 8);
                    lineCnt++;
                }
                String addressOct = str.split(" ")[0];
                String instructionOct = str.split(" ")[1];
                int addressDec = Integer.parseInt(addressOct, 8);
                String instructionBin = String.format("%16s", Integer.toBinaryString(Integer.parseInt(instructionOct, 8))).replace(' ', '0');
                if (addressDec >= MEMORY_SIZE) {
                    System.out.println("Address index out of range");
                    break;
                }
                setMemoryContent(addressDec, instructionBin);
                endAdd = addressDec;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getEndAddress() {
        return endAdd;
    }

    public int getFirstAddress() {
        return firstAdd;
    }
    public void addToCache(String tag, String data) {
        // （FIFO）
        if (cache.size() >= CACHE_SIZE) {
            cache.removeFirst();
        }
        cache.add(new CacheEntry(tag, data));
    }
    public ObservableList<CacheEntry> getCache() {
        return cache;
    }
}