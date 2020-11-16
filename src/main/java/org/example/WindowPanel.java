package org.example;

import net.efabrika.DBTablePrinter;
import org.fusesource.jansi.Ansi;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class WindowPanel extends JPanel
{
    Window window;
    JTextField usernameTextField;
    JPasswordField passwordTextField;

    private final AtomicBoolean successfulLogin = new AtomicBoolean(false);
    private final AtomicBoolean attemptingLogin = new AtomicBoolean(false);
    private final AtomicBoolean enteredInvalidUsernamePassword = new AtomicBoolean(false);
    Map<Integer, List<JButton>> menuButtonMap = new HashMap<>();

    public WindowPanel(Window window)
    {
        this.window = window;
        this.setLayout(new FlowLayout(SwingConstants.LEADING, 10, 10));

        Font font = new Font("Segoe UI", Font.PLAIN, 16);

        this.usernameTextField = new JTextField(8);
        this.usernameTextField.setToolTipText("Username");
        this.usernameTextField.setFont(font);
        this.usernameTextField.addActionListener(this.getLoginAction());
        TextPrompt usernamePrompt = new TextPrompt("Username", usernameTextField);
        this.add(usernameTextField);

        this.passwordTextField = new JPasswordField(8);
        this.passwordTextField.setToolTipText("Password");
        this.passwordTextField.setFont(font);
        this.passwordTextField.addActionListener(this.getLoginAction());
        TextPrompt passwordPrompt = new TextPrompt("Password", passwordTextField);
        this.add(passwordTextField);

        menuButtonMap.put(0, this.getOverviewButtons());
        menuButtonMap.put(1, this.getStaffButtons());
        menuButtonMap.put(2, this.getCustomerButtons());
        menuButtonMap.put(3, this.getsCarsButtons());
    }

    @Override
    public void paintComponent(Graphics g)
    {
        this.usernameTextField.setBounds(this.getWidth() / 2 - 200, (this.getHeight() / 2) - 36, 400, 32);
        this.passwordTextField.setBounds(this.getWidth() / 2 - 200, this.getHeight() / 2 + 4, 400, 32);

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.clearRect(0, 0, this.getWidth(), this.getHeight());

//        GradientPaint gradientPaint = new GradientPaint(0, 0, Color.decode("#e53170"), 0, this.getHeight(), Color.decode("#7f5af0"));
//        g2.setPaint(gradientPaint);
        g2.setColor(Color.decode("#b8c1ec"));
        g2.fill(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight()));

//        g2.setColor(Color.decode("#16161a"));
        g2.setColor(Color.decode("#232946"));
        g2.fill(new Rectangle2D.Double(5, 5, this.getWidth() - 10, this.getHeight() - 10));

        if (this.attemptingLogin.get()) this.drawStringToCenterOfScreen(g2, "Attempting login...", Color.white, 64, 16);
        else if (enteredInvalidUsernamePassword.get()) this.drawStringToCenterOfScreen(g2, "Invalid username and password combination.", Color.red, 64, 16);
//        else if (successfulLogin != null)  this.drawStringToCenterOfScreen(g2, "Login Successful", Color.green, 0, 16);
    }

    public void drawStringToCenterOfScreen(Graphics2D graphics2D, String text, Color color, int yOffset, int fontSize)
    {
        Font originalFont = graphics2D.getFont();

        graphics2D.setColor(color);
        Font newFont = new Font("Segoe UI", Font.PLAIN, fontSize);
        graphics2D.setFont(newFont);

        FontMetrics metrics = graphics2D.getFontMetrics(graphics2D.getFont());
        int x = (this.getWidth() - metrics.stringWidth(text)) / 2;
        int y = ((this.getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() + yOffset;
        graphics2D.drawString(text, x, y);

        graphics2D.setFont(originalFont);
    }

    public void drawCenteredString(Graphics graphics2D, String text, Rectangle rectangle, Color color)
    {
        Color originalColor = graphics2D.getColor();
        graphics2D.setColor(color);
        FontMetrics metrics = graphics2D.getFontMetrics(graphics2D.getFont());
        int x = (int) (rectangle.x + (rectangle.width - metrics.stringWidth(text)) / 2);
        int y = (int) (rectangle.y + ((rectangle.height - metrics.getHeight()) / 2) + metrics.getAscent());
        graphics2D.drawString(text, x, y);
        graphics2D.setColor(originalColor);
    }

    public ActionListener getLoginAction()
    {
        return event ->
        {
            if (attemptingLogin.get()) return;

            attemptingLogin.set(true);

            CompletableFuture<Boolean> loginFuture = ConnectionManager.login(usernameTextField.getText(), String.valueOf(passwordTextField.getPassword()));
            loginFuture.whenComplete((aBoolean, throwable) ->
            {
                attemptingLogin.set(false);

                if (aBoolean)
                {
                    WindowPanel.this.successfulLogin.set(true);
                    WindowPanel.this.enteredInvalidUsernamePassword.set(false);
                    WindowPanel.this.remove(usernameTextField);
                    WindowPanel.this.remove(passwordTextField);

                    JMenuBar jMenuBar = new JMenuBar();
                    String[] fileCommands = { "Overview", "Staff", "Customers", "Cars", "\n", "Exit" };
                    JMenu jMenu = new JMenu("Menus");
                    for (String command : fileCommands)
                    {
                        if (command.equals("\n"))
                        {
                            jMenu.addSeparator();
                            continue;
                        }
                        JMenuItem jMenuItem = new JMenuItem(command);

                        jMenuItem.addActionListener(event1 ->
                        {
                            switch (event1.getActionCommand())
                            {
                                case "Overview":
                                    WindowPanel.this.removeAll();
                                    for (JButton jbutton : WindowPanel.this.menuButtonMap.get(0))
                                    {
                                        WindowPanel.this.add(jbutton);
                                    }
                                    break;
                                case "Staff":
                                    WindowPanel.this.removeAll();
                                    for (JButton jbutton : WindowPanel.this.menuButtonMap.get(1))
                                    {
                                        WindowPanel.this.add(jbutton);
                                    }
                                    break;
                                case "Customers":
                                    WindowPanel.this.removeAll();
                                    for (JButton jbutton : WindowPanel.this.menuButtonMap.get(2))
                                    {
                                        WindowPanel.this.add(jbutton);
                                    }
                                    break;
                                case "Cars":
                                    WindowPanel.this.removeAll();
                                    for (JButton jbutton : WindowPanel.this.menuButtonMap.get(3))
                                    {
                                        WindowPanel.this.add(jbutton);
                                    }
                                    break;
                                case "Exit":
                                    System.exit(0);
                            }

                            WindowPanel.this.revalidate();
                        });
                        jMenu.add(jMenuItem);
                    }
                    jMenuBar.add(jMenu);
                    WindowPanel.this.window.setJMenuBar(jMenuBar);
                    WindowPanel.this.window.revalidate();

                    WindowPanel.this.removeAll();
                    for (JButton jbutton : WindowPanel.this.menuButtonMap.get(0))
                    {
                        WindowPanel.this.add(jbutton);
                    }
                    WindowPanel.this.revalidate();
                }
                else
                {
                    WindowPanel.this.enteredInvalidUsernamePassword.set(true);
                }
            });
        };
    }

    public List<JButton> getOverviewButtons()
    {
        List<JButton> buttons = new ArrayList<>();

        JButton staffButton = new JButton("Staff");
        staffButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Staff;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(staffButton);

        JButton vehicleDetailsButton = new JButton("Vehicle Details");
        vehicleDetailsButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Vehicle_Details;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(vehicleDetailsButton);

        JButton staffPerformanceButton = new JButton("Staff Performance");
        staffPerformanceButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Staff_Performance;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(staffPerformanceButton);

        JButton customerInformationButton = new JButton("Customer Information");
        customerInformationButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Customer_Info;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(customerInformationButton);

        JButton customerServiceButton = new JButton("Customer Service");
        customerServiceButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Customer_Service;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(customerServiceButton);

        JButton purchaseRecordsButton = new JButton("Purchase Records");
        purchaseRecordsButton.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Purchase_Record;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(purchaseRecordsButton);

        return buttons;
    }

    public List<JButton> getStaffButtons()
    {
        List<JButton> buttons = new ArrayList<>();

        JButton allStaff = new JButton("All Staff");
        allStaff.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Staff;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(allStaff);

        JButton overhead = new JButton("Overhead");
        overhead.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT Level, ROUND(AVG(Salary), 0) as Average_Salary, SUM(Price) AS Total_Revenue, SUM(Commission) AS Total_Commission \n" +
                        "FROM Staff, Vehicle_Details, Staff_Performance\n" +
                        "WHERE Staff_Performance.`VIN_#` = Vehicle_Details.`VIN_#` AND Staff_Performance.Employee_ID = Staff.Employee_ID GROUP BY Level;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(overhead);

        JButton averagePay = new JButton("Avg. Pay");
        averagePay.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT Employee_Name, Level, Salary FROM Staff WHERE Salary > (SELECT AVG(Salary) FROM Staff);");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(averagePay);

        JButton accounting = new JButton("Accounting");
        accounting.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Accounting_Dept;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(accounting);

        JButton promote = new JButton("Promote");
        promote.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            // Transaction...
            try
            {
                System.out.println(Ansi.ansi().eraseScreen());
                connection.setAutoCommit(false);
                connection.createStatement().executeUpdate("UPDATE Staff SET Level = 'Junior', Salary = 55000 WHERE Employee_ID = 0020;");

                // Lets print to check our changes
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Staff;");
                DBTablePrinter.printResultSet(resultSet);

                // Undo changes and print the table to ensure.
                System.out.println(Ansi.ansi().fgYellow().a("Reverting database to unchanged state.").reset());
                connection.rollback();
                resultSet = connection.createStatement().executeQuery("SELECT * FROM Staff;");
                DBTablePrinter.printResultSet(resultSet);
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(promote);

        JButton terminate = new JButton("Terminate");
        terminate.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                System.out.println(Ansi.ansi().eraseScreen());
                System.out.println(Ansi.ansi().fgBrightRed().a("Deleting 1 employee from Staff table...").reset());
                connection.createStatement().executeUpdate("DELETE FROM Staff WHERE Employee_ID = 0000;");
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(terminate);

        return buttons;
    }

    public List<JButton> getCustomerButtons()
    {
        List<JButton> buttons = new ArrayList<>();

        JButton allStaff = new JButton("All Customers");
        allStaff.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT Name, Age, City, Brand_Name, Model_Name\n" +
                        " FROM Customer_Info, Purchase_Record, Vehicle_Details\n" +
                        " WHERE Customer_Info.Customer_ID = Purchase_Record.Customer_ID\n" +
                        " AND Purchase_Record.`VIN_#` = Vehicle_Details.`VIN_#`;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(allStaff);

        JButton carsPurchased = new JButton("Cars Purchased");
        carsPurchased.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT Employee_ID, Customer_ID, `VIN_#`, Sale_Date FROM Staff_Performance NATURAL JOIN Customer_Service NATURAL JOIN Purchase_Record;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(carsPurchased);

        JButton demographics = new JButton("Demographics");
        demographics.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT City, COUNT(City) AS Customers_Per_City, ROUND(AVG(age),0) AS Average_Age \n" +
                        "FROM Customer_Info GROUP BY City;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(demographics);

        JButton tradeIn = new JButton("Trade In");
        tradeIn.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT Name, Brand_Name, Model_Name, Sale_Date, trade(Price) AS Trade_In FROM Customer_Info NATURAL JOIN Purchase_Record NATURAL JOIN Vehicle_Details\n" +
                        "WHERE Sale_Date LIKE '%2019%' OR Sale_Date LIKE '%2018%' AND Purchase_Record.`VIN_#` = Vehicle_Details.`VIN_#`;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(tradeIn);

        return buttons;
    }

    public List<JButton> getsCarsButtons()
    {
        List<JButton> buttons = new ArrayList<>();

        JButton allCars = new JButton("All Cars");
        allCars.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Vehicle_Details;");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(allCars);

        JButton remaining = new JButton("Remaining");
        remaining.addActionListener(event -> ConnectionManager.getInstance().getConnection().whenCompleteAsync((connection, throwable1) ->
        {
            try
            {
                ResultSet resultSet = connection.createStatement().executeQuery("CALL Remaining_Inventory();");
                System.out.println(Ansi.ansi().eraseScreen());
                DBTablePrinter.printResultSet(resultSet);
                connection.close();
            }
            catch (SQLException ignored) {}
        }));
        buttons.add(remaining);

        return buttons;
    }
}
