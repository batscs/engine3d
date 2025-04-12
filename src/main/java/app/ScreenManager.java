package app;

import app.screen.Screen;

import javax.swing.JFrame;

public class ScreenManager {
    private final JFrame frame;
    private Screen currentScreen;

    public ScreenManager(JFrame frame) {
        this.frame = frame;
    }

    /**
     * Switches from the current screen to a new screen.
     * This calls stop() on the current screen, clears the frame,
     * then calls start() on the new screen.
     */
    public void switchTo(Screen newScreen) {
        if (currentScreen != null) {
            currentScreen.stop(frame);
        }
        // Remove all components from the frame.
        frame.getContentPane().removeAll();

        // Set the new screen and start it.
        currentScreen = newScreen;
        currentScreen.start(frame);

        // Refresh the frame.
        frame.revalidate();
        frame.repaint();
    }
}