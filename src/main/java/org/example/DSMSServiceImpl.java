package org.example;

import javax.jws.WebService;
import java.io.IOException;
import static java.lang.String.format;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@WebService(endpointInterface = "org.example.DSMSService")
public class DSMSServiceImpl implements DSMSService, Runnable{

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_shares = null;
    // A 3-layer nested hashmap to keep track of buys of each user
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>> chm_userShares = null;

    private final String serverName;
    private DatagramSocket udpReceiverSocket;
    private DatagramSocket udpSenderSocket;
    private int udpReceiverPort;
    private static final List<String> lst_serverAdr = new CopyOnWriteArrayList<>();
    private static final List<Integer> lst_serverPorts = new CopyOnWriteArrayList<>();
    private static final List<String> lst_serverNames = new CopyOnWriteArrayList<>();
    private final Logger logger;

    /**
     * Creating a static shares concurrent hash map per city once, if not
     * already created
     * <br>
     * Then initializing the other server variables
     */
    public DSMSServiceImpl(String serverName, String serverAddr, int udpReceiverPort, int udpSenderPort)
            throws RemoteException {
        chm_shares = new ConcurrentHashMap<>();
        chm_shares.put("Equity", new ConcurrentHashMap<>());
        chm_shares.put("Bonus", new ConcurrentHashMap<>());
        chm_shares.put("Dividend", new ConcurrentHashMap<>());

        chm_userShares = new ConcurrentHashMap<>();

        this.serverName = serverName;
        try {
            this.udpReceiverPort = udpReceiverPort;
            this.udpReceiverSocket = new DatagramSocket(udpReceiverPort);
            this.udpSenderSocket = new DatagramSocket(udpSenderPort);
        } catch (SocketException e) {
            System.err.println("Error occurred while creating the UDP socket: " + e.getMessage());
        }

        lst_serverAdr.add(serverAddr);
        lst_serverPorts.add(udpReceiverPort);
        lst_serverNames.add(serverName);

        // Create Logger instance
        this.logger = new Logger(serverName);

    }

    private boolean isShareValid(String shareID) {
        String test = "NYKE101025";
        if (shareID.substring(0, 3).equals(serverName)) // If the share is of the same server
            if ((shareID.charAt(3) == 'M') || (shareID.charAt(3) == 'A') || (shareID.charAt(3) == 'E')) // Share timing
                // is only 3
                // possible
                // values
                if (shareID.length() == 10)
                    if (Integer.parseInt(shareID.substring(4, 6)) <= 31) // Share should have a valid date
                        return Integer.parseInt(shareID.substring(6, 8)) <= 12;
        return false;
    }

    /**
     * @param shareID   ShareId of the share to be inserted
     * @param shareType Type of share to be inserted
     * @param capacity  How many shares need to be inserted
     * @return Returns the status of the operation
     */
    @Override
    public String addShare(String shareID, String shareType, int capacity) {
        String resultMsg;
        String responseStatus = "Success";

        try {

            if (!isShareValid(shareID)) {
                resultMsg = "Invalid shareID provided creating share. Please enter a valid ID (e.g. NYKM101025, TOKE101225).";
                logger.log("addShare\t" + "shareID: " + shareID + ", shareType: " + shareType + ", capacity: "
                        + capacity + "Status: " + responseStatus + ", serverResponse: " + resultMsg);
                return resultMsg;
            }

            if (!shareID.substring(0, 3).equals(serverName)) {
                resultMsg = "Invalid server for creating share. Please contact respective server.";
                logger.log("addShare\t" + "shareID: " + shareID + ", shareType: " + shareType + ", capacity: "
                        + capacity + "Status: " + responseStatus + ", serverResponse: " + resultMsg);
                return resultMsg;

            }

            ConcurrentHashMap<String, Integer> chm_shareType = chm_shares.get(shareType);

            if (chm_shareType == null) {
                resultMsg = "Invalid share type provided. Please provide a valid share type.";
                responseStatus = "Failure";
                logger.log("addShare\t" + "shareID: " + shareID + ", shareType: " + shareType + ", capacity: "
                        + capacity + "Status: " + responseStatus + ", serverResponse: " + resultMsg);
                return resultMsg;
            }

            // Checks whether the shareID is already available in the table
            chm_shareType.put(shareID, chm_shareType.getOrDefault(shareID, 0) + capacity);
            resultMsg = format("Successfully added %d shares to (%s, %s)", capacity, shareType, shareID);

        } catch (Exception ex) {
            resultMsg = "Unable to add shares to the DSMSService. Exception: " + ex.getMessage();
            responseStatus = "Failure";
        }

        logger.log("addShare\t" + "shareID: " + shareID + ", shareType: " + shareType + ", capacity: " + capacity
                + "Status: " + responseStatus + ", serverResponse: " + resultMsg);

        return resultMsg;
    }

    /**
     * @param shareID   ID of the share to be removed
     * @param shareType Type of the share to be removed
     * @return Returns the status of the operation
     */
    @Override
    public String removeShare(String shareID, String shareType) {
        String resultMsg;
        String responseStatus = "Success";

        try {
            if (!shareID.substring(0, 3).equals(serverName)) {
                resultMsg = "Invalid server for removing share. Please contact respective server.";
                logger.log("removeShare\t" + "shareID: " + shareID + ", shareType: " + shareType + "Status: "
                        + responseStatus + ", serverResponse: " + resultMsg);
                return resultMsg;
            }

            ConcurrentHashMap<String, Integer> chm_shareType = chm_shares.get(shareType);

            if (chm_shareType == null) {
                resultMsg = "Invalid share type provided. Please provide a valid share type.";
                responseStatus = "Failure";
                logger.log("removeShare\t" + "shareID: " + shareID + ", shareType: " + shareType + "Status: "
                        + responseStatus + ", serverResponse: " + resultMsg);
                return resultMsg;
            }

            Integer removedCount = chm_shareType.remove(shareID);
            if (removedCount != null) {
                resultMsg = format("Successfully removed %d shares to (%s, %s)", removedCount, shareType, shareID);
            } else {
                resultMsg = "No shares of the provided type are available in the DSMSService. So, no changes were made.";
            }

        } catch (Exception ex) {
            resultMsg = "Unable to remove shares from the DSMSService. Exception: " + ex.getMessage();
            responseStatus = "Failure";
        }

        logger.log("removeShare\t" + "shareID: " + shareID + ", shareType: " + shareType + "Status: " + responseStatus
                + ", serverResponse: " + resultMsg);

        return resultMsg;
    }

    private String getSharesOfCurrentServer(String shareType) {
        StringBuilder sb = new StringBuilder();

        Set<Map.Entry<String, Integer>> userShares = chm_shares.getOrDefault(shareType, new ConcurrentHashMap<>())
                .entrySet();
        // Append the shares of the current server
        for (Map.Entry<String, Integer> entry : userShares) {
            sb.append(entry.getKey()).append(" ").append(entry.getValue()).append(", \n");
        }

        // Remove the trailing comma and space if there are elements
        if (userShares.isEmpty()) {
            sb.setLength(0);
            sb.append("\nNo shares of type - ").append(shareType).append(" found in server:").append(serverName)
                    .append("\n");
        } else {
            sb.setLength(sb.length() - 3);
            sb.append("\n");
        }

        return sb.toString();
    }

    private String getSharesOfBuyerInServer(String buyerID) {
        StringBuilder sb = new StringBuilder();
        sb.append("\nServer:").append(serverName);

        Set<Map.Entry<String, ConcurrentHashMap<String, Integer>>> userShares = chm_userShares
                .getOrDefault(buyerID, new ConcurrentHashMap<>()).entrySet();
        for (Map.Entry<String, ConcurrentHashMap<String, Integer>> shareTypeMap : userShares) {
            sb.append("\n").append(shareTypeMap.getKey()).append("\n");
            for (Map.Entry<String, Integer> shares : shareTypeMap.getValue().entrySet()) {
                sb.append(shares.getKey()).append(" : ").append(shares.getValue()).append("\n");
            }
        }

        // Remove the trailing comma and space if there are elements
        if (userShares.isEmpty()) {
            sb.append("\nNo shares of buyer - ").append(buyerID).append(" found in server:").append(serverName)
                    .append("\n");
        }

        return sb.toString();
    }

    /**
     * @param shareType The type of share for which the share availability has to be
     *                  listed
     * @return The Share availability in all servers as a presentable string
     */
    @Override
    public String listShareAvailability(String shareType) {
        StringBuilder sb = new StringBuilder();
        String responseStatus = "Success";

        try {
            if (chm_shares.get(shareType) == null) {
                sb.append("\nInvalid share type provided. Please provide a valid share type.\n");
                responseStatus = "Failure";
                logger.log("listShareAvailability\t" + "shareType: " + shareType + "Status: " + responseStatus
                        + ", serverResponse: " + sb.toString());
                return sb.toString();
            }

            // Get the list of available shares from the current server
            sb.append(getSharesOfCurrentServer(shareType));

            // Get the info from the other servers
            String udpReqMsg = format("%s:%s:%s", serverName, "listShareAvailability", shareType);

            // Create a new UDP socket to send the listShareAvailablity request
            // This is done to ensure there is no overlapping between the sender and
            // receiver logics
            // in UDP communication

            for (int i = 0; i < lst_serverAdr.size(); i++) {

                String curServAddr = lst_serverAdr.get(i);
                int curServPort = lst_serverPorts.get(i);
                if (curServPort != this.udpReceiverPort) {
                    DatagramPacket sendPacket = new DatagramPacket(
                            udpReqMsg.getBytes(), udpReqMsg.length(),
                            InetAddress.getByName(curServAddr), curServPort);

                    System.out.println("Requesting Server " + curServAddr + ":" + curServPort + " for shares...");
                    udpSenderSocket.send(sendPacket);

                    // Create a buffer to receive the server's response
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Wait for the response from the server
                    udpSenderSocket.receive(receivePacket);

                    System.out.println("Obtained shares from " + curServAddr + ":" + curServPort + "!");

                    // Convert the response byte data into a string
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    System.out.println(response);

                    sb.append(response);

                }
            }
        } catch (IOException ex) {
            sb.setLength(0);
            sb.append("\nUnable to list shares. Exception: ").append(ex.getMessage()).append("\n");
            responseStatus = "Failure";
        }

        logger.log("listShareAvailability\t" + "shareType: " + shareType + "Status: " + responseStatus
                + ", serverResponse: " + sb.toString());

        return sb.toString();
    }

    private boolean hasUserPurchasedShareToday(ConcurrentHashMap<String, Integer> chm_thisUsersTypeShares,
                                               String shareID) {
        String dateOfPurchase = shareID.substring(shareID.length() - 6);

        for (String purchasedShares : chm_thisUsersTypeShares.keySet()) {
            String date = purchasedShares.substring(purchasedShares.length() - 6);
            if (date.equals(dateOfPurchase)) {
                return true;
            }
        }

        return false;
    }

    private String localPurchaseShare(String buyerID, String shareID, String shareType, int shareCount) {
        String resultMsg = "";
        String responseStatus = "Success";

        if (!chm_userShares.containsKey(buyerID)) {
            chm_userShares.put(buyerID, new ConcurrentHashMap<>());
        } else {
            // If the buyer is not from this server, do not allow them to purchase more than
            // 3 stocks
            if (!buyerID.substring(0, 3).equals(serverName)) {
                if (chm_userShares.get(buyerID).size() >= 3) {
                    resultMsg = format("buyerID %d is not allowed to make more than 3 purchases this week", buyerID);
                    logger.log("purchaseShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareType: "
                            + shareType + ", shareCount: " + shareCount + "Status: " + responseStatus
                            + ", serverResponse: " + resultMsg);
                    return resultMsg;
                }
            }
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_thisUserShares = chm_userShares.get(buyerID);

        if (!chm_shares.containsKey(shareType)) {
            resultMsg = "Invalid share type entered!";
            logger.log("purchaseShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareType: "
                    + shareType + ", shareCount: " + shareCount + "Status: " + responseStatus
                    + ", serverResponse: " + resultMsg);
            return resultMsg;

        }

        Integer sharesAvailable = chm_shares.get(shareType).get(shareID);

        if (sharesAvailable == null) {
            resultMsg = format("\nNo shares found for %s:%s\n", shareType, shareID);
        } else {

            int purchasedShares = sharesAvailable >= shareCount ? shareCount : sharesAvailable;
            // Checks whether the shareID is already available in the table

            if (chm_thisUserShares.containsKey(shareType)) {
                // The user has already purchased this share type and share id
                // we should not allow this action
                resultMsg = "\nYou have already made a purchase under this shareID and shareType. No more purchases are allowed.\n";

                logger.log("purchaseShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareType: "
                        + shareType + ", shareCount: " + shareCount + "Status: " + responseStatus
                        + ", serverResponse: " + resultMsg);
                return resultMsg;
            }

            ConcurrentHashMap<String, Integer> chm_thisUsersTypeShares = chm_thisUserShares.get(shareType);
            if (chm_thisUsersTypeShares == null) {
                chm_thisUserShares.put(shareType, new ConcurrentHashMap<>());
                chm_thisUsersTypeShares = chm_thisUserShares.get(shareType);
            }

            if (hasUserPurchasedShareToday(chm_thisUsersTypeShares, shareID)) {
                // The user has already purchased this share type at the same day
                // this action is also not allowed
                resultMsg = "\nYou have already made a purchase under this date and shareType. No more purchases are allowed.\n";

                logger.log("purchaseShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareType: "
                        + shareType + ", shareCount: " + shareCount + "Status: " + responseStatus
                        + ", serverResponse: " + resultMsg);
                return resultMsg;
            } else {
                chm_thisUsersTypeShares.put(shareID, purchasedShares);
                chm_thisUserShares.put(shareType, chm_thisUsersTypeShares);
                chm_userShares.put(buyerID, chm_thisUserShares);

                // Update / remove the shareID based on the purchased shares
                if (purchasedShares == sharesAvailable) {
                    chm_shares.get(shareType).remove(shareID);
                } else {
                    chm_shares.get(shareType).put(shareID, sharesAvailable - purchasedShares);
                }

                chm_shares.get(shareType).put(shareID,
                        purchasedShares == sharesAvailable ? 0 : sharesAvailable - purchasedShares);
                resultMsg = format("\nSuccessfully purchased %d of (%s:%s) shares for the user %s\n",
                        purchasedShares, shareType, shareID, buyerID);
            }
        }
        return resultMsg;
    }

    // Gets the quantity of the shares available in the current server for the given
    // buyer
    private int localGetBuyerShareQuantity(String buyerID, String shareID, String shareType) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_thisUserShares = chm_userShares.get(buyerID);
        if (chm_thisUserShares != null) {
            ConcurrentHashMap<String, Integer> chm_thisUsersTypeShares = chm_thisUserShares.get(shareType);
            if (chm_thisUsersTypeShares != null) {
                return chm_thisUsersTypeShares.getOrDefault(shareID, 0);
            }
        }

        return 0;
    }

    // Gets the quantity of the shares available in the current server
    private int localGetShareQuantity(String shareID, String shareType) {
        return chm_shares.getOrDefault(shareType, new ConcurrentHashMap<>()).getOrDefault(shareID, 0);
    }

    // Sells the share if available in the current server
    private boolean localSellShare(String buyerID, String shareID, int shareCount) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_thisUserShares = chm_userShares.get(buyerID);
        String shareType = findShareID(chm_thisUserShares, shareID);
        if (!shareType.isEmpty()) {
            int sharesAvailable = chm_thisUserShares.get(shareType).get(shareID);

            if (sharesAvailable == 0) {
                return false;
            } else {
                int soldShares = Math.min(sharesAvailable, shareCount);

                // Update / remove the shareID based on the purchased shares
                if (soldShares == sharesAvailable) {
                    chm_thisUserShares.get(shareType).remove(shareID);

                    // If all shares are sold, remove the share type as well
                    if (chm_thisUserShares.get(shareType).isEmpty()) {
                        chm_thisUserShares.remove(shareType);
                    }
                } else {
                    chm_thisUserShares.get(shareType).put(shareID, sharesAvailable - soldShares);
                }

                // Update the sold shares in the main shares table
                int newShareCount = chm_shares.get(shareType).getOrDefault(shareID, 0) + soldShares;
                chm_shares.get(shareType).put(shareID, newShareCount);

                return true;
            }
        }

        return false;
    }

    /**
     * @param buyerID    The buyer who is requesting the purchase of shares
     * @param shareID    The ShareID which needs to be purchased
     * @param shareType  The type of share which needs to be purchased (Equity,
     *                   Bonus or Dividend)
     * @param shareCount The amount of shares to be purchased
     * @return The status message of the purchase
     */
    @Override
    public String purchaseShare(String buyerID, String shareID, String shareType, int shareCount) {
        String resultMsg = "";
        String responseStatus = "Success";

        try {

            // Check whether share is for this server first. If not, forward the request to
            // the correct server
            if (shareID.startsWith(serverName)) {
                resultMsg = localPurchaseShare(buyerID, shareID, shareType, shareCount);
            } else {
                // Forward the request to the correct server
                String shareServer = shareID.substring(0, 3);

                // Get the server address and port
                int buyerServerIndex = lst_serverNames.indexOf(shareServer);
                if (buyerServerIndex != -1) {
                    String buyerServer = lst_serverAdr.get(buyerServerIndex);

                    String requestQuery = serverName + ":purchaseShare:" + buyerID + ":" + shareID + ":" + shareType
                            + ":" + shareCount;

                    DatagramPacket sendPacket = new DatagramPacket(
                            requestQuery.getBytes(), requestQuery.length(),
                            InetAddress.getByName(buyerServer), lst_serverPorts.get(buyerServerIndex));

                    System.out.println("Forwarding the request to " + buyerServer + " for shares...");
                    udpSenderSocket.send(sendPacket);

                    // Create a buffer to receive the server's response
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Wait for the response from the server
                    udpSenderSocket.receive(receivePacket);

                    System.out.println("Obtained shares from " + buyerServer + "!");

                    // Convert the response byte data into a string
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    System.out.println(response);

                    resultMsg = response;
                } else {
                    resultMsg = "\nServer not found!\n";
                }
            }
        } catch (IOException ex) {
            resultMsg = "\nUnable to add shares to the DSMSService. Exception: " + ex.getMessage();
            responseStatus = "Failure";
        }

        logger.log("purchaseShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareType: " + shareType
                + ", shareCount: " + shareCount + "Status: " + responseStatus + ", serverResponse: " + resultMsg);

        return resultMsg;
    }

    /**
     * @param buyerID The buyer whose shares need to be shown
     * @return The return message containing the information of the buyer's shares
     */
    @Override
    public String getShares(String buyerID) {
        StringBuilder resultMsg = new StringBuilder();
        String responseStatus = "Success";

        try {
            for (int i = 0; i < lst_serverAdr.size(); i++) {
                if (lst_serverNames.get(i).equals(serverName)) {
                    resultMsg.append("Server: ").append(serverName).append("\n");
                    resultMsg.append(getSharesOfBuyerInServer(buyerID));
                } else {
                    String buyerServer = lst_serverAdr.get(i);
                    String requestQuery = serverName + ":getSharesOfBuyerInServer:" + buyerID;

                    DatagramPacket sendPacket = new DatagramPacket(
                            requestQuery.getBytes(), requestQuery.length(),
                            InetAddress.getByName(buyerServer), lst_serverPorts.get(i));

                    System.out.println("Forwarding the request to " + buyerServer + " for shares...");
                    udpSenderSocket.send(sendPacket);

                    // Create a buffer to receive the server's response
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Wait for the response from the server
                    udpSenderSocket.receive(receivePacket);

                    System.out.println("Obtained shares from " + buyerServer + "!");

                    // Convert the response byte data into a string
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    System.out.println(response);
                    resultMsg.append("Server: ").append(buyerServer).append("\n");

                    resultMsg.append(response);
                }

            }
        } catch (IOException ex) {
            resultMsg.setLength(0);
            resultMsg.append("Unable to add shares to the DSMSService. Exception: ").append(ex.getMessage());
            responseStatus = "Failure";
        }

        logger.log("getShares\t" + "buyerID: " + buyerID + "Status: " + responseStatus + ", serverResponse: "
                + resultMsg.toString());

        return resultMsg.toString();
    }

    private String findShareID(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_thisUsersShares,
                               String shareID) {
        String shareType = "";

        for (String boughtShareType : chm_thisUsersShares.keySet()) {
            if (chm_thisUsersShares.get(boughtShareType).containsKey(shareID)) {
                shareType = boughtShareType;
                break;
            }
        }

        return shareType;
    }

    /**
     * @param buyerID
     * @param shareID
     * @param shareCount
     */
    @Override
    public String sellShare(String buyerID, String shareID, int shareCount) {
        StringBuilder resultMsg = new StringBuilder();
        String responseStatus = "Success";

        try {
            // Check whether share is for this server first. If not, forward the request to
            // the correct server
            if (shareID.startsWith(serverName)) {

                ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> chm_thisUserShares = chm_userShares
                        .getOrDefault(buyerID, new ConcurrentHashMap<>());

                // User can only sell the shareID they have bought
                String shareType = findShareID(chm_thisUserShares, shareID);
                if (!shareType.isEmpty()) {
                    int sharesAvailable = chm_thisUserShares.get(shareType).get(shareID);

                    if (sharesAvailable == 0) {
                        resultMsg.append(format("No shares found for %s", shareID));
                    } else {
                        int soldShares = Math.min(sharesAvailable, shareCount);

                        // Update / remove the shareID based on the purchased shares
                        if (soldShares == sharesAvailable) {
                            resultMsg.append("Shares available for the user are ").append(sharesAvailable)
                                    .append(". So selling all shares available.\n");
                            chm_thisUserShares.get(shareType).remove(shareID);

                        } else {
                            chm_thisUserShares.get(shareType).put(shareID, sharesAvailable - soldShares);
                        }

                        // Update the sold shares in the main shares table
                        int newShareCount = chm_shares.get(shareType).getOrDefault(shareID, 0) + soldShares;
                        chm_shares.get(shareType).put(shareID, newShareCount);

                        resultMsg.append(format("Successfully sold %d of (%s) shares for the user %s", soldShares,
                                shareID, buyerID));
                    }
                } else {
                    resultMsg.append(format("No shares found for %s", shareID));
                }

            } else {
                // Forward the request to the correct server
                String shareServer = shareID.substring(0, 3);

                // Get the server address and port
                int buyerServerIndex = lst_serverNames.indexOf(shareServer);
                if (buyerServerIndex != -1) {
                    String buyerServer = lst_serverAdr.get(buyerServerIndex);

                    String requestQuery = serverName + ":sellShare:" + buyerID + ":" + shareID + ":" + shareCount;

                    DatagramPacket sendPacket = new DatagramPacket(
                            requestQuery.getBytes(), requestQuery.length(),
                            InetAddress.getByName(buyerServer), lst_serverPorts.get(buyerServerIndex));

                    System.out.println("Forwarding the request to " + buyerServer + " for shares...");
                    udpSenderSocket.send(sendPacket);

                    // Create a buffer to receive the server's response
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Wait for the response from the server
                    udpSenderSocket.receive(receivePacket);

                    System.out.println("Obtained shares from " + buyerServer + "!");

                    // Convert the response byte data into a string
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    System.out.println(response);

                    resultMsg.append(response);
                } else {
                    resultMsg.append("Server not found!");
                }
            }
        } catch (IOException ex) {
            resultMsg.setLength(0);
            resultMsg.append("Unable to add shares to the DSMSService. Exception: ").append(ex.getMessage());
            responseStatus = "Failure";
        }

        logger.log("sellShare\t" + "buyerID: " + buyerID + ", shareID: " + shareID + ", shareCount: " + shareCount
                + "Status: " + responseStatus + ", serverResponse: " + resultMsg.toString());

        return resultMsg.toString();
    }

    @Override
    public String swapShares(String buyerID, String oldShareID, String oldShareType, String newShareID,
                             String newShareType) {

        boolean isBuyNewShare = false;
        String resultMsg = "";
        String responseStatus = "Success";
        int sharesToBeSwapped = 0, newShareCount = 0;

        try {// Check if the buyer has the old share and get the quanitity of it
            if (oldShareID.startsWith(serverName)) {
                sharesToBeSwapped = localGetBuyerShareQuantity(buyerID, oldShareID, oldShareType);
            } else
            {
                // Forward the request to the correct server
                String shareServer = oldShareID.substring(0, 3);

                // Get the server address and port
                int buyerServerIndex = lst_serverNames.indexOf(shareServer);
                if (buyerServerIndex != -1) {
                    String buyerServer = lst_serverAdr.get(buyerServerIndex);
                    String buyerServerName = lst_serverNames.get(buyerServerIndex);

                    String requestQuery = serverName + ":swapCheckSellShareAvailability:" + buyerID + ":" + oldShareID
                            + ":"
                            + oldShareType;

                    DatagramPacket sendPacket = new DatagramPacket(
                            requestQuery.getBytes(), requestQuery.length(),
                            InetAddress.getByName(buyerServer), lst_serverPorts.get(buyerServerIndex));

                    System.out.println("Forwarding the request to " + buyerServerName + " for shares...");
                    udpSenderSocket.send(sendPacket);

                    // Create a buffer to receive the server's response
                    byte[] receiveData = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                    // Wait for the response from the server
                    udpSenderSocket.receive(receivePacket);

                    System.out.println("Obtained share quantity from " + buyerServerName + "!");

                    // Convert the response byte data into a string
                    String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                    sharesToBeSwapped = Integer.parseInt(response);

                    if (sharesToBeSwapped > 0) {
                        System.out.println("The buyer has the old share to swap");
                    }
                } else {
                    resultMsg = "\nServer not found!\n";
                }
            }

            if (sharesToBeSwapped == 0) {
                System.out.println("The buyer does not have the old share to swap");
                resultMsg = "The buyer does not have the old share to swap";
            } else {
                // Check if the new share is from the current server
                // otherwise forward the request to the correct server
                if (newShareID.startsWith(serverName)) {
                    newShareCount = localGetShareQuantity(newShareID, newShareType);
                } else {

                    // Forward the request to the correct server
                    String shareServer = newShareID.substring(0, 3);

                    // Get the server address and port
                    int buyerServerIndex = lst_serverNames.indexOf(shareServer);
                    if (buyerServerIndex != -1) {
                        String buyerServer = lst_serverAdr.get(buyerServerIndex);
                        String buyerServerName = lst_serverNames.get(buyerServerIndex);

                        String requestQuery = serverName + ":swapCheckPurchaseShareAvailability:" + newShareID + ":"
                                + newShareType;

                        DatagramPacket sendPacket = new DatagramPacket(
                                requestQuery.getBytes(), requestQuery.length(),
                                InetAddress.getByName(buyerServer), lst_serverPorts.get(buyerServerIndex));

                        System.out.println("Forwarding the request to " + buyerServerName + " for shares...");
                        udpSenderSocket.send(sendPacket);

                        // Create a buffer to receive the server's response
                        byte[] receiveData = new byte[1024];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

                        // Wait for the response from the server
                        udpSenderSocket.receive(receivePacket);

                        System.out.println("Obtained share quantity from " + buyerServerName + "!");

                        // Convert the response byte data into a string
                        String response = new String(receivePacket.getData(), 0, receivePacket.getLength());

                        newShareCount = Integer.parseInt(response);

                        if (newShareCount > 0) {
                            System.out.println("The buyer has the new share to swap as well");
                        }
                    } else {
                        resultMsg = "\nServer not found!\n";
                    }
                }

            }

            if (newShareCount < sharesToBeSwapped) {
                System.out.println("The buyer does not have enough new shares to swap");
                resultMsg = "The buyer does not have enough new shares to swap";

            } else {
                // sell the old share and purchase the new share
                resultMsg = purchaseShare(buyerID, newShareID, newShareType, sharesToBeSwapped);
                if (resultMsg.contains("Success"))
                    resultMsg += sellShare(buyerID, oldShareID, sharesToBeSwapped);

            }
        } catch (Exception ex) {
            resultMsg = "Unable to swap shares. Exception: " + ex.getMessage();
            responseStatus = "Failure";
        }

        logger.log("swapShares\t" + "buyerID: " + buyerID + ", oldShareID: " + oldShareID + ", oldShareType: "
                + oldShareType + ", newShareID: " + newShareID + ", newShareType: " + newShareType + "Status: "
                + responseStatus + ", serverResponse: " + resultMsg);

        return resultMsg;
    }

    /**
     * When an object implementing interface {@code Runnable} is used to create
     * a thread, starting the thread causes the object's {@code run} method to
     * be called in that separately executing thread.
     * <p>
     * The general contract of the method {@code run} is that it may take any
     * action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        try {
            // Listen for UDP messages from other servers
            while (true) {
                byte[] receiveData = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                udpReceiverSocket.receive(receivePacket);
                String message = new String(receivePacket.getData(), 0, receivePacket.getLength());

                String[] udpRequest = message.split(":");
                String replyMessage = "";

                switch (udpRequest[1]) {
                    case "purchaseShare":
                        replyMessage = localPurchaseShare(udpRequest[2], udpRequest[3], udpRequest[4],
                                Integer.parseInt(udpRequest[5]));
                        break;
                    case "listShareAvailability":
                        // Print the request from the other servers
                        System.out.println(serverName + " received UDP message : " + message);

                        // Get the shares from the current server
                        replyMessage = getSharesOfCurrentServer(udpRequest[2]);
                        break;
                    case "getSharesOfBuyerInServer":
                        replyMessage = getSharesOfBuyerInServer(udpRequest[2]);
                        break;
                    case "sellShare":
                        replyMessage = sellShare(udpRequest[2], udpRequest[3], Integer.parseInt(udpRequest[4]));
                        break;
                    case "swapCheckSellShareAvailability":
                        replyMessage = String
                                .valueOf(localGetBuyerShareQuantity(udpRequest[2], udpRequest[3], udpRequest[4]));
                        break;
                    case "swapCheckPurchaseShareAvailability":
                        replyMessage = String.valueOf(localGetShareQuantity(udpRequest[2], udpRequest[3]));
                        break;
                    default:
                        System.err.println("Invalid UDP request received: " + message);
                        logger.log("Invalid UDP request received: " + message);
                        break;
                }

                // Send it as the response for the UDP request
                DatagramPacket sendPacket = new DatagramPacket(
                        replyMessage.getBytes(), replyMessage.length(),
                        receivePacket.getAddress(), receivePacket.getPort());
                udpReceiverSocket.send(sendPacket);

            }
        } catch (Exception e) {
            System.err.println("Error occurred while listening for UDP messages: " + e.getMessage());
            logger.log("Error occurred while listening for UDP messages: " + e.getMessage());
        }

    }
}
