package panel2.panel1;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class MachineController extends Application {
    // memory layout
    private String[] memory = new String[2048];
    private String[] GPR = new String[4];
    private String[] IXR = new String[3];
    private String PC = "000000000000";
    private String MAR = "000000000000";
    private String MBR = "0000000000000000";
    private String IR = "0000000000000000";
    private String CC = "0000";
    private String MFR = "0000";

    private Task<Void> runningTask; //
    private Thread runningThread;
    private HashSet<String> LoadStore;
    private HashMap<String, TextField> regMap;
    private HashMap<String, TextField> ixrMap;
    @FXML
    private Button runButton;

    @FXML
    private Button iplButton;

    @FXML
    private Button haltButton;

    @FXML
    private Button stepButton;

    @FXML
    private Button loadButton;

    @FXML
    private Button storeButton;

    @FXML
    private Button loadPlusButton;

    @FXML
    private Button storePlusButton;

    // Add other FXML elements as needed
    @FXML
    private TextField TFGPR0;
    @FXML
    private TextField TFGPR1;
    @FXML
    private TextField TFGPR2;
    @FXML
    private TextField TFGPR3;
    @FXML
    private TextField TFIXR0;
    @FXML
    private TextField TFIXR1;
    @FXML
    private TextField TFIXR2;
    @FXML
    private TextField TFBINARY;
    @FXML
    private TextField TFOCTAL;
    @FXML
    private TextField TFPC;
    @FXML
    private TextField TFMAR;
    @FXML
    private TextField TFMBR;
    @FXML
    private TextField TFIR;
    @FXML
    private TextField TFCC;
    @FXML
    private TextField TFMFR;
    @FXML
    private Button GPR0;
    @FXML
    private Button GPR1;
    @FXML
    private Button GPR2;
    @FXML
    private Button GPR3;
    @FXML
    private Button IXR0;
    @FXML
    private Button IXR1;
    @FXML
    private Button IXR2;
    @FXML
    private Button PC_Button;
    @FXML
    private Button MAR_Button;
    @FXML
    private Button MBR_Button;
    @FXML
    private Button IR_Button;

    public void init(){
        // initialization for memory-related
        Arrays.fill(memory, "0000000000000000");
        Arrays.fill(GPR, "0000000000000000");
        Arrays.fill(IXR, "0000000000000000");
        regMap = new HashMap<>();
        regMap.put("00", TFGPR0);
        regMap.put("01", TFGPR1);
        regMap.put("10", TFGPR2);
        regMap.put("11", TFGPR3);
        ixrMap = new HashMap<>();
        ixrMap.put("00", TFIXR0);
        ixrMap.put("01", TFIXR1);
        ixrMap.put("10", TFIXR2);
        LoadStore = new HashSet<>();
        LoadStore.add("000001");
        LoadStore.add("000010");
        LoadStore.add("000011");
        LoadStore.add("000100");
        LoadStore.add("000101");
    }
    @FXML
    private void initialize() {
        //initialization for FXML-related
        TFOCTAL.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String input = TFOCTAL.getText(); // get user input
                if (input.matches("^[0-7]+$") && input.length() <= 6) { // check for 8 bit octal
                    // transform to binary
                    String binaryString = Integer.toBinaryString(Integer.parseInt(input, 8));
                    // padding with 0
                    String paddedBinaryString = String.format("%16s", binaryString).replace(' ', '0');
                    TFBINARY.setText(paddedBinaryString); // copy to binary textfield
                } else {
                    TFOCTAL.setText("Invalid input!");
                }
            }
        });
        GPR0.setOnAction(event -> {
            TFGPR0.setText(TFBINARY.getText());
            GPR[0] = TFBINARY.getText(); // update GPR[]
        });
        GPR1.setOnAction(event -> {
            TFGPR1.setText(TFBINARY.getText());
            GPR[1] = TFBINARY.getText(); // update GPR[]
        });
        GPR2.setOnAction(event -> {
            TFGPR2.setText(TFBINARY.getText());
            GPR[2] = TFBINARY.getText(); // update GPR[]
        });
        GPR3.setOnAction(event -> {
            TFGPR3.setText(TFBINARY.getText());
            GPR[3] = TFBINARY.getText(); // update GPR[]
        });
        IXR0.setOnAction(event -> {
            TFIXR0.setText(TFBINARY.getText());
            IXR[0] = TFBINARY.getText(); // update IXR[]
        });
        IXR1.setOnAction(event -> {
            TFIXR1.setText(TFBINARY.getText());
            IXR[1] = TFBINARY.getText(); // update IXR[]
        });
        IXR2.setOnAction(event -> {
            TFIXR2.setText(TFBINARY.getText());
            IXR[2] = TFBINARY.getText(); // update IXR[]
        });
        PC_Button.setOnAction(event -> {
            String binaryData = TFBINARY.getText();
            binaryData = String.format("%12s", binaryData).replace(' ', '0');
            String lowest12bits = binaryData.substring(binaryData.length()-12);
            TFPC.setText(lowest12bits);
            PC = lowest12bits; // update pc
        });
        MAR_Button.setOnAction(event -> {
            String binaryData = TFBINARY.getText();
            binaryData = String.format("%12s", binaryData).replace(' ', '0');
            String lowest12bits = binaryData.substring(binaryData.length()-12);
            TFMAR.setText(lowest12bits);
            MAR = lowest12bits; // update MAr
        });
        MBR_Button.setOnAction(event -> {
            TFMBR.setText(TFBINARY.getText());
            MBR = TFBINARY.getText(); // update MBR
        });
        IR_Button.setOnAction(event -> {
            TFIR.setText(TFBINARY.getText());
            IR = TFBINARY.getText(); // update IR
        });
    }
    private int parse(String instruction){
        // input is instruction's binary form
        if(instruction.length()!=16)
            return 10; // wrong format; reserved for debug
        String opcode = instruction.substring(0,6);
        if(LoadStore.contains(opcode)){
            return handleLoadStore(instruction);
        }
        // easy to develop other instructions under this structure
        return 1;// error;
    }
    private int calculateEA(String indexRegisters, String addressingMode, String address){
        int effectiveAddress = 0;
        if (addressingMode.equals("0")) {
            // Direct addressing mode
            if (indexRegisters.equals("00")) {
                effectiveAddress = Integer.parseInt(address,2);
            }
            else {
                // Convert binary strings to integers and calculate the effective address
                TextField IDXR = ixrMap.get(indexRegisters);
                String contentBin = IDXR.getText();
                int contentDec = Integer.parseInt(contentBin, 2);
                int addressDec = Integer.parseInt(address, 2);
                int ea = contentDec+addressDec;
                if(ea>2048){
                    return 4096; // memory index out of range
                }
                effectiveAddress = ea;
            }
        }
        else {
            // Indirect addressing mode
            // for now this does not work (need memory)
            if (indexRegisters.equals("00")) {
                int addressDec = Integer.parseInt(address,2);
                effectiveAddress = Integer.parseInt(memory[addressDec],2);
            }
            else {
                // Convert binary strings to integers and calculate the effective address
                TextField IDXR = ixrMap.get(indexRegisters);
                String contentBin = IDXR.getText();
                int contentDec = Integer.parseInt(contentBin, 2);
                int addressDec = Integer.parseInt(address,2);
                int indexedAdd = contentDec+addressDec;
                if(indexedAdd>2048){
                    return 4096;// memory index out of range
                }
                effectiveAddress = Integer.parseInt(memory[indexedAdd],2);
            }
        }
        return effectiveAddress;
    }
    private int handleLoadStore(String instruction){
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
        int EA = calculateEA(indexRegisters, addressingMode, address);

        switch (opcode) {
            case "000001"://LDR r,x,address,[i]
                String fromMem = memory[EA];
                regMap.get(generalRegisters).setText(String.format("%16s",fromMem).replace(' ','0'));
                return 0;
            case "000010"://STR r,x,address,[i]
                memory[EA] = String.format("%16s",regMap.get(generalRegisters).getText()).replace(' ','0');
                return 0;
            case "000011"://LDA r,x,address,[i]
                regMap.get(generalRegisters).setText(String.format("%16s", address).replace(' ','0'));
                return 0;
            case "000100"://LDX x,address,[i]
                ixrMap.get(indexRegisters).setText(String.format("%16s",memory[EA]).replace(' ','0'));
                return 0;
            case "000101"://STX x,address,[i]
                memory[EA] = String.format("%16s",ixrMap.get(indexRegisters).getText()).replace(' ','0');
                return 0;
            default:
                return 1;
        }
    }
    @FXML
    private void handleRunButton() {
        // concurrency via Task
        runningTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while(!isCancelled()){
                    // HALT controls isCancelled()
                    String addressBin = TFPC.getText();
                    int addressDec = Integer.parseInt(addressBin, 2);
                    String instructionBin = memory[addressDec];
                    String dataChk = instructionBin.substring(0,6);
                    if(dataChk.equals("000000")){// check if input is data
                        memory[addressDec]=instructionBin.substring(6);
                    }
                    int res = parse(instructionBin);// deliver to parse to determine which instruction this is and execute
                    addressDec++;
                    if(addressDec>2048){
                        runningTask.cancel();
                        break;
                    }
                    String incrementAdd = String.format("%12s",Integer.toBinaryString(addressDec)).replace(' ','0');
                    TFPC.setText(incrementAdd);
                    Thread.sleep(1000);// sleep 1s each cycle for a clear display in panel
                }
                return null;
            }
        };
        runningTask.setOnSucceeded(e -> System.out.println("Task completed successfully."));
        runningTask.setOnCancelled(e -> System.out.println("Task was cancelled."));
        // start in new thread
        runningThread = new Thread(runningTask);
        runningThread.start();
    }

    @FXML
    private void handleIPLButton() {
        // Initialize the machine, clear memory
        init();
        TFGPR0.setText(String.format("%016d", 0));
        TFGPR1.setText(String.format("%016d", 0));
        TFGPR2.setText(String.format("%016d", 0));
        TFGPR3.setText(String.format("%016d", 0));
        TFIXR0.setText(String.format("%016d", 0));
        TFIXR1.setText(String.format("%016d", 0));
        TFIXR2.setText(String.format("%016d", 0));
        TFPC.setText(String.format("%012d",0));
        TFMAR.setText(String.format("%012d",0));
        TFMBR.setText(String.format("%016d", 0));
        TFIR.setText(String.format("%016d", 0));
        TFCC.setText(String.format("%04d", 0));
        TFMFR.setText(String.format("%04d", 0));

        //read from the input file and put each line of instruction into the memory
        int firstIns = 6; // default start location
        try {
            String filePath = getClass().getClassLoader().getResource("LoadFile.txt").getPath();
            BufferedReader in = new BufferedReader(new FileReader(filePath));
            String str;
            int lineCnt=0;
            while ((str = in.readLine()) != null) {
                if(lineCnt==0){// check for explicit start location
                    firstIns = Integer.parseInt(str.split(" ")[0],8);
                    lineCnt++;
                }
                String addressOct = str.split(" ")[0];
                String instructionOct = str.split(" ")[1];
                int addressDec = Integer.parseInt(addressOct, 8);
                String instructionBin = String.format("%16s",Integer.toBinaryString(Integer.parseInt(instructionOct, 8))).replace(' ', '0');
                if(addressDec>2048){
                    System.out.println("address index out of range");
                    break;
                }
                memory[addressDec] = instructionBin;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String firstLocation = String.format("%12s",Integer.toBinaryString(firstIns)).replace(' ','0');
        TFPC.setText(firstLocation);
        System.out.println("The IPL button is pressed");
    }

    @FXML
    private void handleHaltButton() {
        // HALT cancels run
        if (runningTask != null && runningTask.isRunning()) {
            runningTask.cancel();
        }
        System.out.println("The Halt button is pressed");
    }

    @FXML
    private void handleStepButton() {
        // execute the instruction at PC location and increment PC
        String addressBin = TFPC.getText();
        int addressDec = Integer.parseInt(addressBin, 2);
        String instructionBin = memory[addressDec];
        if(instructionBin.substring(0,6).equals("000000")){
            memory[addressDec]=String.format("%16s",instructionBin.substring(6)).replace(' ','0');
        }
        int res = parse(instructionBin);
        addressDec++;
        String incrementAdd = String.format("%12s",Integer.toBinaryString(addressDec)).replace(' ','0');
        TFPC.setText(incrementAdd);
        System.out.println("The Step button is pressed");
    }

    @FXML
    private void handleLoadButton() {
        // Load MBR with memory at MAR
        String address = TFMAR.getText();
        int decimalAdd = Integer.parseInt(address, 2);
        String memoryContent = memory[decimalAdd];
        TFMBR.setText(memoryContent);
        System.out.println("The Load button is pressed");
    }

    @FXML
    private void handleStoreButton() {
        // Store MBR to memory at MAR
        String content = TFMBR.getText();
        String address = TFMAR.getText();
        int decimalAdd = Integer.parseInt(address, 2);
        memory[decimalAdd] = content;
        System.out.println("The Store button is pressed");
    }

    @FXML
    private void handleLoadPlusButton() {
        // not implemented yet
        System.out.println("The Load+ button is pressed");
    }

    @FXML
    private void handleStorePlusButton() {
        // not implemented yet
        System.out.println("The Store+ button is pressed");
    }

    @Override
    public void start(Stage stage) throws IOException {
        // set up the UI
        FXMLLoader fxmlLoader = new FXMLLoader(MachineController.class.getResource("machine.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("Machine Simulator");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}