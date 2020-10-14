package org.example;

import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.Console;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Main
{
    private static ConnectionManager connectionManager;

    public static void main(String[] args)
    {
        AnsiConsole.systemInstall();

        Console console = System.console();
        String username = args.length > 0 ? args[0] : console.readLine("Username: ");
        String password = args.length > 1 ? args[1] : String.valueOf(console.readPassword("Password: "));

        connectionManager = new ConnectionManager(username, password);
        System.out.println("Attempting database connection...");
        ScheduledFuture<?> connectionFuture = connectionManager.connect();

        try { Awaitility.await().atMost(30, TimeUnit.SECONDS).until(connectionManager.isConnected()); }
        catch (ConditionTimeoutException exception)
        {
            System.out.println(Ansi.ansi().fgBrightRed().a("Connection could not be established after 30 seconds, exiting...").reset());
            connectionFuture.cancel(true);
            AnsiConsole.systemUninstall();
            System.exit(1);
        }

        // Test entering data and printing it out
        Connection connection = connectionManager.getConnection();
        try
        {
            connection.createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS testTable(id int);");

            connection.createStatement().executeUpdate("INSERT INTO testTable VALUES(1)");
            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM testTable");
            if (resultSet.next())
            {
                // should print out 1 if its the only data in the table
                System.out.println(resultSet.getInt(1));
            }

            connection.createStatement().executeUpdate("DROP TABLE testTable;");
        }
        catch (SQLException exception)
        {
            exception.printStackTrace();
        }

        System.out.println("Closing connection...");
        connectionManager.closeConnection();
        AnsiConsole.systemUninstall();
        System.exit(0);
    }
}
