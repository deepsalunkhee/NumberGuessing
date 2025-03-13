package client;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;

public class ClientGUI extends JFrame {
    private JTextField guessField;
    private JTextArea gameLog;
    private JButton submitGuessButton;
    private RMIClientInterface client;

    public ClientGUI(RMIClientInterface client) {
        this.client = client;
        setTitle("Number Guessing Game");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        gameLog = new JTextArea();
        gameLog.setEditable(false);
        add(new JScrollPane(gameLog), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        guessField = new JTextField(10);
        submitGuessButton = new JButton("Submit Guess");
        bottomPanel.add(guessField);
        bottomPanel.add(submitGuessButton);
        add(bottomPanel, BorderLayout.SOUTH);

        submitGuessButton.addActionListener(e -> {
            try {
                int guess = Integer.parseInt(guessField.getText());
                client.updateGameState("Guess submitted: " + guess);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateGameLog(String message) {
        gameLog.append(message + "\n");
    }
}