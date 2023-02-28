import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JPicture extends JPanel {
    private BufferedImage picture;

    public JPicture(BufferedImage picture) {
        super();
        this.picture = picture;
    }
    public JPicture() {
        this(null);
    }

    public void setPicture(BufferedImage picture) {
        this.picture = picture;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (picture != null)
            g.drawImage(picture, 0, 0, getWidth(), getHeight(), this);
    }
}
