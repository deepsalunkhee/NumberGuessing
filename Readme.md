# Number Guessing Game: Distributed Computing Concepts Analysis

## Introduction

This document analyzes a multiplayer number guessing game implemented using Java RMI (Remote Method Invocation), examining how it demonstrates key distributed computing concepts. The game allows multiple players to join rooms, make guesses, and compete to correctly identify a randomly generated number.

## 1. Communication in Distributed Systems

### Theory
Communication is fundamental to distributed systems, allowing separate processes to interact and coordinate. Key communication paradigms include:
- **Direct communication**: Processes explicitly name communication partners
- **Indirect communication**: Processes communicate through intermediaries
- **Synchronous vs. Asynchronous communication**: Whether the sender waits for a response

### Implementation Analysis
The game implements communication through Java RMI:

```java
// Client-side RMI connection
Registry registry = LocateRegistry.getRegistry(host, Constants.RMI_PORT);
server = (RMIServerInterface) registry.lookup(Constants.RMI_ID);
```

```java
// Server-side RMI setup
RMIServerInterface stub = (RMIServerInterface) UnicastRemoteObject.exportObject(server, 0);
Registry registry = LocateRegistry.createRegistry(Constants.RMI_PORT);
registry.rebind(Constants.RMI_ID, stub);
```

The implementation features:
- **Direct communication**: The client explicitly names the server via registry lookup
- **Synchronous communication**: Method calls block until a response is received
- **Stateful interactions**: The server maintains state across multiple client calls

## 2. RPC/RMI (Remote Procedure Call/Remote Method Invocation)

### Theory
RPC/RMI allows a program to execute procedures/methods on remote systems as if they were local calls. Java RMI specifically:
- Provides object-oriented remote invocation
- Handles parameter marshalling/unmarshalling
- Manages remote object references
- Handles distributed garbage collection

### Implementation Analysis
The game uses Java RMI extensively:

```java
// RMI interface definition
public interface RMIServerInterface extends Remote {
    String createRoom(String playerName) throws RemoteException;
    List<String> getAvailableRooms() throws RemoteException;
    boolean joinRoom(String roomId, String playerName) throws RemoteException;
    // Other methods...
}
```

Key RMI features demonstrated:
- **Location transparency**: Clients interact with remote methods as if local
- **Interface-based design**: Server functionality defined via `RMIServerInterface`
- **Exception handling**: `RemoteException` handling for network failures
- **Parameter passing**: Complex objects like `List<String>` passed between systems

## 3. Clock Synchronization

### Theory
Clock synchronization ensures a consistent notion of time across distributed processes. Approaches include:
- **Physical clock synchronization**: Synchronizing actual system clocks
- **Logical clock synchronization**: Using logical timestamps to order events
  - Lamport clocks: Single counter incremented on events and message exchanges
  - Vector clocks: Array of counters tracking causality between processes

### Implementation Analysis
The game implements a simple logical clock mechanism through the `ClockSync` class:

```java
public class ClockSync {
    private int logicalClock;

    public ClockSync() {
        this.logicalClock = 0;
    }

    public synchronized void increment() {
        logicalClock++;
    }

    public synchronized int getLogicalClock() {
        return logicalClock;
    }
}
```

In the server implementation:
```java
@Override
public String createRoom(String playerName) throws RemoteException {
    clockSync.increment();
    return gameLogic.createRoom(playerName);
}
```

This demonstrates:
- **Logical clock implementation**: Simple Lamport-style clock
- **Event ordering**: Clock increments with each RMI call, establishing a partial ordering
- **Synchronized access**: Thread-safe clock updates
- **Centralized approach**: Server maintains the logical clock

## 4. Multiple Nodes and Local Clocks

### Theory
Distributed systems consist of multiple nodes, each with its own local state and clock. Challenges include:
- **Clock drift**: Local clocks running at different rates
- **State consistency**: Maintaining consistent views across nodes
- **Failure independence**: Nodes can fail independently

### Implementation Analysis
The game demonstrates multiple nodes through:

```java
// Client nodes connecting to central server
public class GameClient extends JFrame {
    private RMIServerInterface server;
    private String playerName;
    private String roomId;
    // ...
}
```

Each client represents a separate node with:
- **Local state**: Client maintains its own UI state and local guesses
- **Node independence**: Clients operate independently
- **Event processing**: Each client processes events locally
- **UI updates**: Local rendering of game state

```java
// Local client state
private List<String> localGuesses = new ArrayList<>();
private boolean gameHasStarted = false;
```

The system handles the challenge of state consistency through periodic polling:
```java
// Client periodically pulls updates from server
List<String> serverGuesses = server.getGuesses(roomId);
```

## 5. Deadlock Management

### Theory
Deadlocks occur when processes hold resources while waiting for additional resources held by other processes. Management strategies include:
- **Prevention**: Design to make deadlocks impossible
- **Avoidance**: Dynamically avoid deadlock states
- **Detection and recovery**: Identify and resolve deadlocks after they occur
- **Resource allocation graph**: Model resource requests and allocations

### Implementation Analysis
The game employs deadlock prevention techniques:

```java
// Synchronized methods for resource access
public synchronized String createRoom(String playerName) {
    String roomId = "Room" + (rooms.size() + 1);
    rooms.put(roomId, new GameRoom(roomId, playerName));
    return roomId;
}
```

```java
// Thread control for clean termination
private void stopUpdateThread() {
    gameRunning.set(false);
    if (updateThread != null && updateThread.isAlive()) {
        try {
            updateThread.interrupt();
        } catch (Exception e) {
            // Ignore
        }
    }
}
```

Deadlock prevention techniques include:
- **Resource ordering**: Methods acquire locks in a consistent order
- **Atomic operations**: Operations complete without requiring additional resources
- **Thread termination**: Clean shutdown of threads prevents hanging resources
- **Timeout mechanisms**: Implicit in RMI connectivity

## 6. Mutual Exclusion

### Theory
Mutual exclusion ensures that only one process accesses a shared resource at any time. Implementation approaches include:
- **Locks/Monitors**: Synchronization primitives for resource access
- **Semaphores**: Counting mechanisms to control resource access
- **Distributed mutual exclusion**: Token-based or permission-based protocols

### Implementation Analysis
The game implements mutual exclusion through Java's `synchronized` keyword:

```java
public synchronized boolean joinRoom(String roomId, String playerName) {
    if (rooms.containsKey(roomId)) {
        return rooms.get(roomId).addPlayer(playerName);
    }
    return false;
}
```

```java
public synchronized void increment() {
    logicalClock++;
}
```

Mutual exclusion techniques include:
- **Method-level synchronization**: All GameLogic methods are synchronized
- **Object locks**: Java's intrinsic locks protect shared resources
- **Thread-safe collections**: Safe access to shared game state
- **Atomic operations**: ClockSync uses atomic increments

## 7. Load Management

### Theory
Load management distributes work across computing resources to optimize performance. Strategies include:
- **Load balancing**: Distributing tasks evenly
- **Load sharing**: Moving tasks from overloaded to underloaded nodes
- **Admission control**: Limiting new tasks when load is high
- **Dynamic resource allocation**: Adjusting resources based on load

### Implementation Analysis
The game implements basic load management through its room-based architecture:

```java
// Room creation distributes players across separate game instances
public synchronized String createRoom(String playerName) {
    String roomId = "Room" + (rooms.size() + 1);
    rooms.put(roomId, new GameRoom(roomId, playerName));
    return roomId;
}
```

```java
// Client polling frequency adjustment
Thread.sleep(1000); // Polling interval
```

Load management techniques include:
- **Game room isolation**: Each room operates independently
- **Controlled polling frequency**: Clients poll at reasonable intervals
- **UI thread offloading**: Background threads for server communication
- **Lazy initialization**: Resources created only when needed

## 8. Multithreading

### Theory
Multithreading allows concurrent execution within a process, enabling:
- **Parallelism**: Simultaneous execution of tasks
- **Asynchronous processing**: Non-blocking operations
- **Responsive UIs**: Background processing without freezing interfaces
- **Resource utilization**: Maximizing CPU and I/O usage

### Implementation Analysis
The game heavily utilizes multithreading:

```java
// Background thread for game updates
private void startUpdateThread() {
    gameRunning.set(true);
    updateThread = new Thread(() -> {
        try {
            while (gameRunning.get()) {
                // Update game state
                // ...
                Thread.sleep(1000);
            }
        } finally {
            gameRunning.set(false);
        }
    });

    updateThread.setDaemon(true);
    updateThread.start();
}
```

```java
// Thread for monitoring player count
new Thread(() -> {
    try {
        while (server.getPlayers(roomId).size() < 2) {
            log("Waiting for at least 2 players to join...");
            Thread.sleep(3000);
        }
        
        SwingUtilities.invokeLater(() -> {
            log("Minimum players reached! Game starting...");
            try {
                server.startGame(roomId);
            } catch (Exception e) {
                log("Error starting game: " + e.getMessage());
            }
        });
    } catch (Exception e) {
        log("Error waiting for players: " + e.getMessage());
    }
}).start();
```

Multithreading techniques include:
- **Background update threads**: Non-blocking server communication
- **Thread synchronization**: Safe shared state access
- **UI thread management**: SwingUtilities.invokeLater for UI updates
- **Thread lifecycle management**: Daemon threads, interruption handling
- **Thread-safe state updates**: AtomicBoolean for thread control

## Conclusion

The number guessing game successfully implements the required distributed computing concepts:

1. **Communication**: Effective RMI-based client-server communication
2. **RPC/RMI**: Complete implementation of Java RMI for remote method calls
3. **Clock Synchronization**: Simple logical clock for event ordering
4. **Multiple Nodes**: Independent client nodes with local state
5. **Deadlock Management**: Prevention through synchronized methods and careful resource handling
6. **Mutual Exclusion**: Synchronized access to shared server-side resources
7. **Load Management**: Room-based architecture for work distribution
8. **Multithreading**: Extensive use of threads for responsive operation

---

- Commands to run the game:
    - Compile:`javac -d out src/server/*.java src/client/*.java`
    - Server:`.\run\start_server.bat`
    - Client:`.\run\start_client.bat`

---