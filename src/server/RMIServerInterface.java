package server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RMIServerInterface extends Remote {
    String createRoom(String playerName) throws RemoteException;
    List<String> getAvailableRooms() throws RemoteException;
    boolean joinRoom(String roomId, String playerName) throws RemoteException;
    boolean submitGuess(String roomId, String playerName, int guess) throws RemoteException;
    List<String> getPlayers(String roomId) throws RemoteException;
    boolean isGameStarted(String roomId) throws RemoteException;
    int getTargetNumber(String roomId) throws RemoteException;
    void startGame(String roomId) throws RemoteException;
    List<String> getGuesses(String roomId) throws RemoteException;
    String getWinner(String roomId) throws RemoteException;
    boolean getStatus(String roomId) throws RemoteException;

}