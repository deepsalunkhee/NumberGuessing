package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class GameServer implements RMIServerInterface {
    private GameLogic gameLogic;
    private ClockSync clockSync;


    public GameServer() {
        this.gameLogic = new GameLogic();
        this.clockSync = new ClockSync();
    }

    public static void main(String[] args) {
        try {
            GameServer server = new GameServer();
            RMIServerInterface stub = (RMIServerInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.createRegistry(Constants.RMI_PORT);
            registry.rebind(Constants.RMI_ID, stub);
            System.out.println("Server is running...");
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public String createRoom(String playerName) throws RemoteException {
        clockSync.increment();
        return gameLogic.createRoom(playerName);
    }

    @Override
    public List<String> getAvailableRooms() throws RemoteException {
        clockSync.increment();
        return gameLogic.getAvailableRooms();
    }

    @Override
    public boolean joinRoom(String roomId, String playerName) throws RemoteException {
        clockSync.increment();
        return gameLogic.joinRoom(roomId, playerName);
    }

    @Override
    public boolean submitGuess(String roomId, String playerName, int guess) throws RemoteException {
        clockSync.increment();
        
        return gameLogic.submitGuess(roomId, playerName, guess);

    }

    @Override
    public List<String> getPlayers(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.getPlayers(roomId);
    }

    @Override
    public boolean isGameStarted(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.isGameStarted(roomId);
    }

    @Override
    public int getTargetNumber(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.getTargetNumber(roomId);
    }

    @Override
    public void startGame(String roomId) throws RemoteException {
        clockSync.increment();
        gameLogic.startGame(roomId);
    }

    @Override
    public List<String> getGuesses(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.getGuesses(roomId);
    }

    @Override
    public String getWinner(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.getWinner(roomId);
    }

    @Override
    public boolean getStatus(String roomId) throws RemoteException {
        clockSync.increment();
        return gameLogic.getStatus(roomId);
    }
}