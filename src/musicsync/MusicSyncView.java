package musicsync;

import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;


public class MusicSyncView extends JPanel {
    private static final int HEIGHT = 600;
    private static final int WIDTH = 700;
    private static final int DEFAULT_DEVICE_INDEX = 0;

    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        JTextField desktopPath = new JTextField("Desktop folder", 25);
        JTextField androidPath = new JTextField("Android music folder", 18);
        desktopPath.setEditable(false);
        androidPath.setEditable(false);

        JPanel upperHalf = new JPanel(new BorderLayout());
        JPanel middle = new JPanel(new BorderLayout());
        JTextPane desktopSongsPreview= new JTextPane();
        JTextPane androidSongsPreview= new JTextPane();
        desktopSongsPreview.setEditable(false);
        androidSongsPreview.setEditable(false);
        middle.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50));
        desktopSongsPreview.setPreferredSize(new Dimension((int)(WIDTH*0.4),(int)(HEIGHT*0.6)));
        androidSongsPreview.setPreferredSize(new Dimension((int)(WIDTH*0.4),(int)(HEIGHT*0.6)));
        middle.add(desktopSongsPreview,BorderLayout.LINE_START);
        middle.add(androidSongsPreview,BorderLayout.LINE_END);
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
        this.add(middle,BorderLayout.CENTER);

        setDesktopPathButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //choose only folders
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                desktopPath.setText(chooser.getSelectedFile().getPath());
                androidPath.setText(findAndroidPath(DEFAULT_DEVICE_INDEX));
                syncSongsButton.setBackground(Color.WHITE);
                syncSongsButton.setText("Sync songs");
                syncSongsButton.setEnabled(true);
            }
        });

        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
            List<File> desktopSongs = listFilesForDesktopFolder(new File(desktopPath.getText()));
            List<PortableDeviceObject> androidSongs = listFilesForAndroidFolder(DEFAULT_DEVICE_INDEX);
            if (desktopSongs != null) {
                StringBuilder sb = new StringBuilder();
                for (File song:desktopSongs){
                    sb.append(song.getName()).append("\n");
                }
                desktopSongsPreview.setText(sb.toString());
            }else{
               desktopSongsPreview.setText("No songs found.");
            }
            if (androidSongs != null) {
                StringBuilder sb = new StringBuilder();
                for (PortableDeviceObject song:androidSongs){
                    sb.append(song.getName()).append("\n");
                }
                androidSongsPreview.setText(sb.toString());
            }else{
                desktopSongsPreview.setText("No songs found.");
            }
            //TODO COMPARE TWO LISTS; ADD THE MISSING SONGS TO THE DESKTOP AND ANDROID FOLDER
        });
    }

    public static void drawGUI() {
        //renders the GUI
        JFrame frame = new JFrame("MusicSync");
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(new MusicSyncView());
        frame.pack();
        frame.setLocation(300, 100);
        frame.setSize(new Dimension(WIDTH, HEIGHT));
        frame.setResizable(false);
        frame.setVisible(true);
    }

    @SuppressWarnings("ConstantConditions")
    private List<File> listFilesForDesktopFolder(File folder) {
        if (folder == null) {
            showMessageDialog(null, "Error, folder does not exist.");
            return null;
        }
        List<File> songs = new ArrayList<>();
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) { //another folder found, recursive call
                songs.addAll(listFilesForDesktopFolder(fileEntry));
            } else if (fileEntry.getName().contains(".mp3")) {  //only files with .mp3 accepted
                songs.add(fileEntry);
            }
        }
        return songs;
    }

    private List<PortableDeviceObject> listFilesForAndroidFolder(int deviceIndex) {
        PortableDevice device = findDevice(deviceIndex);
        if (device == null) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        }
        PortableDeviceFolderObject musicFolder = findMusicFolder(device);
        if (musicFolder == null) {
            showMessageDialog(null, "No music folder found.");
            return null;
        }
        return Arrays.asList(musicFolder.getChildObjects());
    }


    private String findAndroidPath(int deviceIndex) {
        PortableDevice device = findDevice(deviceIndex);
        if (device == null) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        }
        PortableDeviceFolderObject musicFolder = findMusicFolder(device);
        if (musicFolder == null) {
            showMessageDialog(null, "No music folder found.");
            return null;
        }
        //TODO MAKE IT NOT HARDCODED
        return device.getFriendlyName() + "\\" + musicFolder.getParent().getName() + "\\" + musicFolder.getName();
    }


    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder) {
        BigInteger bigInteger1 = new BigInteger("123456789");
        File file = new File("C:\\GettingJMTP.pdf");
        try {
            targetFolder.addAudioObject(file, "jj", "jj", bigInteger1);
        } catch (Exception e) {
            System.out.println("Exception e = " + e);
        }
    }

    private static void copyFileFromDeviceToComputerFolder(PortableDeviceObject song, PortableDevice device, String target) {
        device.open();
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
        try {
            copy.copyFromPortableDeviceToHost(song.getID(), target, device);
        } catch (COMException ex) {
            ex.printStackTrace();
        }finally {
            device.close();
        }
    }

    private PortableDeviceFolderObject findMusicFolder(PortableDevice device) {
        device.open();
        //Iterate through root folders
        for (PortableDeviceObject objects : device.getRootObjects()) {
            // If the object is a storage object
            if (objects instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) objects;
                //Iterate through sub-folders
                for (PortableDeviceObject o2 : storage.getChildObjects()) { //TODO TUNNEL THROUGH ALL SUBFOLDERS
                    if (o2.getName().equals("Music")) {
                        //Found the music folder
                        device.close();
                        return (PortableDeviceFolderObject) o2;
                    }
                }
            }
        }
        device.close();
        //No music folder was found
        return null;
    }

    private PortableDevice findDevice(int deviceIndex) {
        PortableDeviceManager manager = new PortableDeviceManager();
        return manager.getDevices().length == 0 ? null : manager.getDevices()[deviceIndex];
    }

}
