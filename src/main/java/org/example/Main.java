package org.example;

import org.fusesource.jansi.AnsiConsole;

import java.io.Console;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        connectionManager.getConnection().whenComplete((connection, throwable) ->
        {
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

                System.out.println("Closing connection...");
                connection.close();
                AnsiConsole.systemUninstall();
                System.exit(0);
            }
            catch (SQLException exception)
            {
                exception.printStackTrace();
            }
        });
    }
}
