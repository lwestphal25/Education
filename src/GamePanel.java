import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.util.Scanner;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Teacher teacher;
    private Student student;
    private boolean isSpacePressed = false;
    private int score = 0;
    private boolean gameOver = false;
    private BufferedImage background;

    private boolean chatMode = false;
    private StringBuilder chatInput = new StringBuilder();
    private String chatGPTResponse = "";

    private final String OPENAI_API_KEY = ""; // <<< PON TU API KEY AQUÍ

    public GamePanel() {

        setFocusable(true);  // Ensure that the panel is focusable and can receive key events
        requestFocusInWindow();  // Request focus immediately after creation

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        addKeyListener(this);

        teacher = new Teacher();
        student = new Student();
        timer = new Timer(100, this); // update every 100ms
        timer.start();

        try {
            background = ImageIO.read(getClass().getResource("Classroom.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !chatMode) {
            teacher.update();

            if (isSpacePressed) {
                student.setUsingPhone(true);
                if (teacher.isWatching()) {
                    gameOver = true;
                } else {
                    score++;
                }
            } else {
                student.setUsingPhone(false);
            }

            repaint();
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, null); // draw background

        teacher.draw(g);
        student.draw(g);

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 40);

        if (gameOver) {
            // draw game over stuff
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Game Over", getWidth()/2 - 150, getHeight()/2);
            g.setFont(new Font("Arial", Font.PLAIN, 24));
            g.drawString("Press ENTER to restart", getWidth()/2 - 130, getHeight()/2 + 50);
        }

        // 👇👇👇 ADD THIS 👇👇👇

        if (chatMode) {
            // Draw a semi-transparent panel at the bottom for the chat box
            g.setColor(new Color(0, 0, 0, 150)); // black, semi-transparent
            g.fillRect(10, getHeight() - 150, getWidth() - 20, 140);

            // Draw what the player is typing
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("You: " + chatInput.toString(), 20, getHeight() - 110);

            // Draw the last ChatGPT response
            if (!chatGPTResponse.isEmpty()) {
                g.setColor(Color.CYAN);
                g.setFont(new Font("Arial", Font.PLAIN, 18));
                g.drawString("ChatGPT: " + chatGPTResponse, 20, getHeight() - 60);
            }
        }
    }




    private void drawStringMultiLine(Graphics g, String text, int x, int y) {
        for (String line : text.split("\n")) {
            g.drawString(line, x, y += g.getFontMetrics().getHeight());
        }
    }

    public void keyPressed(KeyEvent e) {
        // Toggle chat mode with the "1" key
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            chatMode = !chatMode;  // Toggle between chat mode and game mode
            repaint(); // Force a repaint to update the display when toggling modes
        }

        if (chatMode) {
            // Chat mode logic
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                if (!chatInput.isEmpty()) {
                    String question = chatInput.toString();
                    chatInput.setLength(0); // Clear input after sending
                    askChatGPT(question); // Send the question to ChatGPT
                } else {
                    chatMode = false; // Exit chat mode if no input is provided
                }
            } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (chatInput.length() > 0) {
                    chatInput.setLength(chatInput.length() - 1); // Remove last character if backspace is pressed
                }
            } else {
                char c = e.getKeyChar();
                // Allow letters, digits, spaces, and symbols to be typed in the input field
                if (Character.isLetterOrDigit(c) || Character.isSpaceChar(c) || isSymbol(c)) {
                    chatInput.append(c); // Append the typed character to the chat input
                }
            }
        } else {
            // Game mode logic
            if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                isSpacePressed = true; // Handle spacebar in regular game mode
            }
        }
    }


    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isSpacePressed = false;
        }
    }

    public void keyTyped(KeyEvent e) {}

    private boolean isSymbol(char c) {
        return "!@#$%^&*()_+-=[]{}|;:'\",.<>?/".indexOf(c) >= 0;
    }

    private void askChatGPT(String question) {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.openai.com/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + OPENAI_API_KEY);
                conn.setDoOutput(true);

                String jsonInputString = "{\n" +
                        "\"model\": \"gpt-3.5-turbo\",\n" +
                        "\"messages\": [{\"role\": \"user\", \"content\": \"" + question + "\"}]\n" +
                        "}";

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                Scanner scanner = new Scanner(conn.getInputStream());
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                scanner.close();

                String fullResponse = response.toString();
                String content = extractContent(fullResponse);
                chatGPTResponse = content;

                repaint();
            } catch (Exception ex) {
                chatGPTResponse = "Error: " + ex.getMessage();
                repaint();
            }
        }).start();
    }

    private String extractContent(String json) {
        int index = json.indexOf("\"content\":\"");
        if (index == -1) return "No response.";
        int start = index + 10;
        int end = json.indexOf("\"", start);
        if (end == -1) return "No response.";
        return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
    }
}
