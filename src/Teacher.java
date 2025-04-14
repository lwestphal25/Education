
import java.awt.*;
import java.awt.image.*;
import java.util.Random;
import javax.imageio.ImageIO;
import java.io.IOException;

public class Teacher {
    private BufferedImage frontImage, backImage, halfwayImage;
    private State currentState = State.BACK;
    private int stateTimer = 0;
    private final int TICK_RATE = 100; // ms per tick (matches Timer)
    private Random rand = new Random();

    private final int MAX_FRONT_TIME = 3000; // 3 seconds
    private final int HALF_BUFFER_TIME = 1000; // 1 second buffer
    private int switchDelay = 2000 + rand.nextInt(2000); // 2-4s before turning again

    private enum State {
        BACK, HALF, FRONT
    }

    public Teacher() {
        try {
            frontImage = ImageIO.read(getClass().getResource("Teacher_Front.png"));
            backImage = ImageIO.read(getClass().getResource("Teacher_Back.png"));
            halfwayImage = ImageIO.read(getClass().getResource("Teacher_Side.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update() {
        stateTimer += TICK_RATE;

        switch (currentState) {
            case BACK:
                if (stateTimer >= switchDelay) {
                    currentState = State.HALF;
                    stateTimer = 0;
                }
                break;
            case HALF:
                if (stateTimer >= HALF_BUFFER_TIME) {
                    currentState = State.FRONT;
                    stateTimer = 0;
                }
                break;
            case FRONT:
                if (stateTimer >= MAX_FRONT_TIME) {
                    currentState = State.BACK;
                    switchDelay = 2000 + rand.nextInt(2000);
                    stateTimer = 0;
                }
                break;
        }
    }

    public void draw(Graphics g) {
        int width = 150;
        int height = 150;
        int x = 600;
        int y = 150;

        switch (currentState) {
            case BACK:
                g.drawImage(backImage, x, y, width, height, null);
                break;
            case HALF:
                g.drawImage(halfwayImage, x, y, width, height, null);
                break;
            case FRONT:
                g.drawImage(frontImage, x, y, width, height, null);
                break;
        }
    }

    public boolean isWatching() {
        return currentState == State.FRONT;
    }
}
