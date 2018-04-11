package musicsync;

import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;


public class MusicSyncView extends JPanel {
    private static final int HEIGHT = 100;
    private static final int WIDTH = 700;

    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        JTextField desktopPath = new JTextField("Desktop folder", 25);
        JTextField androidPath = new JTextField("Android music folder", 18);
        desktopPath.setEditable(false);
        androidPath.setEditable(false);

        JPanel upperHalf = new JPanel(new BorderLayout());
        JPanel lowerHalf = new JPanel();
        upperHalf.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50));
        upperHalf.add(desktopPath, BorderLayout.LINE_START);
        upperHalf.add(androidPath, BorderLayout.LINE_END);

        JButton setDesktopPathButton = new JButton("Select desktop path");
        JButton syncSongsButton = new JButton("First choose a folder");
        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);

        lowerHalf.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        lowerHalf.add(setDesktopPathButton, BorderLayout.LINE_START);
        lowerHalf.add(syncSongsButton, BorderLayout.LINE_END);
        this.add(upperHalf, BorderLayout.NORTH);
        this.add(lowerHalf, BorderLayout.SOUTH);

        setDesktopPathButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //choose only folders
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                desktopPath.setText(chooser.getSelectedFile().getPath());
                androidPath.setText(findAndroidPath());
                syncSongsButton.setBackground(Color.WHITE);
                syncSongsButton.setText("Sync songs");
                syncSongsButton.setEnabled(true);
            }
        });

        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
            showMessageDialog(null, "Not implemented yet");
            //List<File> desktopSongs = listFilesForFolder(new File(desktopPath.getText())); //TODO MAKE LIST OF ANDROID SONGS
            //TODO COMPARE TWO LISTS; ADD THE MISSING SONGS TO THE DESKTOP AND ANDROID FOLDER
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
                songs.addAll(listFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".mp3")) {  //only files with .mp3 accepted
                songs.add(fileEntry);
            }
        }
        return songs;
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

        PortableDeviceObject[] songs = musicFolder.getChildObjects();
       /* for (PortableDeviceObject song : songs) {
            copyFileFromDeviceToComputerFolder(song,device,desktopPath);
            }*/ //TODO EXPORT TO ANOTHER METHOD
        manager.getDevices()[0].close();
        return device.getFriendlyName() + "\\" + musicFolder.getParent().getName() + "\\" + musicFolder.getName(); //TODO make it NOT hardcoded
    }
    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder)
    {
                BigInteger bigInteger1 = new BigInteger("123456789");
                File file = new File("C:\\GettingJMTP.pdf");
                try {
                        targetFolder.addAudioObject(file, "jj", "jj", bigInteger1);
                    } catch (Exception e) {
                        System.out.println("Exception e = " + e);
                    }
    }

    private static void copyFileFromDeviceToComputerFolder(PortableDeviceObject pDO, PortableDevice device, String path) {
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
        try {
            copy.copyFromPortableDeviceToHost(pDO.getID(), path, device);
        } catch (COMException ex) {
            ex.printStackTrace();
        }
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
