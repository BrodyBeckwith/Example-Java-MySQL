package org.example;

import org.fusesource.jansi.AnsiConsole;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main
{
    public static void main(String[] args)
    {
        AnsiConsole.systemInstall();

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Window window = new Window();
        executor.execute(() ->
        {
            while (true)
            {
                try { window.repaint(); }
                catch (Exception exception) { exception.printStackTrace(); }
            }
        });
    }
}
