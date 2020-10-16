package org.example;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import lombok.Getter;
import org.fusesource.jansi.Ansi;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ConnectionManager
{
    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(4);
    private static final String HOST_NAME = "167.114.128.206";
    private static final String DATABASE_NAME = "test";

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

        ConnectionManager connectionManager = new ConnectionManager(username, password, loginFuture);

        loginFuture.whenComplete((aBoolean, throwable) ->
        {
            if (aBoolean) instance = connectionManager;
            loginFuture = null;
        });

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
            System.exit(1);
        }

        this.comboPooledDataSource.setJdbcUrl("jdbc:mysql://" + HOST_NAME + ":3306/" + DATABASE_NAME);
        this.comboPooledDataSource.setUser(username);
        this.comboPooledDataSource.setPassword(password);
        this.comboPooledDataSource.setMinPoolSize(4);
        this.comboPooledDataSource.setInitialPoolSize(4);
        this.comboPooledDataSource.setMaxPoolSize(8);
        this.comboPooledDataSource.setAcquireRetryAttempts(3);

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
                connection.close();
                System.out.println(Ansi.ansi().fgBrightGreen().a("MySQL Connection successful ").fgBrightMagenta().a("(" + (System.currentTimeMillis() - startTime) + "ms)").reset());
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

        SCHEDULED_THREAD_POOL_EXECUTOR.execute(() ->
        {
            try
            {
                Connection connection = comboPooledDataSource.getConnection();
                System.out.println("ok");
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
