package panel2.panel1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Memory {
    private static final int MEMORY_SIZE = 2048;
    private String[] memory;
    private int endAdd = 6;
    private int firstAdd = 6;

    public Memory() {
        memory = new String[MEMORY_SIZE];
        initializeMemory();
    }

    private void initializeMemory() {
        for (int i = 0; i < MEMORY_SIZE; i++) {
            memory[i] = "0000000000000000";
        }
    }

    public String getInstruction(int address) {
        return memory[address];
    }

    public void setInstruction(int address, String instruction) {
        memory[address] = instruction;
    }

    public String getMemoryContent(int address) {
        return memory[address];
    }

    public void setMemoryContent(int address, String content) {
        memory[address] = content;
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
                setInstruction(addressDec, instructionBin);
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
}