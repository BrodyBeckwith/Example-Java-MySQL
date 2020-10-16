package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class Window extends JFrame
{
//    private static final Dimension GAME_SIZE = new Dimension(480, 700);

    public WindowPanel windowPanel;

    public Window()
    {
        super("Test");

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
//        textField.requestFocusInWindow();


        this.pack();
    }
}