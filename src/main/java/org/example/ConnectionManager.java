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

    public ConnectionManager(String username, String password)
    {
        this.comboPooledDataSource = new ComboPooledDataSource();

        try { this.comboPooledDataSource.setDriverClass("com.mysql.cj.jdbc.Driver"); }
        catch (PropertyVetoException exception)
        {
            System.out.println(Ansi.ansi().fgBrightRed().a("Failed to find MySQL Driver.").reset());
            exception.printStackTrace();
            System.exit(1);
        }

        this.comboPooledDataSource.setJdbcUrl("jdbc:mysql://" + HOST_NAME + ":3306/" + DATABASE_NAME);
        this.comboPooledDataSource.setUser(username);
        this.comboPooledDataSource.setPassword(password);
        this.comboPooledDataSource.setMinPoolSize(4);
        this.comboPooledDataSource.setInitialPoolSize(4);
        this.comboPooledDataSource.setMaxPoolSize(8);

        System.out.println(Ansi.ansi().fgBrightGreen().a("Attempting MySQL connection...").reset());
        long startTime = System.currentTimeMillis();
        this.getConnection().whenComplete((connection, throwable) ->
        {
            System.out.println(Ansi.ansi().fgBrightGreen().a("Connection successful ").fgBrightMagenta().a("(" + (System.currentTimeMillis() - startTime) + "ms)").reset());
            try { connection.close(); }
            catch (SQLException exception) { exception.printStackTrace(); }
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
                connectionFuture.complete(connection);
            }
            catch (SQLException exception)
            {
                exception.printStackTrace();
                System.out.println(Ansi.ansi().fgBrightRed().a("MySQL connection attempt failed.").reset());
                System.exit(1);
            }
        });

        return connectionFuture;
    }
}
