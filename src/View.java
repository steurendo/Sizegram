import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;

public class View extends JFrame implements ActionListener, ListSelectionListener {
    private JButton buttonAdjust;
    private JButton buttonSelectFolder;
    private JTextField textfieldCurrentFolder;
    private JPanel mainPanel;
    private JRadioButton radioHorizontal;
    private JRadioButton radioVertical;
    private JLabel labelWidth;
    private JLabel labelHeight;
    private JLabel labelFileSize;
    private JList listPictures;
    private JButton buttonToggleCustomRatio;
    private JProgressBar progressAdjust;
    private JPicture picturePreview;
    private ButtonGroup groupRadios;

    //APP
    private ModelState state;
    private final Model model;

    public View() {
        super();
        //VIEW
        groupRadios = new ButtonGroup();
        groupRadios.add(radioVertical);
        groupRadios.add(radioHorizontal);
        //LISTENERS
        radioHorizontal.addActionListener(this);
        radioVertical.addActionListener(this);
        buttonToggleCustomRatio.addActionListener(this);
        buttonSelectFolder.addActionListener(this);
        buttonAdjust.addActionListener(this);
        listPictures.addListSelectionListener(this);

        model = new Model(this);
        updateState(ModelState.NO_FOLDER_SELECTED);
    }

    public void start() {
        setTitle("Sizegram");
        setSize(600, 450);
        setContentPane(mainPanel);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void updateState(ModelState state) {
        this.state = state;
        textfieldCurrentFolder.setText(model.getWorkingDirectoryPath());
        buttonAdjust.setEnabled(state == ModelState.READY);
        updateRadios();
        if (state != ModelState.SAVING)
            reloadList();
    }
    private void updateRadios() {
        if (state == ModelState.READY) {
            radioHorizontal.setEnabled(!model.isUsingCustomRatio());
            radioVertical.setEnabled(!model.isUsingCustomRatio());
            buttonToggleCustomRatio.setEnabled(true);
            buttonToggleCustomRatio.setText(model.isUsingCustomRatio() ? "Unset custom ratio" : "Set custom ratio");
        }
        else {
            radioHorizontal.setEnabled(false);
            radioVertical.setEnabled(false);
            buttonToggleCustomRatio.setEnabled(false);
        }
    }

    private void reloadList() {
        List<PictureItem> pictureItems;
        DefaultListModel<String> listModel;

        pictureItems = model.getPictureItems();
        listModel = new DefaultListModel<>();
        listPictures.setModel(listModel);
        if (pictureItems != null)
            if (pictureItems.size() > 0) {
                listModel.addAll(pictureItems.stream().map(PictureItem::getName).toList());
                listPictures.setSelectedIndex(0);
                model.setSelectedItem(0);
            }
        updateSelectedItemInfo();
    }
    private void updateSelectedItemInfo() {
        PictureItem item = model.getSelectedItem();
        if (item == null) {
            picturePreview.setPicture(null);
            labelWidth.setText("Width: -");
            labelHeight.setText("Height: -");
            labelFileSize.setText("File size: -");
        } else {
            picturePreview.setPicture(model.getSelectedItem().getPicture());
            labelWidth.setText("Width: " + item.getWidth() + "px");
            labelHeight.setText("Height: " + item.getHeight() + "px");
            labelFileSize.setText("File size: " + (item.getSize() / 1024) + "KB");
        }
    }

    private void actionLoadFolder() {
        JFileChooser fc;
        Preferences prefs;
        int returnVal;

        prefs = Preferences.userRoot().node(getClass().getName());
        fc = new JFileChooser(prefs.get("LAST_USED",
                new File(".").getAbsolutePath()));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            ModelMessage result;
            prefs.put("LAST_USED", fc.getSelectedFile().getParent());
            result = model.loadFilesInFolder(fc.getSelectedFile());
            switch (result) {
                case ERR_EMPTY_DIRECTORY:
                    JOptionPane.showMessageDialog(this,
                            "Error: the selected folder is empty",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case ERR_NOT_A_DIRECTORY:
                    JOptionPane.showMessageDialog(this,
                            "Error: this is not a folder",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case WRN_COUNT_EXCEEDS:
                    JOptionPane.showMessageDialog(this,
                            "Warning: more than 10 images in the folder",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                case RET_GOOD:
                    updateState(ModelState.READY);
                    break;
            }
        }
    }

    private void actionAdjustPictures() {
        int result;
        String msg;

        msg = "Adjust aspect ratio for " + model.getPictureItems().size() + " items to ";
        if (model.isUsingCustomRatio())
            msg += "custom value?";
        else
            msg += model.getOutputRatio() == OutputRatio.HORIZONTAL ? "5:4?" : "4:5?";
        result = JOptionPane.showConfirmDialog(this, msg, "Sizegram", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {         //RESULT YES
            updateState(ModelState.SAVING);
            new SwingWorker<>() {
                @Override
                protected Object doInBackground() {
                    model.adjustRatio();
                    return null;
                }
            }.execute();
        }
    }

    public void notifyProgress(int percent) {
        if (percent == -1) {
            JOptionPane.showMessageDialog(this,
                    "Done!",
                    "Sizegram",
                    JOptionPane.INFORMATION_MESSAGE);
            progressAdjust.setValue(0);
            updateState(ModelState.NO_FOLDER_SELECTED);
        } else {
            progressAdjust.setValue(percent);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == radioHorizontal)
            model.setOutputRatio(OutputRatio.HORIZONTAL);
        else if (e.getSource() == radioVertical)
            model.setOutputRatio(OutputRatio.VERTICAL);
        else if (e.getSource() == buttonToggleCustomRatio) {
            model.toggleCustomRatio();
            updateRadios();
        }
        else if (e.getSource() == buttonSelectFolder)
            actionLoadFolder();
        else if (e.getSource() == buttonAdjust)
            actionAdjustPictures();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) {
            model.setSelectedItem(listPictures.getSelectedIndex());
            updateSelectedItemInfo();
        }
    }
}
