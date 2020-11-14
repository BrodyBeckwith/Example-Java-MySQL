package org.example;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.Getter;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ConnectionManager
{
    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(4);
    private static final String HOST_NAME = "satoshi.cis.uncw.edu";
    private static final String DATABASE_NAME = "narayanFall2020group2";

    @Getter
    private final ComboPooledDataSource comboPooledDataSource;

    private static ConnectionManager instance;

    public static ConnectionManager getInstance()
    {
        return instance;
    }

    private static CompletableFuture<Boolean> loginFuture;

    public static CompletableFuture<Boolean> login(String username, String password)
    {
        if (loginFuture != null && !loginFuture.isDone()) return CompletableFuture.completedFuture(false);

        if (instance != null) return CompletableFuture.completedFuture(true);

        loginFuture = new CompletableFuture<>();

        CompletableFuture.runAsync(() ->
        {
            ConnectionManager connectionManager = new ConnectionManager(username, password, loginFuture);
            loginFuture.whenComplete((aBoolean, throwable) ->
            {
                if (aBoolean) instance = connectionManager;
                loginFuture = null;
            });
        }, SCHEDULED_THREAD_POOL_EXECUTOR);

        return loginFuture;
    }

    private ConnectionManager(String username, String password, CompletableFuture<Boolean> future)
    {
        this.comboPooledDataSource = new ComboPooledDataSource();

        try { this.comboPooledDataSource.setDriverClass("com.mysql.cj.jdbc.Driver"); }
        catch (PropertyVetoException exception)
        {
            System.out.println(Ansi.ansi().fgBrightRed().a("Failed to find MySQL Driver.").reset());
            exception.printStackTrace();
            future.complete(false);
            AnsiConsole.systemUninstall();
            System.exit(1);
        }

        this.comboPooledDataSource.setJdbcUrl("jdbc:mysql://" + HOST_NAME + "/" + DATABASE_NAME + "?useLegacyDatetimeCode=false&serverTimezone=America/New_York&noAccessToProcedureBodies=true&useSSL=false");
        this.comboPooledDataSource.setUser(username);
        this.comboPooledDataSource.setPassword(password);
        this.comboPooledDataSource.setMinPoolSize(4);
        this.comboPooledDataSource.setInitialPoolSize(4);
        this.comboPooledDataSource.setMaxPoolSize(8);
        this.comboPooledDataSource.setAcquireRetryAttempts(0);

        System.out.println("Attempting MySQL connection...");
        long startTime = System.currentTimeMillis();
        this.getConnection().whenComplete((connection, throwable) ->
        {
            if (connection == null)
            {
                future.complete(false);
                return;
            }

            try
            {
                System.out.println(Ansi.ansi().fgBrightGreen().a("MySQL Connection successful ").fgBrightMagenta().a("(" + (System.currentTimeMillis() - startTime) + "ms)").reset());

                // Run a test query as an example to show we can interact with the database
//                System.out.println("Show the name level and salary of all staff who earn more than the average of all Staff");
//                ResultSet resultSet = connection.createStatement().executeQuery("select Employee_Name, Level, Salary from Staff where Salary > (select avg(Salary) from Staff);");
//                while (resultSet.next())
//                {
//                    System.out.println(resultSet.getString(1) + " " + resultSet.getString(2) + " " + resultSet.getInt(3));
//                }

                connection.close();
                future.complete(true);
                return;
            }
            catch (SQLException exception) { exception.printStackTrace(); }

            future.complete(false);
        });
    }

    public CompletableFuture<Connection> getConnection()
    {
        CompletableFuture<Connection> connectionFuture = new CompletableFuture<>();

        SCHEDULED_THREAD_POOL_EXECUTOR.submit(() ->
        {
            try
            {
                Connection connection = comboPooledDataSource.getConnection();
                connectionFuture.complete(connection);
                return;
            }
            catch (SQLException exception)
            {
                System.out.println(Ansi.ansi().fgBrightRed().a("MySQL connection attempt failed.").reset());
                exception.printStackTrace();
            }
            connectionFuture.complete(null);
        });

        return connectionFuture;
    }
}
