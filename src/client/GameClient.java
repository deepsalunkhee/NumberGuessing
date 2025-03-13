package client;

import server.RMIServerInterface;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class GameClient {
    private RMIServerInterface server;
    private String playerName;
    private String roomId;

    public GameClient(String host) {
        try {
            Registry registry = LocateRegistry.getRegistry(host, Constants.RMI_PORT);
            server = (RMIServerInterface) registry.lookup(Constants.RMI_ID);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your name: ");
        playerName = scanner.nextLine();
        
        while (true) {
            System.out.println("\n1. Create Room\n2. Join Room\n3. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    createRoom();
                    break;
                case 2:
                    joinRoom(scanner);
                    break;
                case 3:
                    System.out.println("Exiting game...");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void createRoom() {
        try {
            roomId = server.createRoom(playerName);
            System.out.println("Room created: " + roomId);
            waitForPlayers();
            playGame();
        } catch (Exception e) {
            System.err.println("Error creating room: " + e.getMessage());
        }
    }

    private void joinRoom(Scanner scanner) {
        try {
            List<String> rooms = server.getAvailableRooms();
            if (rooms.isEmpty()) {
                System.out.println("No rooms available. Create one instead.");
                return;
            }
            
            System.out.println("Available rooms: " + rooms);
            System.out.print("Enter Room ID: ");
            String selectedRoom = scanner.nextLine();
            
            if (server.joinRoom(selectedRoom, playerName)) {
                roomId = selectedRoom;
                System.out.println("Joined room: " + roomId);
                waitForPlayers();
                playGame();
            } else {
                System.out.println("Failed to join room. It may be full or game has started.");
            }
        } catch (Exception e) {
            System.err.println("Error joining room: " + e.getMessage());
        }
    }

    private void waitForPlayers() {
        try {
            while (server.getPlayers(roomId).size() < 2) {
                System.out.println("Waiting for at least 2 players to join...");
                Thread.sleep(3000);
            }
            System.out.println("Minimum players reached! Game starting...");
            server.startGame(roomId);
        } catch (Exception e) {
            System.err.println("Error waiting for players: " + e.getMessage());
        }
    }

    private void playGame() {
        Scanner scanner = new Scanner(System.in);
        try {
            while (!server.isGameStarted(roomId)) {
                System.out.println("Waiting for the game to start...");
                Thread.sleep(2000);
            }
            
            System.out.println("Game started!");
            
            while (true) {
                // Guesses so far
                List<String> allGuesses = server.getGuesses(roomId);
                System.out.println("Recent guesses: " + allGuesses);

                boolean status = server.getStatus(roomId);
                if (status) {
                    System.out.println("Game Over! Winner is: " + server.getWinner(roomId));
                    break;
                }
                System.out.print("Enter your guess (1-25): ");
                int guess = scanner.nextInt();
                
                boolean correct = server.submitGuess(roomId, playerName, guess);
               
                allGuesses = server.getGuesses(roomId);
                System.out.println("Recent guesses: " + allGuesses);
                
                if (correct) {
                    System.out.println("Congratulations! You guessed correctly.");
                    break;
                } else {
                    System.out.println("Incorrect guess. Try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error playing game: " + e);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        GameClient client = new GameClient(host);
        client.start();
    }
}
