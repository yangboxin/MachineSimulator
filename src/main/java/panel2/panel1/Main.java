package panel2.panel1;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        // Ensure the Memory class is loaded before launching the JavaFX application
        Memory memory = new Memory();

        // Launch the JavaFX application by instantiating MachineController
        Application.launch(MachineController.class, args);
    }
}
