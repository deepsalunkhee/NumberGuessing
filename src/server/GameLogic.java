package server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameLogic {
    private Map<String, GameRoom> rooms;

    public GameLogic() {
        this.rooms = new HashMap<>();
    }

    public synchronized String createRoom(String playerName) {
        String roomId = "Room" + (rooms.size() + 1);
        rooms.put(roomId, new GameRoom(roomId, playerName));
        return roomId;
    }

    public synchronized List<String> getAvailableRooms() {
        return new ArrayList<>(rooms.keySet());
    }

    public synchronized boolean joinRoom(String roomId, String playerName) {
        if (rooms.containsKey(roomId)) {
            return rooms.get(roomId).addPlayer(playerName);
        }
        return false;
    }

    public synchronized void startGame(String roomId) {
        if (rooms.containsKey(roomId)) {
            rooms.get(roomId).startGame();
        }
    }

    public synchronized boolean isGameStarted(String roomId) {
        return rooms.containsKey(roomId) && rooms.get(roomId).isGameStarted();
    }

    public synchronized int getTargetNumber(String roomId) {
        return rooms.containsKey(roomId) ? rooms.get(roomId).getTargetNumber() : -1;
    }

    public synchronized List<String> getPlayers(String roomId) {
        return rooms.containsKey(roomId) ? rooms.get(roomId).getPlayers() : new ArrayList<>();
    }

    public synchronized boolean submitGuess(String roomId, String playerName, int guess) {
        if (rooms.containsKey(roomId)) {
            return rooms.get(roomId).submitGuess(playerName, guess);
        }
        return false;
    }

    public synchronized List<String> getGuesses(String roomId) {
        return rooms.containsKey(roomId) ? rooms.get(roomId).getGuesses() : new ArrayList<>();
    }

    public synchronized String getWinner(String roomId) {
        return rooms.containsKey(roomId) ? rooms.get(roomId).getWinner() : "";
    }

    public synchronized boolean getStatus(String roomId) {
        return rooms.containsKey(roomId) ? rooms.get(roomId).getStatus() : false;
      
    }

    
}