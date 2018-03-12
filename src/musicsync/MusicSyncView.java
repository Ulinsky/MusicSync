package musicsync;

import jmtp.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;


public class MusicSyncView extends JPanel {
    private static final int HEIGHT = 100;
    private static final int WIDTH = 800;

    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        JTextField firstPathText = new JTextField("First folder", 20);
        JTextField secondPathText = new JTextField("Second folder", 30);
        firstPathText.setEditable(false);
        secondPathText.setEditable(false);

        JPanel upperHalf = new JPanel(new BorderLayout());
        JPanel lowerHalf = new JPanel();
        upperHalf.setBorder(BorderFactory.createEmptyBorder(10, 100, 0, 100));
        upperHalf.add(firstPathText, BorderLayout.LINE_START);
        upperHalf.add(secondPathText, BorderLayout.LINE_END);

        JButton setDesktopPathButton = new JButton("Select desktop path");
        JButton syncSongsButton = new JButton("First choose a folder");
        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);

        lowerHalf.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        lowerHalf.add(setDesktopPathButton,BorderLayout.LINE_START);
        lowerHalf.add(syncSongsButton, BorderLayout.LINE_END);
        this.add(upperHalf, BorderLayout.NORTH);
        this.add(lowerHalf, BorderLayout.SOUTH);

        setDesktopPathButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            //choose only folders
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                firstPathText.setText(chooser.getSelectedFile().getPath());
                secondPathText.setText(findAndroidPath());
                syncSongsButton.setBackground(Color.WHITE);
                syncSongsButton.setText("Sync songs");
                syncSongsButton.setEnabled(true);
            }
        });

        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
            showMessageDialog(null, "Not implemented yet");
        });
    }

    public static void drawGUI() {
        //renders the GUI
        JFrame frame = new JFrame("MusicSync");
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(new MusicSyncView());
        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    @SuppressWarnings("ConstantConditions")
    private List<File> listFilesForFolder(final File folder) {
        if (folder == null) {
            showMessageDialog(null, "Error.");
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
            showMessageDialog(null, "Error.");
            return;
        }
        boolean found;
        if (source.isEmpty()) {
            showMessageDialog(null, "Source path contains no songs.");
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
                    showMessageDialog(null, "Error while writing song.");
                }
            }
        }
        showMessageDialog(null, counter == 1 ? counter + " song has been copied." : counter + " songs have been copied.");
    }

    private String findAndroidPath() {
        PortableDeviceManager manager = new PortableDeviceManager();
        if (manager.getDevices().length == 0) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        }
        //First connected device (support multiple in future?)
        PortableDevice device = manager.getDevices()[0];
        // Connect to USB device
        device.open();
        PortableDeviceFolderObject musicFolder = findMusicFolder(device);
        if (musicFolder == null) {
            showMessageDialog(null, "No music folder found.");
            return null;
        }
        //End the connection
        manager.getDevices()[0].close();
        return device.getFriendlyName() + "\\" + musicFolder.getParent().getName() + "\\" + musicFolder.getName(); //TODO make it NOT hardcoded
    }


    private PortableDeviceFolderObject findMusicFolder(PortableDevice device) {
        //Iterate through root folders
        for (PortableDeviceObject objects : device.getRootObjects()) {
            // If the object is a storage object
            if (objects instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) objects;
                //Iterate through sub-folders
                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    if (o2.getName().equals("Music")) {
                        //Found the music folder
                        return (PortableDeviceFolderObject) o2;
                    }
                }
            }
        }
        //No music folder was found
        return null;
    }

}
