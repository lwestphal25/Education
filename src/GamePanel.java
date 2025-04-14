import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Teacher teacher;
    private Student student;
    private boolean isSpacePressed = false;
    private int score = 0;
    private boolean gameOver = false;
    private BufferedImage background;

    public GamePanel() {
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
        if (!gameOver) {
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
        g.drawImage(background, 0, 0, null);

        teacher.draw(g);
        student.draw(g);


        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 40);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("CAUGHT!", 300, 300);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isSpacePressed = true;
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            isSpacePressed = false;
        }
    }

    public void keyTyped(KeyEvent e) {}
}
