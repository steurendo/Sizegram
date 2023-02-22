import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Model {
    public static String[] ALLOWED_EXTENSIONS = {"png", "jpg", "gif"};

    private ModelConstants state;
    private List<File> images;
    private boolean adjustHorizontal;

    public Model(){
        images = new ArrayList<File>().stream().toList();
        adjustHorizontal = true;
        state = ModelConstants.STATE_START;
        images = null;
    }

    public boolean isAdjustHorizontal() { return adjustHorizontal; }
    public ModelConstants getState() { return state; }
    public String getWorkingDirectoryPath(){
        if (images.isEmpty())
            return "";

        return images.get(0).getParentFile().getAbsolutePath();
    }
    public void setAdjustHorizontal(boolean adjustHorizontal) { this.adjustHorizontal = adjustHorizontal; }

    public ModelConstants obtainFilesFromDirectory(File directory){
        File[] files;

        if (!directory.isDirectory())
            return ModelConstants.ERR_NOT_A_DIRECTORY;
        files = directory.listFiles();
        if (files == null)
            return ModelConstants.RET_EMPTY_DIRECTORY;
        images = Arrays.stream(files)
                .filter(f -> !f.isDirectory())
                .filter(f -> isEndWith(f.getName().toLowerCase(), ALLOWED_EXTENSIONS))
                .toList();
        if (images.isEmpty()){
            state = ModelConstants.STATE_START;
            return ModelConstants.RET_EMPTY_DIRECTORY;
        }

        state = ModelConstants.STATE_READY;
        if (images.size() > 10)
            return ModelConstants.WRN_COUNT_EXCEEDS;

        return ModelConstants.RET_GOOD;
    }

    public ModelConstants adjustAspectRatio(){
        images.forEach(e -> {
            System.out.println(e.getAbsolutePath());
            BufferedImage img, out;
            int n, d, expectedW, expectedH, addW, addH, x, y;
            Graphics g2d;

            n = adjustHorizontal ? 4 : 5;
            d = adjustHorizontal ? 5 : 4;
            try {
                img = ImageIO.read(e);
                expectedW = d * img.getHeight() / n;
                expectedH = n * img.getWidth() / d;
                if (expectedH > img.getHeight()) { //AGGIUNGO PIXEL IN ALTEZZA
                    addW = 0;
                    addH = (expectedH - img.getHeight()) / 2;
                    expectedW = img.getWidth();
                    expectedH = 2 * addH + img.getHeight();
                }
                else {                             //AGGIUNGO PIXEL IN LUNGHEZZA
                    addW = (expectedW - img.getWidth()) / 2;
                    addH = 0;
                    expectedW = 2 * addW + img.getWidth();
                    expectedH = img.getHeight();
                }
                out = new BufferedImage(expectedW, expectedH, BufferedImage.TYPE_INT_RGB);
                g2d = out.createGraphics();
                g2d.setColor(new Color(0xFFFFFFFF));
                g2d.fillRect(0, 0, expectedW, expectedH);
                g2d.drawImage(img, addW, addH, null);
                ImageIO.write(out, "jpg", e);
            } catch (Exception ex){ throw new RuntimeException(ex); }
        });
        images = new ArrayList<File>().stream().toList();

        state = ModelConstants.STATE_START;
        return ModelConstants.RET_GOOD;
    }
    public ImgFileInfo getImgInfo(int index){
        return new ImgFileInfo(images.get(index));
    }
    public List<ImgFileInfo> getAllImgsInfo(){
        return images.stream()
                .map(ImgFileInfo::new)
                .toList();
    }

    private static boolean isEndWith(String file, String[] fileExtensions){
        for (String fileExtension : fileExtensions) {
            if (file.endsWith(fileExtension)) {
                return true;
            }
        }
        return false;
    }
    public void printState(){
        System.out.printf("Current state: %s\nAdjust set to %s\n", state.toString(), (adjustHorizontal ? "horizontal" : "vertical"));
        if (images.isEmpty())
            System.out.println("No folder selected\n");
        else {
            System.out.println("Selected folder: " + getWorkingDirectoryPath());
            images.forEach(f -> System.out.println("\t-" + f.getName()));
        }
    }
}
