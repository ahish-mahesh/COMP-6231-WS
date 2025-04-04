package org.example;

/**
 * The `DSMSServerInitializer` class is responsible for initializing and starting multiple DSMSService servers.
 */
public class DSMSServerInitializer {

    /**
     * The main method creates and starts three DSMSService servers, each running in its own thread.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            // Create and bind 3 different servers
            Thread server1Thread = new Thread(new DSMSServer("NYK", "localhost", 9876, 9976));
            Thread server2Thread = new Thread(new DSMSServer("LON", "localhost", 9877, 9977));
            Thread server3Thread = new Thread(new DSMSServer("TOK", "localhost", 9878, 9978));

            // Start each server in its own thread
            server1Thread.start();
            server2Thread.start();
            server3Thread.start();

            // Optionally, wait for all threads to complete
            server1Thread.join();
            server2Thread.join();
            server3Thread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
