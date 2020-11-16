package org.example;

import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Dimension;

public class Window extends JFrame
{
    public WindowPanel windowPanel;

    public Window()
    {
        super("Car Dealership Menu");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setResizable(true);

        this.windowPanel = new WindowPanel(this);
        this.add(this.windowPanel);

        this.setMinimumSize(new Dimension(720, 480));
        this.setVisible(true);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        this.windowPanel.requestFocusInWindow();


        this.pack();
    }
}