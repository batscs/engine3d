package app.screen;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;

public class Launcher implements Screen {

    private final ActionListener startAction;
    private final ActionListener hostAction;
    private final ActionListener joinAction;

    public Launcher(ActionListener startAction, ActionListener hostAction, ActionListener joinAction) {
        this.startAction = startAction;
        this.hostAction = hostAction;
        this.joinAction = joinAction;
    }

    @Override
    public void start(JFrame frame) {
        frame.setTitle("Main Menu");
        frame.setLayout(new BorderLayout());

        // Create outer panel with BoxLayout to center content
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 100, 200));


        // Create and add buttons
        centerPanel.add(createStyledButton("Start", startAction));
        centerPanel.add(Box.createVerticalStrut(20)); // spacing
        centerPanel.add(createStyledButton("Host", hostAction));
        centerPanel.add(createStyledButton("Join", joinAction));

        // Add the centered panel to the frame
        frame.add(centerPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    JButton createStyledButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.setAlignmentX(JButton.CENTER_ALIGNMENT);
        button.setMaximumSize(new java.awt.Dimension(200, 40));
        button.setFocusPainted(false);
        button.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));
        button.addActionListener(listener);
        return button;
    }


    @Override
    public void stop(JFrame frame) {
        // Clean up by removing all components from the frame
        frame.getContentPane().removeAll();
        // Refresh the frame to ensure that changes take effect immediately.
        frame.revalidate();
        frame.repaint();
    }
}
