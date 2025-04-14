package app;

import app.screen.EngineScreen;
import app.screen.Launcher;
import app.screen.MultiplayerScreen;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.File;

public class Application {

    private final JFrame frame;
    private final ScreenManager screenManager;
    private final EngineScreen engine;
    private final Launcher launcher;

    public Application() {
        engine = new EngineScreen();
        launcher = new Launcher(e -> switchToEngine(), e -> hostEngine(), e -> joinEngine());

        frame = new JFrame("Java Engine 3D");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        frame.setLayout(new BorderLayout());
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem launcherItem = new JMenuItem("Launcher");
        launcherItem.addActionListener(e -> switchToLauncher());

        JMenuItem loadItem = new JMenuItem("Load File");
        loadItem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                switchToEngine();
                engine.getEngine().importScene(file.getAbsolutePath());
            }
        });

        fileMenu.add(launcherItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        // Initialize the screen manager.
        screenManager = new ScreenManager(frame);

        // Create the Launcher. The start button's ActionListener
        // calls switchToEngine() which uses screenManager.
        screenManager.switchTo(launcher);
    }

    private void switchToEngine() {
        // Create and initialize your Engine
        screenManager.switchTo(engine);
    }

    private void hostEngine() {
        screenManager.switchTo(new MultiplayerScreen());
    }

    private void joinEngine() {
        String endpoint = JOptionPane.showInputDialog(frame, "Enter server IP (e.g. 127.0.0.1:1111)", "Join Server", JOptionPane.QUESTION_MESSAGE);
        if (endpoint != null && !endpoint.isBlank()) {
            MultiplayerScreen joinScreen = new MultiplayerScreen(endpoint);
            screenManager.switchTo(joinScreen);
        }
    }

    private void switchToLauncher() {
        screenManager.switchTo(launcher);
    }
}

