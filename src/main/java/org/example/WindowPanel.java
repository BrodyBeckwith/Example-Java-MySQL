package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.concurrent.CompletableFuture;

public class WindowPanel extends JPanel
{
    Window window;
    JTextField usernameTextField;
    JPasswordField passwordTextField;

    Boolean successfulLogin = null;
    boolean attemptingLogin = false;
    boolean enteredInvalidUsernamePassword = false;

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

        if (attemptingLogin) this.drawStringToCenterOfScreen(g2, "Attempting login...", Color.white, 64, 16);
        else if (enteredInvalidUsernamePassword) this.drawStringToCenterOfScreen(g2, "Invalid username and password combination.", Color.red, 64, 16);
        else if (successfulLogin != null)  this.drawStringToCenterOfScreen(g2, "Login Successful", Color.green, 0, 16);
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
            if (attemptingLogin) return;

            CompletableFuture<Boolean> loginFuture = ConnectionManager.login(usernameTextField.getText(), String.valueOf(passwordTextField.getPassword()));
            loginFuture.whenComplete((aBoolean, throwable) ->
            {
                attemptingLogin = false;

                if (aBoolean)
                {
                    WindowPanel.this.successfulLogin = true;
                    WindowPanel.this.enteredInvalidUsernamePassword = false;
                    WindowPanel.this.remove(usernameTextField);
                    WindowPanel.this.remove(passwordTextField);
                }
                else
                {
                    WindowPanel.this.enteredInvalidUsernamePassword = true;
                }
            });

            attemptingLogin = true;
        };
    }
}
