import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ImgFileInfo {
    private String name;
    private long size;
    private int width;
    private int height;

    public ImgFileInfo(File file){
        BufferedImage img;

        name = file.getName();
        size = file.length();
        try {
            img = ImageIO.read(file);
            width = img.getWidth();
            height = img.getHeight();
        } catch (Exception e) { e.printStackTrace(); }

    }
    public ImgFileInfo(String name, long size, int width, int height) {
        this.name = name;
        this.size = size;
        this.width = width;
        this.height = height;
    }

    public String getName() { return name; }
    public long getSize() { return size; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
