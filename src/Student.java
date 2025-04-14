import java.awt.*;
import java.awt.image.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Student {
    private BufferedImage idleImage;
    private BufferedImage phoneImage;
    private boolean usingPhone = false;

    public Student() {
        try {
            idleImage = ImageIO.read(getClass().getResource("Student.png"));
            phoneImage = ImageIO.read(getClass().getResource("Student_Phone.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setUsingPhone(boolean val) {
        usingPhone = val;
    }

    public void draw(Graphics g) {
        int width = 150;
        int height = 200;
        int x = (800 - width) / 2;
        int y = 600 - height - 50; // bottom with padding

        if (usingPhone) {
            g.drawImage(phoneImage, x, y, width, height, null);
        } else {
            g.drawImage(idleImage, x, y, width, height, null);
        }
    }
}
