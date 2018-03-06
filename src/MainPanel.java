import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainPanel extends JPanel implements ActionListener {
    private static final int HEIGHT = 800;
    private static final int WIDTH = 600;
    private JFileChooser chooser;
    private String desktopPath, androidPath;
    private JButton setDesktopPath, setAndroidPath, syncSongs;
    private JLabel desktopPathLabel, androidPathLabel;

    public MainPanel() {
        super(new BorderLayout());
        setSize(new Dimension(HEIGHT, WIDTH));
        JPanel upperPanel = new JPanel();
        setDesktopPath = new JButton("Select desktop path");
        upperPanel.add(setDesktopPath, BorderLayout.WEST);
        setAndroidPath = new JButton("Select android path");
        upperPanel.add(setAndroidPath, BorderLayout.EAST);
        JPanel middlePanel = new JPanel();
        desktopPathLabel = new JLabel("No desktop path set");
        androidPathLabel = new JLabel("No android path set");
        middlePanel.add(desktopPathLabel, BorderLayout.WEST);
        middlePanel.add(androidPathLabel, BorderLayout.EAST);
        setDesktopPath.addActionListener(this);
        setAndroidPath.addActionListener(this);
        add(upperPanel, BorderLayout.NORTH);
        add(middlePanel, BorderLayout.CENTER);
        JPanel lowerPanel = new JPanel();
        syncSongs = new JButton("Sync songs");
        syncSongs.addActionListener(this);
        lowerPanel.add(syncSongs);
        add(lowerPanel, BorderLayout.SOUTH);
    }

    public static void showGui() {
        JFrame frame = new JFrame("MusicSync");
        frame.setSize(new Dimension(HEIGHT, WIDTH));
        frame.add(new MainPanel());
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setDesktopPath || e.getSource() == setAndroidPath) {
            chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setDesktopPath) {
                desktopPath = chooser.getSelectedFile().getPath();
                desktopPathLabel.setText("Desktop path set");
                System.out.println(desktopPath);
            } else if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setAndroidPath) {
                androidPath = chooser.getSelectedFile().getPath();
                androidPathLabel.setText("Android path set");
                System.out.println(androidPath);
            }
        } else if (e.getSource() == syncSongs) {
            if (desktopPath == null || androidPath == null) {
                System.out.println("Either desktop or android path was not set");
                return;
            }
            List<File> desktopSongs = listFilesForFolder(new File(desktopPath));
            List<File> androidSongs = listFilesForFolder(new File(androidPath));
            //TODO COMPARE AND COPY
        }
    }
    public List<File> listFilesForFolder(final File folder) {
        List<File> songs = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else if(fileEntry.getName().contains(".mp3")){
               songs.add(fileEntry);
            }
        }
        return songs;
    }
}
