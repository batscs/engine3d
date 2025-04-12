package app;

import app.screen.EngineScreen;
import app.screen.Launcher;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import java.io.File;

public class Application {

    private final JFrame frame;
    private final ScreenManager screenManager;
    private final EngineScreen engine;
    private final Launcher launcher;

    public Application() {
        engine = new EngineScreen(e -> switchToLauncher());
        launcher = new Launcher(e -> switchToEngine());

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

    private void switchToLauncher() {
        screenManager.switchTo(launcher);
    }
}

