package org.example;


import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.URL;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class DSMS_Client {
    private String userID;
    private DSMSService server;
    private String relatedServer;
    private static ConcurrentHashMap<String, String> hostsMap = null;
    private static ConcurrentHashMap<String, Integer> portsMap = null;
    private final Logger logger;

    public DSMS_Client(String userID) {
        this.userID = userID;
        this.relatedServer = userID.substring(0, 3);
        initializeServer();

        logger = new Logger(userID);
    }

    public void initializeServer() {
        try {

            // URL of the WSDL document for the web service
            URL url = new URL("http://localhost:8080/"+this.relatedServer+"?wsdl");

            // QName representing the service name and namespace
            QName qname = new QName("http://example.org/", "DSMSServiceImplService");

            // Create a Service object for the specified WSDL document and QName
            Service service = Service.create(url, qname);

            // Get a proxy to the DSMS Service interface
            this.server = service.getPort(DSMSService.class);

        } catch (Exception e) {
            System.err.println("Client exception: " + e.getMessage());
        }
    }

    public boolean isAdmin() {
        return userID.charAt(3) == 'A';
    }

    public void buyShares(String shareID, String shareType, int quantity) {
        try {
            String result = server.purchaseShare(userID, shareID, shareType, quantity);
            System.out.println("Result of buyShares: " + result);
            logger.log("buyShares\tshareID: " + shareID + " shareType: " + shareType + " quantity: " + quantity
                    + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error buying shares: " + e.getMessage());
            logger.log("buyShares\tError: " + e.getMessage());
        }

    }

    public void sellShares(String shareID, int quantity) {
        try {
            String result = server.sellShare(userID, shareID, quantity);
            System.out.println("Result of sellShares: " + result);

            logger.log("sellShares\tshareID: " + shareID + " quantity: " + quantity + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error selling shares: " + e.getMessage());
            logger.log("sellShares\tError: " + e.getMessage());
        }
    }

    public void getShares() {
        try {
            System.out.println("Fetching shares for " + userID);
            String result = server.getShares(userID);
            System.out.println("Result of getShares: " + result);
            logger.log("getShares\tResult: " + result);
        } catch (Exception e) {
            System.out.println("Error getting shares: " + e.getMessage());
            logger.log("getShares\tError: " + e.getMessage());
        }
    }

    // Admin methods

    public void addShares(String shareID, String shareType, int capacity) {
        try {
            if (!isAdmin()) {
                System.out.println("You are not authorized to add shares!");
                return;
            }

            String result = server.addShare(shareID, shareType, capacity);
            System.out.println("Result of addShares: " + result);
            logger.log("addShares\tshareID: " + shareID + " shareType: " + shareType + " capacity: " + capacity
                    + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error adding shares: " + e.getMessage());
            logger.log("addShares\tError: " + e.getMessage());
        }
    }

    public void removeShares(String shareID, String shareType) {
        try {
            if (!isAdmin()) {
                System.out.println("You are not authorized to remove shares!");
                return;
            }

            String result = server.removeShare(shareID, shareType);
            System.out.println("Result of removeShares: " + result);

            logger.log("removeShares\tshareID: " + shareID + " shareType: " + shareType + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error removing shares: " + e.getMessage());
            logger.log("removeShares\tError: " + e.getMessage());
        }
    }

    public void listShareAvailability(String shareType) {
        try {
            if (!isAdmin()) {
                System.out.println("You are not authorized to list share availability!");
                return;
            }

            String result = server.listShareAvailability(shareType);
            System.out.println("Result of listShareAvailability: " + result);
            logger.log("listShareAvailability\tshareType: " + shareType + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error listing share availability: " + e.getMessage());
            logger.log("listShareAvailability\tError: " + e.getMessage());
        }
    }

    public void swapShares(String oldShareID, String oldShareType, String newShareID, String newShareType) {
        try {
            String result = server.swapShares(userID, oldShareID, oldShareType, newShareID, newShareType);
            System.out.println("Result of swapShares: " + result);
            logger.log("swapShares\toldShareID: " + oldShareID + " oldShareType: " + oldShareType + " newShareID: "
                    + newShareID + " newShareType: " + newShareType + " Result: " + result);
        } catch (Exception e) {
            System.out.println("Error swapping shares: " + e.getMessage());
            logger.log("swapShares\tError: " + e.getMessage());
        }
    }

    public void showMainMenu(Scanner inpScan) {
        while (true) {
            System.out.println("1. Buy shares");
            System.out.println("2. Sell shares");
            System.out.println("3. Get shares");
            System.out.println("4. Swap shares");
            if (isAdmin()) {
                System.out.println("5. Add shares");
                System.out.println("6. Remove shares");
                System.out.println("7. List share availability");
            }
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = inpScan.nextInt();
            inpScan.nextLine();

            switch (choice) {
                case 1: {
                    System.out.print("Enter the shareID you want to buy: ");
                    String shareID = inpScan.nextLine();

                    System.out.print("Enter the shareType you want to buy: ");
                    String shareType = inpScan.nextLine();

                    System.out.print("Enter the quantity of shares you want to buy: ");
                    int quantity = inpScan.nextInt();
                    buyShares(shareID, shareType, quantity);
                    break;
                }
                case 2: {
                    System.out.print("Enter the shareID you want to sell: ");
                    String shareID = inpScan.nextLine();

                    System.out.print("Enter the quantity of shares you want to sell: ");
                    int quantity = inpScan.nextInt();

                    sellShares(shareID, quantity);
                    break;
                }
                case 3:
                    getShares();
                    break;
                case 4: {
                    System.out.print("Enter the old shareID you want to swap: ");
                    String oldShareID = inpScan.nextLine();
                    System.out.println("Enter the old shareType you want to swap: ");
                    String oldShareType = inpScan.nextLine();
                    System.out.println("Enter the new shareID you want to swap: ");
                    String newShareID = inpScan.nextLine();
                    System.out.println("Enter the new shareType you want to swap: ");
                    String newShareType = inpScan.nextLine();
                    swapShares(oldShareID, oldShareType, newShareID, newShareType);
                    break;
                }
                case 5: {
                    System.out.print("Enter the shareID you want to add: ");
                    String shareID = inpScan.nextLine();

                    System.out.print("Enter the shareType you want to add: ");
                    String shareType = inpScan.nextLine();

                    System.out.print("Enter the capacity of shares you want to add: ");
                    int capacity = inpScan.nextInt();

                    addShares(shareID, shareType, capacity);
                    break;
                }
                case 6: {
                    System.out.print("Enter the shareID you want to remove: ");
                    String shareID = inpScan.nextLine();

                    System.out.print("Enter the shareType you want to remove: ");
                    String shareType = inpScan.nextLine();

                    removeShares(shareID, shareType);
                    break;
                }
                case 7: {
                    System.out.print("Enter the shareType you want to list: ");
                    String shareType = inpScan.nextLine();
                    listShareAvailability(shareType);
                    break;
                }
                case 0: {
                    return;
                }
                default:
                    System.out.println("Invalid choice! Please try again.");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Welcome to DSMSService Client!");
        System.out.println("Please enter your userID to login.");
        Scanner inpScan = new Scanner(System.in);
        String userID = inpScan.nextLine();
        DSMS_Client client = new DSMS_Client(userID);
        client.showMainMenu(inpScan);

    }
}