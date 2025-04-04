package org.example;


/**
 * The DSMSServer class implements a server that listens for UDP requests
 * and publishes a web service endpoint.
 */
public class DSMSServer implements Runnable {
    private String serverName;
    private String serverAddress;
    private int senderUdpPort;
    private int receiverUdpPort;

    /**
     * Constructs a DSMSServer with the specified server name and UDP port.
     *
     * @param serverName the name of the server
     * @param senderUdpPort the sender UDP port number
     * @param receiverUdpPort the receiver UDP port number
     */
    public DSMSServer(String serverName, String serverAddress,  int senderUdpPort, int receiverUdpPort) {
        this.serverName = serverName;
        this.senderUdpPort = senderUdpPort;
        this.receiverUdpPort = receiverUdpPort;
        this.serverAddress = serverAddress;
    }

    /**
     * Runs the server, creating a remote server object and publishing it as a web service endpoint.
     * Also starts a new thread to listen for UDP requests.
     */
    @Override
    public void run() {
        try {
            // Create the remote server object
            DSMSServiceImpl server = new DSMSServiceImpl(this.serverName, this.serverAddress, this.senderUdpPort, this.receiverUdpPort);
            // Publish the web service endpoint
            javax.xml.ws.Endpoint.publish("http://localhost:8080/" + this.serverName, server);
            // Start the server's UDP listening thread
            new Thread(server).start();
            System.out.println(this.serverName + " started and listening on http://localhost:8080/" + this.serverName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}