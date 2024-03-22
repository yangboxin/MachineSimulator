package panel2.panel1;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.Arrays;
import java.util.HashSet;

public class CPU {
    public String[] GPR = new String[4];
    public String[] IXR = new String[3];
    public String PC = "000000000000";
    public String MAR = "000000000000";
    public String MBR = "0000000000000000";
    public String IR = "0000000000000000";
    public String CC = "0000";
    public String MFR = "0000";
    private HashSet<String> LoadStore;
    public CPU() {

    }

    public void init(){
        Arrays.fill(GPR, "0000000000000000");
        Arrays.fill(IXR, "0000000000000000");
        PC = "000000000000";
        MAR = "000000000000";
        MBR = "0000000000000000";
        IR = "0000000000000000";
        CC = "0000";
        MFR = "0000";
        LoadStore = new HashSet<>();
        LoadStore.add("000001");
        LoadStore.add("000010");
        LoadStore.add("000011");
        LoadStore.add("000100");
        LoadStore.add("000101");
    }

    public void setGPR(int index, String content){
        GPR[index]=content;
    }
    public void setIXR(int index, String content){
        IXR[index]=content;
    }
    public void setPC(String content){
        PC=content;
    }
    public void setMAR(String content){
        MAR=content;
    }
    public void setMBR(String content) {
        MBR=content;
    }
    public void setIR(String content) {
        IR=content;
    }
    public void setCC(String content) {
        CC=content;
    }
    public void setMFR(String content) {
        MFR=content;
    }
    public String getGPR(int index){
        return GPR[index];
    }
    public String getIXR(int index){
        return IXR[index];
    }
    public String getPC() {
        return PC;
    }
    public String getMAR() {
        return MAR;
    }
    public String getMBR() {
        return MBR;
    }
    public String getIR() {
        return IR;
    }
    public String getCC() {
        return CC;
    }
    public String getMFR() {
        return MFR;
    }

    private int parse(String instruction, Memory memory){
        // input is instruction's binary form
        if(instruction.length()!=16)
            return 10; // wrong format; reserved for debug
        String opcode = instruction.substring(0,6);
        if(LoadStore.contains(opcode)){
            return handleLoadStore(instruction, memory);
        }
        // easy to develop other instructions under this structure
        return 1;// error;
    }
    public int step(Memory memory){
        String addressBin = PC;
        int addressDec = Integer.parseInt(addressBin, 2);
        String instructionBin = memory.getInstruction(addressDec);
        if(instructionBin.substring(0,6).equals("000000")){
            memory.setMemoryContent(addressDec, String.format("%16s", instructionBin.substring(6)).replace(' ', '0'));
        }
        int res = parse(instructionBin, memory);
        addressDec++;
        String incrementAdd = String.format("%12s",Integer.toBinaryString(addressDec)).replace(' ','0');
        PC=incrementAdd;
        MBR=memory.getInstruction(addressDec);
        MAR=incrementAdd;
        if(addressDec==memory.getEndAddress()){
            PC=(String.format("%16s", Integer.toBinaryString(memory.getFirstAddress())).replace(' ', '0'));
        }
        return 0;
    }
    private int handleLoadStore(String instruction, Memory memory){
        // Extracting opcode
        String opcode = instruction.substring(0, 6);

        // Extracting general purpose registers
        String generalRegisters = instruction.substring(6, 8);

        // Extracting index registers
        String indexRegisters = instruction.substring(8, 10);

        // Extracting addressing bit
        String addressingMode = instruction.substring(10, 11);

        // Extracting address
        String address = instruction.substring(11);

        // Calculate effective address (EA)
        int EA = calculateEA(indexRegisters, addressingMode, address, memory);
        if(EA<0){
            return -1; // unreachable EA
        }
        int index=0;
        String fromMem="0";
        switch (opcode) {
            case "000001"://LDR r,x,address,[i]
                fromMem = memory.getMemoryContent(EA);
                fromMem=(String.format("%16s", fromMem).replace(' ', '0'));
                index=Integer.parseInt(generalRegisters,2);
                GPR[index]=fromMem;
                return 0;
            case "000010"://STR r,x,address,[i]
                index=Integer.parseInt(generalRegisters,2);
                memory.setMemoryContent(EA, String.format("%16s", GPR[index]).replace(' ', '0'));
                return 0;
            case "000011"://LDA r,x,address,[i]
                index=Integer.parseInt(generalRegisters,2);
                GPR[index]=(String.format("%16s", address).replace(' ', '0'));
                return 0;
            case "000100"://LDX x,address,[i]
                fromMem = memory.getMemoryContent(EA);
                index=Integer.parseInt(indexRegisters,2);
                IXR[index]=(String.format("%16s", fromMem).replace(' ', '0'));
                return 0;
            case "000101"://STX x,address,[i]
                index=Integer.parseInt(indexRegisters,2);
                memory.setMemoryContent(EA, String.format("%16s", IXR[index]).replace(' ', '0'));
                return 0;
            default:
                return 1;
        }
    }
    private int calculateEA(String indexRegisters, String addressingMode, String address, Memory memory){
        int effectiveAddress = 0;
        if (addressingMode.equals("0")) {
            // Direct addressing mode
            if (indexRegisters.equals("00")) {
                effectiveAddress = Integer.parseInt(address,2);
            }
            else {
                // Convert binary strings to integers and calculate the effective address
                int index=Integer.parseInt(indexRegisters,2);
                String contentBin = IXR[index];
                int contentDec = Integer.parseInt(contentBin, 2);
                int addressDec = Integer.parseInt(address, 2);
                int ea = contentDec+addressDec;
                if(ea>memory.getMemorySize()){
                    return -2; // memory index out of range
                }
                effectiveAddress = ea;
            }
        }
        else {
            // Indirect addressing mode
            // for now this does not work (need memory)
            if (indexRegisters.equals("00")) {
                int addressDec = Integer.parseInt(address,2);
                effectiveAddress = Integer.parseInt(memory.getMemoryContent(addressDec), 2);
            }
            else {
                // Convert binary strings to integers and calculate the effective address
                int index=Integer.parseInt(indexRegisters,2);
                String contentBin = IXR[index];
                int contentDec = Integer.parseInt(contentBin, 2);
                int addressDec = Integer.parseInt(address,2);
                int indexedAdd = contentDec+addressDec;
                if(indexedAdd>memory.getMemorySize()){
                    return 4096;// memory index out of range
                }
//                effectiveAddress = Integer.parseInt(memory[indexedAdd],2);
                effectiveAddress = Integer.parseInt(memory.getMemoryContent(indexedAdd), 2);

            }
        }
        if(effectiveAddress<=5 && effectiveAddress>=0){
            return -1;
        }
        return effectiveAddress;
    }
}
