import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class PictureItem {
    private final File file;
    private BufferedImage picture;
    private int width;
    private int height;

    public PictureItem(File file){
        this.file = file;
        try {
            picture = ImageIO.read(file);
            width = picture.getWidth();
            height = picture.getHeight();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public File getFile() { return file; }
    public BufferedImage getPicture() { return picture; }
    public String getName() { return file.getName(); }
    public String getPath() { return file.getAbsolutePath(); }
    public long getSize() { return file.length(); }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
