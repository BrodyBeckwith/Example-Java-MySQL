package org.example;

import lombok.Getter;
import org.fusesource.jansi.Ansi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionManager
{
    // Just a hardcoded IP to a test VPS, would probably be in some sort of config in production
    private static final String HOST_NAME = "167.114.128.206";
    private static final String DATABASE_NAME = "test";

    @Getter
    private Connection connection;
    private final String username;
    private final String password;

    public ConnectionManager(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    public ScheduledFuture<?> connect()
    {
        long startTime = System.currentTimeMillis();
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(4);
        return scheduledThreadPoolExecutor.scheduleAtFixedRate(() ->
        {
            try
            {
                connection = DriverManager.getConnection("jdbc:mysql://" + HOST_NAME + "/" + DATABASE_NAME + "?useLegacyDatetimeCode=false&serverTimezone=America/New_York"+"&noAccessToProcedureBodies=true"+"&useSSL=false"+"&user=" + this.username + "&password=" + this.password);
                System.out.println(Ansi.ansi().fgBrightGreen().a("Connection successful ").fgBrightMagenta().a("(" + (System.currentTimeMillis() - startTime) + "ms)").reset());
                throw new RuntimeException();
            }
            catch (SQLException exception) { System.out.println(Ansi.ansi().fgBrightRed().a("Connection attempt failed. Retrying...").reset()); }
            }, 0, 5, TimeUnit.SECONDS);
    }

    public Callable<Boolean> isConnected()
    {
        return () -> ConnectionManager.this.connection != null && !this.connection.isClosed();
    }

    public void closeConnection()
    {
        if (this.connection == null) return;

        try { connection.close(); }
        catch (SQLException exception) { exception.printStackTrace(); }
    }
}
