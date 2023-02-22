import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

public class View extends JFrame {
    private JButton adjustButton;
    private JButton selectFolderButton;
    private JTextField textField1;
    private JPanel mainPanel;
    private JPanel panelRadios;
    private JRadioButton horizontalRadioButton;
    private JRadioButton verticalRadioButton;
    private JScrollPane listImages;
    private JLabel labelWidth;
    private JLabel labelHeight;
    private JLabel labelFileSize;
    private ButtonGroup groupRadios;
    private Model model;

    public View(){
        super();

        model = new Model();
        groupRadios = new ButtonGroup();
        groupRadios.add(verticalRadioButton);
        groupRadios.add(horizontalRadioButton);
        horizontalRadioButton.setSelected(model.isAdjustHorizontal());
        verticalRadioButton.setSelected(!model.isAdjustHorizontal());
        horizontalRadioButton.addActionListener(e -> {
            model.setAdjustHorizontal(true);
        });
        verticalRadioButton.addActionListener(e -> {
            model.setAdjustHorizontal(false);
        });
        selectFolderButton.addActionListener(e -> {
            JFileChooser fc;
            Preferences prefs;
            int returnVal;

            prefs = Preferences.userRoot().node(getClass().getName());
            fc = new JFileChooser(prefs.get("LAST_USED",
                    new File(".").getAbsolutePath()));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                ModelConstants result;
                prefs.put("LAST_USED", fc.getSelectedFile().getParent());
                result = model.obtainFilesFromDirectory(fc.getSelectedFile());
                switch (result){
                    case RET_EMPTY_DIRECTORY:
                        JOptionPane.showMessageDialog(this,
                                "Error: the selected folder is empty",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    case WRN_COUNT_EXCEEDS:
                        JOptionPane.showMessageDialog(this,
                                "Warning: more than 10 images in the folder",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    case RET_GOOD:
                        updateList();
                        break;
                }
                textField1.setText(model.getWorkingDirectoryPath());
            }
        });
        adjustButton.addActionListener(e -> {
            if (model.getState() != ModelConstants.STATE_READY){
                JOptionPane.showMessageDialog(this,
                        "Error: no images selected",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            else {
                switch (model.adjustAspectRatio()){
                    case RET_GOOD -> {
                        updateList();
                        JOptionPane.showMessageDialog(this,
                                "Done!",
                                "Info",
                                JOptionPane.INFORMATION_MESSAGE);
                        textField1.setText(model.getWorkingDirectoryPath());
                    }
                    default -> {
                    }
                }
            }
        });
    }

    public void updateList(){
        JList list;
        List<ImgFileInfo> imgInfos;
        DefaultListModel<String> listModel;

        listModel = new DefaultListModel<>();
        imgInfos = model.getAllImgsInfo();
        listModel.addAll(imgInfos.stream().map(ImgFileInfo::getName).toList());
        list = new JList(listModel);
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()){
                ImgFileInfo imgInfo;

                imgInfo = model.getImgInfo(list.getSelectedIndex());
                labelWidth.setText("Width: " + imgInfo.getWidth() + "px");
                labelHeight.setText("Height: " + imgInfo.getHeight() + "px");
                labelFileSize.setText("File size: " + (imgInfo.getSize() / 1024) + "KB");
            }
        });
        listImages.setViewportView(list);
    }

    public void start(){
        setTitle("Sizegram");
        setSize(400, 450);
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
