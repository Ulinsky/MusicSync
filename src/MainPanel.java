import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MainPanel extends JPanel implements ActionListener {
    private static final int HEIGHT = 800;
    private static final int WIDTH = 600;
    private JFileChooser chooser;
    private String firstPath, secondPath;
    private JButton setFirstPathButton, setSecondPathButton, syncSongsButton;

    private MainPanel() {
        super();
        super.setSize(new Dimension(HEIGHT, WIDTH));

        setFirstPathButton = new JButton("Select first path");
        setSecondPathButton = new JButton("Select second path");
        add(setFirstPathButton, BorderLayout.WEST);
        add(setSecondPathButton, BorderLayout.EAST);


        setFirstPathButton.addActionListener(this);
        setSecondPathButton.addActionListener(this);

        syncSongsButton = new JButton("Sync songs");
        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
            if (firstPath == null || secondPath == null) {
                JOptionPane.showMessageDialog(new JButton("Ok"), "One of the paths were not set.");
                return;
            }
            List<File> firstSongList = listFilesForFolder(new File(firstPath));
            List<File> secondSongList = listFilesForFolder(new File(secondPath));
            copyMissing(firstSongList, secondSongList, secondPath);
            copyMissing(secondSongList, firstSongList, firstPath);
        });
        add(syncSongsButton, BorderLayout.SOUTH);

    }

    static void drawGUI() {
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
        if (e.getSource() == setFirstPathButton || e.getSource() == setSecondPathButton) {
            chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setFirstPathButton) {
                firstPath = chooser.getSelectedFile().getPath();
            } else if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setSecondPathButton) {
                secondPath = chooser.getSelectedFile().getPath();
            }
        }
    }


    @SuppressWarnings("ConstantConditions")
    private List<File> listFilesForFolder(final File folder) {
        if (folder == null) {
            return null;
        }
        List<File> songs = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else if (fileEntry.getName().contains(".mp3")) {
                songs.add(fileEntry);
            }
        }
        return songs;
    }

    private void copyMissing(List<File> source, List<File> destination, String secondPath) {
        boolean found;
        if (source.isEmpty()) {
            JOptionPane.showMessageDialog(new JButton("Ok"), "Source path contains no songs.");
            return;
        }
        int counter = 0;
        for (File toCompare : source) {
            found = false;
            for (File compareTo : destination) {
                if (toCompare.getName().equals(compareTo.getName())) {
                    found = true;
                }
            }
            if (!found) {
                try {
                    InputStream in = new FileInputStream(toCompare.getPath());
                    String destPath = secondPath.concat("\\" + toCompare.getName());
                    System.out.println(destPath);
                    OutputStream out = new FileOutputStream(destPath);
                    byte[] buf = new byte[(int) toCompare.length()];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    counter++;
                } catch (Exception e) {
                    System.out.println("Error when copying files");
                }
            }
        }
        JOptionPane.showMessageDialog(new JButton("Ok"), counter == 1 ? counter + " song has been copied." : counter + " songs have been copied.");
    }
}
