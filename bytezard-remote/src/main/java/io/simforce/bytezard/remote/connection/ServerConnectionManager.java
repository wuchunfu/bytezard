package io.simforce.bytezard.remote.connection;

/**
 * @author zixi0825
 */
public class ServerConnectionManager {

    private Connection masterConnection;

    private ServerConnectionManager() {}

    private static class Singleton{
        static ServerConnectionManager instance = new ServerConnectionManager();
    }

    public static ServerConnectionManager getInstance() {
        return Singleton.instance;
    }

    public Connection getConnection() {
        return masterConnection;
    }

    public void setConnection(Connection masterConnection) {
        this.masterConnection = masterConnection;
    }
}
