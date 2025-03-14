package client;

import server.RMIServerInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient extends JFrame {
    private RMIServerInterface server;
    private String playerName;
    private String roomId;
    
    // UI Components
    private JTextField nameField;
    private JButton createRoomButton;
    private JButton joinRoomButton;
    private JComboBox<String> roomsComboBox;
    private JTextArea gameLogArea;
    private JTextField guessField;
    private JButton submitGuessButton;
    private JPanel cardPanel;
    private CardLayout cardLayout;
    
    // Threading control
    private Thread updateThread;
    private AtomicBoolean gameRunning = new AtomicBoolean(false);
    
    // Store local guesses to track changes
    private List<String> localGuesses = new ArrayList<>();
    private boolean gameHasStarted = false;

    public GameClient(String host) {
        super("Guessing Game Client");
        
        try {
            Registry registry = LocateRegistry.getRegistry(host, Constants.RMI_PORT);
            server = (RMIServerInterface) registry.lookup(Constants.RMI_ID);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Failed to connect to server: " + e.getMessage(),
                "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        
        initializeUI();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setVisible(true);
        
        // Handle window closing to clean up threads
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                stopUpdateThread();
                System.exit(0);
            }
        });
    }
    
    private void initializeUI() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        
        // Login panel
        JPanel loginPanel = createLoginPanel();
        
        // Lobby panel
        JPanel lobbyPanel = createLobbyPanel();
        
        // Game panel
        JPanel gamePanel = createGamePanel();
        
        // Add panels to card layout
        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(lobbyPanel, "LOBBY");
        cardPanel.add(gamePanel, "GAME");
        
        add(cardPanel);
        
        // Start with login panel
        cardLayout.show(cardPanel, "LOGIN");
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        JLabel nameLabel = new JLabel("Enter your name:");
        nameField = new JTextField(20);
        JButton loginButton = new JButton("Enter Game Lobby");
        
        loginButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (playerName.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please enter a name", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            cardLayout.show(cardPanel, "LOBBY");
            refreshAvailableRooms();
        });
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(nameField, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.insets = new Insets(20, 5, 5, 5);
        panel.add(loginButton, gbc);
        
        return panel;
    }
    
    private JPanel createLobbyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        createRoomButton = new JButton("Create New Room");
        topPanel.add(createRoomButton);
        
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        
        roomsComboBox = new JComboBox<>();
        JButton refreshButton = new JButton("Refresh");
        joinRoomButton = new JButton("Join Selected Room");
        
        JPanel roomControlPanel = new JPanel(new FlowLayout());
        roomControlPanel.add(roomsComboBox);
        roomControlPanel.add(refreshButton);
        roomControlPanel.add(joinRoomButton);
        
        centerPanel.add(roomControlPanel, BorderLayout.NORTH);
        
        JTextArea roomInfoArea = new JTextArea(10, 40);
        roomInfoArea.setEditable(false);
        roomInfoArea.setText("Select a room to see details...");
        JScrollPane scrollPane = new JScrollPane(roomInfoArea);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        // Add event listeners
        createRoomButton.addActionListener(e -> createRoom());
        refreshButton.addActionListener(e -> refreshAvailableRooms());
        joinRoomButton.addActionListener(e -> joinSelectedRoom());
        
        return panel;
    }
    
    private JPanel createGamePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel roomLabel = new JLabel("Room: Waiting...");
        JLabel statusLabel = new JLabel("Status: Waiting for players...");
        topPanel.add(roomLabel, BorderLayout.WEST);
        topPanel.add(statusLabel, BorderLayout.EAST);
        
        gameLogArea = new JTextArea(15, 40);
        gameLogArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(gameLogArea);
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        JLabel guessLabel = new JLabel("Your Guess (1-25):");
        guessField = new JTextField(5);
        submitGuessButton = new JButton("Submit Guess");
        submitGuessButton.setEnabled(false); // Disabled until game starts
        inputPanel.add(guessLabel);
        inputPanel.add(guessField);
        inputPanel.add(submitGuessButton);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        // Add event listener for submit button
        submitGuessButton.addActionListener(e -> submitGuess());
        
        return panel;
    }
    
    private void refreshAvailableRooms() {
        try {
            List<String> rooms = server.getAvailableRooms();
            roomsComboBox.removeAllItems();
            
            if (rooms.isEmpty()) {
                joinRoomButton.setEnabled(false);
                roomsComboBox.addItem("No rooms available");
            } else {
                joinRoomButton.setEnabled(true);
                for (String room : rooms) {
                    roomsComboBox.addItem(room);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error getting available rooms: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createRoom() {
        try {
            roomId = server.createRoom(playerName);
            log("Room created: " + roomId);
            cardLayout.show(cardPanel, "GAME");
            
            // Reset game state
            gameHasStarted = false;
            localGuesses.clear();
            
            // Start the update thread
            startUpdateThread();
            
            // Wait for more players
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
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error creating room: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void joinSelectedRoom() {
        try {
            String selectedRoom = (String) roomsComboBox.getSelectedItem();
            if (selectedRoom == null || selectedRoom.equals("No rooms available")) {
                return;
            }
            
            if (server.joinRoom(selectedRoom, playerName)) {
                roomId = selectedRoom;
                log("Joined room: " + roomId);
                cardLayout.show(cardPanel, "GAME");
                
                // Reset game state
                gameHasStarted = false;
                localGuesses.clear();
                
                // Start the update thread
                startUpdateThread();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to join room. It may be full or game has started.",
                    "Join Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error joining room: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void submitGuess() {
        try {
            String guessText = guessField.getText().trim();
            if (guessText.isEmpty()) {
                return;
            }
            
            int guess;
            try {
                guess = Integer.parseInt(guessText);
                if (guess < 1 || guess > 25) {
                    JOptionPane.showMessageDialog(this,
                        "Please enter a number between 1 and 25",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid number",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean correct = server.submitGuess(roomId, playerName, guess);
            guessField.setText("");
            
            if (correct) {
                log("Congratulations! You guessed correctly.");
            } else {
                log("Incorrect guess. Try again.");
            }
        } catch (Exception e) {
            log("Error submitting guess: " + e.getMessage());
        }
    }
    
    private void startUpdateThread() {
        gameRunning.set(true);
        updateThread = new Thread(() -> {
            try {
                while (gameRunning.get()) {
                    try {
                        // Check game status 
                        boolean gameStarted = server.isGameStarted(roomId);
                        boolean gameOver = server.getStatus(roomId);
                        
                        // Track when the game starts
                        boolean gameJustStarted = !gameHasStarted && gameStarted;
                        if (gameJustStarted) {
                            gameHasStarted = true;
                            SwingUtilities.invokeLater(() -> {
                                log("Game has officially started! You can now make guesses.");
                                submitGuessButton.setEnabled(true);
                            });
                        }
                        
                        // Only process guesses if the game has started
                        if (gameHasStarted) {
                            // Get all guesses
                            List<String> serverGuesses = server.getGuesses(roomId);
                            
                            // Find new guesses
                            List<String> newGuesses = new ArrayList<>();
                            if (serverGuesses.size() > localGuesses.size()) {
                                for (int i = localGuesses.size(); i < serverGuesses.size(); i++) {
                                    newGuesses.add(serverGuesses.get(i));
                                }
                                
                                // Update local guesses
                                localGuesses = new ArrayList<>(serverGuesses);
                                
                                // Log only the new guesses
                                if (!newGuesses.isEmpty()) {
                                    final List<String> guessesToShow = new ArrayList<>(newGuesses);
                                    SwingUtilities.invokeLater(() -> {
                                        for (String guess : guessesToShow) {
                                            log("New guess: " + guess);
                                        }
                                    });
                                }
                            }
                        }
                        
                        // If game is over, check for winner
                        if (gameOver) {
                            String winner = server.getWinner(roomId);
                            SwingUtilities.invokeLater(() -> {
                                log("Game Over! Winner is: " + winner);
                                submitGuessButton.setEnabled(false);
                            });
                            break;
                        }
                        
                        // Sleep for a short period before next update
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        log("Update thread error: " + e.getMessage());
                        break;
                    }
                }
            } finally {
                gameRunning.set(false);
            }
        });
        
        updateThread.setDaemon(true);
        updateThread.start();
    }
    
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
    
    private void log(String message) {
        gameLogArea.append(message + "\n");
        // Auto-scroll to bottom
        gameLogArea.setCaretPosition(gameLogArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";
        
        SwingUtilities.invokeLater(() -> new GameClient(host));
    }
}