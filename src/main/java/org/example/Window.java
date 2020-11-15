package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Window extends JFrame
{
    public WindowPanel windowPanel;

    public Window()
    {
        super("MySQL Example");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { }

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setResizable(true);

//        JMenuBar jMenuBar = new JMenuBar();
//        String[] fileCommands = { "Overview", "Staff", "Customers", "Cars", "\n", "Exit" };
//        JMenu jMenu = new JMenu("Menus");
//        for (String command : fileCommands)
//        {
//            if (command.equals("\n"))
//            {
//                jMenu.addSeparator();
//                continue;
//            }
//            JMenuItem jMenuItem = new JMenuItem(command);
//
//            jMenuItem.addActionListener(new ActionListener()
//            {
//                @Override
//                public void actionPerformed(ActionEvent event)
//                {
//                    switch (event.getActionCommand())
//                    {
//                        case "Overview":
//                            break;
//                        case "Staff":
//                            break;
//                        case "Customers":
//                            break;
//                        case "Cars":
//                            break;
//                        case "Exit":
//                            System.exit(0);
//                    }
//                }
//            });
//            jMenu.add(jMenuItem);
//        }
//        jMenuBar.add(jMenu);
//        this.setJMenuBar(jMenuBar);

        this.windowPanel = new WindowPanel(this);
        this.add(this.windowPanel);

        this.setMinimumSize(new Dimension(720, 480));
        this.setVisible(true);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);

        this.windowPanel.requestFocusInWindow();


        this.pack();
    }
}