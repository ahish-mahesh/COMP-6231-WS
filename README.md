# Distributed Share Management System (Web Services)

## Done by: Ahish Mahesh

## Software Architecture
The solution consists of three servers (NYK, TOK, and LON) hosted as Web Servers, along with client applications that communicate with these servers. The servers interact among themselves using UDP.

- Clients communicate with their respective servers to buy and sell shares.
- If clients need to buy shares from another location, the transaction is performed through their own server.
- The servers use UDP to exchange information and facilitate buying, selling, and swapping of shares among clients.

## Users of the System

### Admins
- Can add, remove, and list share availability in their respective servers.
- Can buy, sell, swap, and display their purchased shares in each server.

### Buyers
- Can only buy, sell, swap, and display their purchased shares.

## Data Structures Used

### ConcurrentHashMap
- Used to store share information on each server and track buyers' purchase history.
- **Shares HashMap**: A two-layer `ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>` to store share details.
- **User Shares HashMap**: A three-layer `ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>>` to maintain purchase history, including cross-server purchases.

### CopyOnWriteArrayList
- Used to store information such as server names, addresses, and ports, ensuring thread-safe concurrent access by all servers.

## Activity Diagram
The `swapShares` method follows a step-by-step process:
1. Validates the buyer’s ownership of the shares to be sold.
2. Checks the availability of the new shares to be acquired.
3. Executes the swap if conditions are met, handling errors such as insufficient shares.

## Sequence Diagram
A sequence example:
- A NYK admin requests to list all available shares of the "Bonus" share type.
- The NYK server forwards this request to all other servers via UDP.
- The responses from all servers are collected and returned to the NYK admin.

## Crucial Part of the Solution
Ensuring atomicity in `swapShares` transactions:
- A swap can only proceed if the user possesses the shares to be exchanged and if the new shares are available.
- The shares may reside on different servers, requiring careful handling of multiple share availability scenarios before implementing the solution.

## Test Cases

### Basic Functionality Tests
1. Successful swap - Both shares on the local server with sufficient quantities.
2. Successful swap - Old share on the local server, new share on a remote server.
3. Successful swap - Old share on a remote server, new share on the local server.
4. Successful swap - Both shares on different remote servers.

### Validation Tests
5. Buyer does not own any of the old shares.
6. Not enough new shares available for the swap.

### Server Communication Tests
7. Remote server for old share is unreachable/times out.
8. Remote server for new share is unreachable/times out.
9. Remote server returns an invalid response format.

### Edge Cases
10. Swap exactly the same amount of shares as available.
11. Same share ID but different share types.
12. Attempting to swap shares with identical IDs and types.
13. Concurrent swap requests affecting the same shares.

### Boundary Tests
14. Zero shares requested for swap (invalid input).
15. Null or empty share IDs or types.
16. Invalid share types that don't exist in the system.
17. Invalid buyer ID format or non-existent buyer.

---

This document serves as the `README.md` for the Distributed Share Management System (Web Services) project, providing insights into the architecture, user roles, data structures, workflows, and test cases.
