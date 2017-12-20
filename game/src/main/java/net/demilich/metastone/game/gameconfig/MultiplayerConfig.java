package net.demilich.metastone.game.gameconfig;

public class MultiplayerConfig extends GameConfig {
    private String ipAddress;
    private int port;

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}
