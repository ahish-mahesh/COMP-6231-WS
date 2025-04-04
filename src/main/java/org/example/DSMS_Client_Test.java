package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DSMS_Client_Test {

    public static void scenario1(String args[]) {
        ExecutorService executor = Executors.newFixedThreadPool(15);

        // Admin Test Cases
        executor.execute(new AdminClient( "NYKA1001", "add", "NYKM100925", "Equity", 10));
        executor.execute(new AdminClient( "NYKA1001", "add", "NYKM101025", "Equity", 10));
        executor.execute(new AdminClient( "NYKA1002", "add", "NYKE110925", "Bonus", 20));
        executor.execute(new AdminClient( "NYKA1003", "add", "NYKE120925", "Dividend", 18));
        executor.execute(new AdminClient( "LONA2001", "add", "LONM101025", "Equity", 15));
        executor.execute(new AdminClient( "LONA2002", "add", "LONE110925", "Bonus", 20));
        executor.execute(new AdminClient( "LONA2003", "add", "LONE120925", "Dividend", 18));
        executor.execute(new AdminClient( "TOKA3001", "add", "TOKM101125", "Equity", 20));
        executor.execute(new AdminClient( "TOKA3002", "add", "TOKE110925", "Bonus", 20));
        executor.execute(new AdminClient( "TOKA3003", "add", "TOKE120925", "Dividend", 18));

        // remove a few shares
        executor.execute(new AdminClient( "TOKA3003", "remove", "TOKE120925", "Dividend", 2));
        executor.execute(new AdminClient( "NYKA1002", "remove", "NYKE110925", "Bonus", 1));
        executor.execute(new AdminClient( "LONA2003", "remove", "LONE120925", "Dividend", 3));

        // Enter invalid shareIDs
        executor.execute(new AdminClient( "TOKA3003", "add", "TOKE925", "Dividend", 18));
        executor.execute(new AdminClient( "TOKA3003", "add", "TOKS120925", "Dividend", 18));
        executor.execute(new AdminClient( "TOKA3003", "add", "TOKE122225", "Dividend", 18));

        // Wait for the admin add operations to complete
        try {
            Thread.sleep(3000);
            System.out.println();
            System.out.println("Waiting till admin actions are complete!");
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // List all the shares in the server
        executor.execute(new AdminClient( "NYKA1001", "list", "", "Equity", 5));
        executor.execute(new AdminClient( "TOKA3001", "list", "", "Bonus", 5));
        executor.execute(new AdminClient( "LONA2003", "list", "", "Dividend", 5));

        // Buyer Test Cases
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 2));
        executor.execute(new BuyerClient( "LONB5001", "buy", "LONM101025", "Bonus", 5));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "LONM101025", "Bonus", 3));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "TOKM101125", "Dividend", 4));
        executor.execute(new BuyerClient( "LONB5001", "buy", "TOKE110925", "Bonus", 4));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "NYKM100925", "Equity", 1));

        // Wait for the buyers to buy their stocks
        try {
            Thread.sleep(2000);
            System.out.println();
            System.out.println("Waiting till admin actions are complete!");
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Buyer selling test cases
        executor.execute(new BuyerClient( "NYKB4001", "sell", "NYKM100925", "Equity", 1));
        executor.execute(new BuyerClient( "LONB5001", "sell", "LONM101025", "Bonus", 2));
        executor.execute(new BuyerClient( "LONB5001", "sell", "TOKE110925", "Bonus", 1));
        executor.execute(new BuyerClient( "TOKB6001", "sell", "TOKM101125", "Dividend", 1));

        // Buying no shares
        executor.execute(new BuyerClient( "LONB5001", "getShares", "", "", 0));

        // Edge Cases
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 10)); // Over-purchase
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 1)); // Duplicate purchase
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM101025", "Equity", 1)); // Duplicate purchase of
        // same share type
        executor.execute(new BuyerClient( "LONB5001", "buy", "LONM101025", "Bonus", 60)); // Exceeding
        // out-of-market limit

        // Buyer trying admin functions
        executor.execute(new AdminClient( "TOKB3001", "remove", "TOKM101125", "Dividend", 20));
        executor.execute(new AdminClient( "LONB2001", "add", "LONM101025", "Bonus", 15));
        executor.execute(new AdminClient( "NYKB1001", "list", "", "Equity", 5));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // list final availability of shares

        executor.execute(new AdminClient( "NYKA1001", "list", "", "Equity", 5));
        executor.execute(new AdminClient( "TOKA3001", "list", "", "Equity", 5));
        executor.execute(new AdminClient( "LONA2003", "list", "", "Equity", 5));

        executor.shutdown();
    }

    public static void noSharesScenario(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(15);

        executor.execute(new AdminClient( "NYKA1001", "list", "", "Equity", 5));

        // Buyer Test Cases
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 2));
        executor.execute(new BuyerClient( "LONB5001", "buy", "LONM101025", "Bonus", 1));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "LONM101025", "Bonus", 1));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "TOKM101125", "Dividend", 1));
        executor.execute(new BuyerClient( "TOKB6001", "buy", "NYKM100925", "Equity", 1));
        executor.execute(new BuyerClient( "NYKB4001", "sell", "NYKM100925", "Equity", 1));
        executor.execute(new BuyerClient( "LONB5001", "getShares", "", "", 0));

        // Edge Cases
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 10)); // Over-purchase
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 1)); // Duplicate purchase
        executor.execute(new BuyerClient( "LONB5001", "buy", "LONM101025", "Bonus", 60)); // Exceeding
        // out-of-market limit

        // Buyer trying admin functions
        executor.execute(new AdminClient( "TOKB3001", "remove", "TOKM101125", "Dividend", 20));
        executor.execute(new AdminClient( "LONB2001", "add", "LONM101025", "Bonus", 15));
        executor.execute(new AdminClient( "NYKB1001", "list", "", "Equity", 5));

        executor.shutdown();
    }

    public static void buyerTryingAdminFunc(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(15);

        // Admin Test Cases
        executor.execute(new AdminClient( "NYKA1001", "add", "NYKM100925", "Equity", 10));
        executor.execute(new AdminClient( "NYKA1002", "add", "NYKE110925", "Bonus", 20));
        executor.execute(new AdminClient( "NYKA1003", "add", "NYKE120925", "Dividend", 18));
        executor.execute(new AdminClient( "LONA2001", "add", "LONM101025", "Bonus", 15));
        executor.execute(new AdminClient( "LONA2002", "add", "LONE110925", "Bonus", 20));
        executor.execute(new AdminClient( "LONA2003", "add", "LONE120925", "Dividend", 18));
        executor.execute(new AdminClient( "TOKA3001", "add", "TOKM101125", "Dividend", 20));
        executor.execute(new AdminClient( "TOKA3002", "add", "TOKE110925", "Bonus", 20));
        executor.execute(new AdminClient( "TOKA3003", "add", "TOKE120925", "Dividend", 18));

        // Buyer trying admin functions
        executor.execute(new AdminClient( "TOKB3001", "remove", "TOKM101125", "Dividend", 20));
        executor.execute(new AdminClient( "LONB2001", "add", "LONM101025", "Bonus", 15));
        executor.execute(new AdminClient( "NYKB1001", "list", "", "Equity", 5));

        executor.shutdown();
    }

    public static void scenarioSwapShares(String args[]) {
        ExecutorService executor = Executors.newFixedThreadPool(15);

        // Setup: Admin adds shares
        executor.execute(new AdminClient( "NYKA1001", "add", "NYKM100925", "Equity", 10));
        executor.execute(new AdminClient( "LONA2001", "add", "LONM101025", "Bonus", 15));

        // Buyer purchases a share
        executor.execute(new BuyerClient( "NYKB4001", "buy", "NYKM100925", "Equity", 2));
        executor.execute(new BuyerClient( "LONB5001", "buy", "LONM101025", "Bonus", 5));

        // Wait for purchase to complete
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Successful swap
        executor.execute(
                new SwapBuyerClient( "NYKB4001", "swapShares", "NYKM100925", "Equity", "LONM101025", "Bonus"));

        // Edge Cases
        executor.execute(
                new SwapBuyerClient( "NYKB4001", "swapShares", "NYKM999999", "Equity", "LONM101025", "Bonus")); // Old
        // share
        // doesn't
        // exist
        executor.execute(
                new SwapBuyerClient( "NYKB4001", "swapShares", "NYKM100925", "Equity", "LONM999999", "Bonus")); // New
        // share
        // doesn't
        // exist
        executor.execute(
                new SwapBuyerClient( "NYKB4001", "swapShares", "NYKM100925", "Equity", "LONM101025", "Bonus")); // Atomicity
        // test

        executor.shutdown();
    }

    public static void main(String[] args) {
        scenario1(args);
        scenarioSwapShares(args);
    }
}

class AdminClient implements Runnable {
    private String userID, action, shareID, shareType;
    private int quantity;
    private String[] args;

    public AdminClient(String userID, String action, String shareID, String shareType, int quantity) {
        this.userID = userID;
        this.action = action;
        this.shareID = shareID;
        this.shareType = shareType;
        this.quantity = quantity;
        this.args = args;
    }

    @Override
    public void run() {
        DSMS_Client client = new DSMS_Client( userID);
        switch (action) {
            case "add":
                client.addShares(shareID, shareType, quantity);
                break;
            case "remove":
                client.removeShares(shareID, shareType);
                break;
            case "list":
                client.listShareAvailability(shareType);
                break;
        }
    }
}

class BuyerClient implements Runnable {
    private String userID, action, shareID, shareType;
    private int quantity;
    private String[] args;

    public BuyerClient(String userID, String action, String shareID, String shareType, int quantity) {
        this.userID = userID;
        this.action = action;
        this.shareID = shareID;
        this.shareType = shareType;
        this.quantity = quantity;
        this.args = args;
    }

    @Override
    public void run() {
        DSMS_Client client = new DSMS_Client( userID);
        switch (action) {
            case "buy":
                client.buyShares(shareID, shareType, quantity);
                break;
            case "sell":
                client.sellShares(shareID, quantity);
                break;
            case "getShares":
                client.getShares();
                break;
        }
    }
}

class SwapBuyerClient implements Runnable {
    private String userID, action, oldShareID, oldShareType, newShareID, newShareType;
    private int quantity;
    private String[] args;

    public SwapBuyerClient(String userID, String action, String oldShareID, String oldShareType,
                           String newShareID, String newShareType) {
        this.userID = userID;
        this.action = action;
        this.oldShareID = oldShareID;
        this.oldShareType = oldShareType;
        this.newShareID = newShareID;
        this.newShareType = newShareType;
        this.args = args;
    }

    @Override
    public void run() {
        DSMS_Client client = new DSMS_Client( userID);
        switch (action) {
            case "swapShares":
                client.swapShares(oldShareID, oldShareType, newShareID, newShareType);
                break;
        }
    }
}
