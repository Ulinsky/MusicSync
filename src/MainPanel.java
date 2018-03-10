import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class MainPanel extends JPanel implements ActionListener {
    private static final int HEIGHT = 1200;
    private static final int WIDTH = 600;
    private String firstPath, secondPath;
    private JButton setFirstPathButton;
    private JButton setSecondPathButton;
    private JButton syncSongsButton = new JButton("Sync songs");
    private JTextField firstPathText;
    private JTextField secondPathText;
    private MainPanel() {
        super();
        super.setSize(new Dimension(HEIGHT, WIDTH));
        firstPathText=new JTextField("SourceFolder");
        secondPathText=new  JTextField("destinationFolder");
        this.add(firstPathText,BorderLayout.SOUTH);
        this.add(secondPathText, BorderLayout.SOUTH);
        setFirstPathButton = new JButton("Select first path");
        setSecondPathButton = new JButton("Select second path");
        add(setFirstPathButton, BorderLayout.NORTH);
        add(setSecondPathButton, BorderLayout.NORTH);


        setFirstPathButton.addActionListener(this);
        setSecondPathButton.addActionListener(this);
        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);
        syncSongsButton.setText("First choose 2 folders");
        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
               	if (firstPath == null || secondPath == null) {
                //error case if no path was selected
               		//Button is disabled so no nedd for a Dialoge
               		JOptionPane.showMessageDialog(new JButton("Ok"), "One of the paths were not set.");
                
            	return;
            }else{
            	
                syncSongsButton.setText("Sync");
                
            
            //making a list of .mp3 files found in the set paths
            List<File> firstSongList = listFilesForFolder(new File(firstPath));
            List<File> secondSongList = listFilesForFolder(new File(secondPath));
            //copies a file found in first ,but not the second list
            copyMissing(firstSongList, secondSongList, secondPath);
            //copies a file found in second ,but not the first list
            copyMissing(secondSongList, firstSongList, firstPath);
            }
            });
        add(syncSongsButton, BorderLayout.SOUTH);

    }

    static void drawGUI() {

        //renders the GUI
         JFrame frame = new JFrame("MusicSync");
        frame.setSize(new Dimension(HEIGHT, WIDTH));
        frame.add(new MainPanel());
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == setFirstPathButton || e.getSource() == setSecondPathButton) {
            //used to select folder and get path
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            //choose only folders
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setFirstPathButton) {
                firstPath = chooser.getSelectedFile().getPath();  //get first string
                firstPathText.setText(chooser.getSelectedFile().getPath());
            } else if (selection == JFileChooser.APPROVE_OPTION && e.getSource() == setSecondPathButton) {
                secondPath = chooser.getSelectedFile().getPath(); //get second string
                secondPathText.setText(chooser.getSelectedFile().getPath());
            }
        }
    }


    @SuppressWarnings("ConstantConditions")
    private List<File> listFilesForFolder(final File folder) {
        if (folder == null) {
            JOptionPane.showMessageDialog(new JButton("Ok"), "Error.");
            return null;
        }
        List<File> songs = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) { //for each file in the folder
            if (fileEntry.isDirectory()) { //another folder found, recursive call
                listFilesForFolder(fileEntry);
            } else if (fileEntry.getName().contains(".mp3")) {  //only files with .mp3 accepted
                songs.add(fileEntry);
            }
        }
        return songs;
    }

    private void copyMissing(List<File> source, List<File> destination, String secondPath) {
        if (source == null || destination == null || secondPath == null) {
            JOptionPane.showMessageDialog(new JButton("Ok"), "Error.");
            return;
        }
        boolean found;
        if (source.isEmpty()) {
            JOptionPane.showMessageDialog(new JButton("Ok"), "Source path contains no songs.");
            return;
        }
        int counter = 0;
        for (File toCompare : source) { //iterates first list
            found = false; //assume the song is not present
            for (File compareTo : destination) { //iterate second list
                if (toCompare.getName().equals(compareTo.getName())) { //song found
                    found = true;
                }
            }
            if (!found) { //case song not find, copy it to second directory
                try {
                    InputStream in = new FileInputStream(toCompare.getPath()); //file to be copied
                    String destPath = secondPath.concat("\\" + toCompare.getName()); //destination where to copy
                    OutputStream out = new FileOutputStream(destPath);
                    byte[] buf = new byte[(int) toCompare.length()]; //buffer
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len); //write process
                    }
                    in.close(); //done writing
                    out.close();
                    counter++;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(new JButton("Ok"), "Error while writing song.");
                }
            }
        }
        JOptionPane.showMessageDialog(new JButton("Ok"), counter == 1 ? counter + " song has been copied." : counter + " songs have been copied.");
    }
}
