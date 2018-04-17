package musicsync;

import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;


public class MusicSyncView extends JPanel {
    //dimensions of the main frame
    private static final int HEIGHT = 500;
    private static final int WIDTH = 700;
    //positions for arrays, final values changed in anon classes
    private static final int DESKTOP_PATH_FLAG_POS = 0;
    private static final int ANDROID_PATH_FLAG_POS = 1;
    private static final int MUSIC_FOLDER_POS = 0;
    private final JTextArea desktopPreview;
    private final JTextArea androidPreview;
    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JTextField desktopPath = new JTextField("Desktop music folder", WIDTH / 30);
        JTextField androidPath = new JTextField("Android music folder", WIDTH / 30);
        final PortableDeviceFolderObject[] musicFolder = new PortableDeviceFolderObject[1];
        boolean[] pathFlags = {false, false};
        desktopPath.setEditable(false);
        androidPath.setEditable(false);

        desktopPreview = new JTextArea();
        desktopPreview.setEditable(false);
        desktopPreview.setPreferredSize(new Dimension((int) (WIDTH * 0.4), (int) (HEIGHT * 0.7)));

        androidPreview = new JTextArea();
        androidPreview.setEditable(false);
        androidPreview.setPreferredSize(new Dimension((int) (WIDTH * 0.4), (int) (HEIGHT * 0.7)));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, (int) (WIDTH * 0.05), 0, (int) (WIDTH * 0.05)));
        top.add(desktopPath, BorderLayout.LINE_START);
        top.add(androidPath, BorderLayout.LINE_END);


        JPanel middle = new JPanel();

        middle.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        middle.add(new JScrollPane(desktopPreview), BorderLayout.LINE_START);
        middle.add(new JScrollPane(androidPreview), BorderLayout.LINE_END);

        JButton setDesktopFolderButton = new JButton("Select desktop folder");
        JButton setAndroidFolderButton = new JButton("Select android folder");
        JButton syncSongsButton = new JButton("First choose a folder");

        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);

        JPanel bottom = new JPanel();
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        bottom.add(setDesktopFolderButton, BorderLayout.LINE_START);
        bottom.add(setAndroidFolderButton, BorderLayout.CENTER);
        bottom.add(syncSongsButton, BorderLayout.LINE_END);

        this.add(top, BorderLayout.NORTH);
        this.add(middle, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);

        setDesktopFolderButton.addActionListener(actionEvent -> EventQueue.invokeLater(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //choose only folders
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                desktopPath.setText(chooser.getSelectedFile().getPath());
                pathFlags[DESKTOP_PATH_FLAG_POS] = true;
                if (pathFlags[ANDROID_PATH_FLAG_POS]) {
                    enableMusicSyncButton(syncSongsButton);
                }
                updateDesktopPreview(new File(desktopPath.getText()));
            }
        }));

        setAndroidFolderButton.addActionListener(actionEvent -> {
            if (FileManager.getDevice() == null) return;
            EventQueue.invokeLater(() -> {
                AndroidBrowserView list = new AndroidBrowserView();
                list.drawGui();
                list.setOnSelection(e -> {
                    musicFolder[MUSIC_FOLDER_POS] = list.getSelectedFolder();
                    androidPath.setText(FileManager.getFolderPath(musicFolder[MUSIC_FOLDER_POS]));
                    updateAndroidPreview(musicFolder[MUSIC_FOLDER_POS]);
                    pathFlags[ANDROID_PATH_FLAG_POS] = true;
                    if (pathFlags[DESKTOP_PATH_FLAG_POS]) {
                        enableMusicSyncButton(syncSongsButton);
                    }
                });

            });
        });
        syncSongsButton.addActionListener((ActionEvent actionEvent) -> EventQueue.invokeLater(() -> {
            FileManager.sync(desktopPath.getText(), musicFolder[MUSIC_FOLDER_POS]);
            updateDesktopPreview(new File(desktopPath.getText()));
            updateAndroidPreview(musicFolder[MUSIC_FOLDER_POS]);
        }));
    }

    public static void drawGUI() {
        //renders the GUI
        JFrame frame = new JFrame("MusicSync");
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setLocation(200, 100);
        frame.add(new MusicSyncView());
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void updateAndroidPreview(PortableDeviceFolderObject androidFolder) {
        StringBuilder sb = new StringBuilder();
        for (PortableDeviceObject obj : FileManager.listFilesForFolder(androidFolder)) {
            sb.append(obj.getOriginalFileName()).append("\n");
        }
        if (sb.length() == 0) sb.append("Selected folder contains no songs.");
        androidPreview.setText(sb.toString());
    }

    private void updateDesktopPreview(File folder) {
        StringBuilder sb = new StringBuilder();
        for (File f : FileManager.listFilesForFolder(folder)) {
            sb.append(f.getName()).append("\n");
        }
        if (sb.length() == 0) sb.append("Selected folder contains no songs.");
        desktopPreview.setText(sb.toString());
    }

    private void enableMusicSyncButton(JButton syncSongsButton) {
        syncSongsButton.setBackground(Color.WHITE);
        syncSongsButton.setText("Sync songs");
        syncSongsButton.setEnabled(true);
    }


}
