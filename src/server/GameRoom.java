package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameRoom {
    private String roomId;
    private String creatorName;
    private List<String> players;
    private boolean gameStarted;
    private int targetNumber;
    private Random random;
    private List<String> guesses;
    private boolean status;
    private String winner;


    public GameRoom(String roomId, String creatorName) {
        this.roomId = roomId;
        this.creatorName = creatorName;
        this.players = new ArrayList<>();
        this.players.add(creatorName);
        this.gameStarted = false;
        this.random = new Random();
        this.guesses = new ArrayList<>();
        this.status = false;
        this.winner = "";
        
    }

    public String getRoomId() {
        return roomId;
    }

    public List<String> getPlayers() {
        return players;
    }

    public boolean addPlayer(String playerName) {
        if (!gameStarted) {
            players.add(playerName);
            return true;
        }
        return false;
    }

    public void startGame() {
        this.gameStarted = true;
        this.targetNumber = random.nextInt(25) + 1;
        System.out.println("Game started in room " + roomId + " with target number " + targetNumber);
    }

    public boolean isGameStarted() {
        return gameStarted;
    }

    public int getTargetNumber() {
        return targetNumber;
    }

    public boolean submitGuess(String playerName, int guess) {
        if (gameStarted) {
            String guessStr = playerName + " guessed " + guess;
            System.out.println(guessStr);
            guesses.add(guessStr);

            if(guess == targetNumber){
                status = true;
                winner = playerName;
            }
            return guess == targetNumber;
        }
        return false;
    }



    public List<String> getGuesses() {
        return guesses;
    }

    public boolean getStatus() {
        return status;
    }

    public String getWinner() {
        return winner;
    }

    
}