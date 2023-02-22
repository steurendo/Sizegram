import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class PictureItem {
    private final File file;
    private int width;
    private int height;

    public PictureItem(File file){
        BufferedImage img;

        this.file = file;
        try {
            img = ImageIO.read(file);
            width = img.getWidth();
            height = img.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public File getFile() { return file; }
    public String getName() { return file.getName(); }
    public String getPath() { return file.getAbsolutePath(); }
    public long getSize() { return file.length(); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
