package musicsync;

import be.derycke.pieter.com.COMException;
import jmtp.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static javax.swing.JOptionPane.showMessageDialog;


public class MusicSyncView extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int HEIGHT = 200;
    private static final int WIDTH = 700;

        JPanel top = new JPanel(new BorderLayout());
        JPanel middle = new JPanel();
        JPanel bottom = new JPanel();
        PortableDevice d = null;
        
    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        
        d = FileManager.getDevice();
        
        Long time = System.nanoTime();
        List<PortableDeviceFolderObject> androidMusicFiles = FileManager.findMusicFolders(d);
        System.out.println(System.nanoTime() - time);
        
        String[] androidFileNames = new String[androidMusicFiles.size()];
        for(int i = 0; i < androidMusicFiles.size(); i++){
        	androidFileNames[i] = FileManager.getFolderPath(androidMusicFiles.get(i));
        }

        JTextField desktopText = new JTextField("Desktop music folder", 25);
        JTextField androidText = new JTextField("Android music folder", 18);
        
        JTextField desktopPath = new JTextField("Desktop folder", 25);
        JComboBox<String> androidPath = new JComboBox<String>(androidFileNames);
        
        desktopPath.setEditable(false);
        androidText.setEditable(false);


        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        top.add(desktopText, BorderLayout.LINE_START);
        top.add(androidText, BorderLayout.LINE_END);
        

        middle.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        middle.add(desktopPath, BorderLayout.LINE_START);
        middle.add(androidPath, BorderLayout.LINE_END);

        JButton setDesktopPathButton = new JButton("Select desktop path");
        JButton syncSongsButton = new JButton("First choose a folder");
        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);

        bottom.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        bottom.add(setDesktopPathButton, BorderLayout.LINE_START);
        bottom.add(syncSongsButton, BorderLayout.LINE_END);

        this.add(top, BorderLayout.NORTH);
        this.add(middle, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);

        setDesktopPathButton.addActionListener(actionEvent -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); //choose only folders
            chooser.setApproveButtonText("Select");
            int selection = chooser.showOpenDialog(null);
            if (selection == JFileChooser.APPROVE_OPTION) {
                desktopPath.setText(chooser.getSelectedFile().getPath());
                
                
                syncSongsButton.setBackground(Color.WHITE);
                syncSongsButton.setText("Sync songs");
                syncSongsButton.setEnabled(true);
            }
        });

        syncSongsButton.addActionListener((ActionEvent actionEvent) -> {
            FileManager.sync(desktopPath.getText(), androidMusicFiles.get(androidPath.getSelectedIndex()), d);
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


}
