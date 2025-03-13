package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIClientInterface extends Remote {
    void updateGameState(String message) throws RemoteException;
}