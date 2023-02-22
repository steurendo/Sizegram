import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Model {
    public static String[] ALLOWED_EXTENSIONS = { "png", "jpg", "gif" };
    private static final float RATIO_IG_HORIZONTAL = 5f / 4;
    private static final float RATIO_IG_VERTICAL = 4f / 5;
    private final View view;
    private File workingDirectory;
    private List<PictureItem> pictureItems;
    private int selectedItem;
    private OutputRatio outputRatio;
    private boolean usingCustomRatio;

    public Model(View view) {
        this.view = view;
        outputRatio = OutputRatio.HORIZONTAL;
        usingCustomRatio = false;
        workingDirectory = null;
        pictureItems = null;
        selectedItem = -1;
    }

    public OutputRatio getOutputRatio() { return outputRatio; }
    public void setOutputRatio(OutputRatio outputRatio) {
        this.outputRatio = outputRatio;
    }
    public void toggleCustomRatio() {
        usingCustomRatio = !usingCustomRatio;
    }
    public boolean isUsingCustomRatio() {
        return usingCustomRatio;
    }
    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }
    public PictureItem getSelectedItem() {
        if (selectedItem == -1)
            return null;
        return pictureItems.get(selectedItem);
    }

    public String getWorkingDirectoryPath() {
        if (workingDirectory == null)
            return "";

        return workingDirectory.getAbsolutePath();
    }

    public ModelMessage loadFilesInFolder(File folder) {
        File[] files;

        files = folder.listFiles();
        if (files == null)
            return ModelMessage.ERR_NOT_A_DIRECTORY;
        pictureItems = Arrays.stream(files)
                .filter(f -> !f.isDirectory())
                .filter(f -> isEndWith(f.getName().toLowerCase(), ALLOWED_EXTENSIONS))
                .map(PictureItem::new)
                .toList();
        if (pictureItems.isEmpty())
            return ModelMessage.ERR_EMPTY_DIRECTORY;

        workingDirectory = folder;
        if (pictureItems.size() > 10)
            return ModelMessage.WRN_COUNT_EXCEEDS;

        return ModelMessage.RET_GOOD;
    }
    public void adjustRatio() {
        int prog, count, percent;
        BufferedImage img, out;
        float ratio, outRatio;
        int expectedW, expectedH, addW, addH;
        Graphics g2d;

        prog = 0;
        count = pictureItems.size();
        if (usingCustomRatio) {
            PictureItem selectedPicture = pictureItems.get(selectedItem);
            outRatio = ((float) selectedPicture.getWidth()) / selectedPicture.getHeight();
        }
        else if (outputRatio == OutputRatio.HORIZONTAL)
            outRatio = RATIO_IG_HORIZONTAL;
        else
            outRatio = RATIO_IG_VERTICAL;
        for (PictureItem picture : pictureItems) {
            try {
                img = ImageIO.read(picture.getFile());
                ratio = ((float) img.getWidth()) / img.getHeight();
                if (ratio > outRatio) { //AGGIUNGO PIXEL IN ALTEZZA
                    expectedW = img.getWidth();
                    expectedH = (int) (expectedW / outRatio);
                    addW = 0;
                    addH = (expectedH - img.getHeight()) / 2;
                } else {                             //AGGIUNGO PIXEL IN LUNGHEZZA
                    expectedH = img.getHeight();
                    expectedW = (int) (expectedH * outRatio);
                    addH = 0;
                    addW = (expectedW - img.getWidth()) / 2;
                }
                out = new BufferedImage(expectedW, expectedH, BufferedImage.TYPE_INT_RGB);
                g2d = out.createGraphics();
                g2d.setColor(new Color(0xFFFFFFFF));
                g2d.fillRect(0, 0, expectedW, expectedH);
                g2d.drawImage(img, addW, addH, null);
                ImageIO.write(out, "jpg", picture.getFile());
                prog++;
                percent = (int)(100 * ((float)prog) / count);
                view.notifyProgress(percent);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        pictureItems = null;
        selectedItem = -1;
        view.notifyProgress(-1);
    }

    public List<PictureItem> getPictureItems() {
        return pictureItems;
    }

    private static boolean isEndWith(String file, String[] fileExtensions) {
        for (String fileExtension : fileExtensions) {
            if (file.endsWith(fileExtension)) {
                return true;
            }
        }
        return false;
    }

    public void printState() {
        System.out.printf("Adjust set to %s\n", outputRatio);
        if (pictureItems.isEmpty())
            System.out.println("No folder selected\n");
        else {
            System.out.println("Selected folder: " + getWorkingDirectoryPath());
            pictureItems.forEach(f -> System.out.println("\t-" + f.getName()));
        }
    }
}
