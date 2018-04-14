package musicsync;

import oracle.jrockit.jfr.JFR;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.JOptionPane.showMessageDialog;


public class MusicSyncView extends JPanel {

    private static final int HEIGHT = 500;
    private static final int WIDTH = 700;


    private MusicSyncView() {
        super(new BorderLayout());
        super.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JTextField desktopPath = new JTextField("Desktop music folder", 25);
        JTextField androidPath = new JTextField("Android music folder", 18);
        desktopPath.setEditable(false);
        androidPath.setEditable(false);

        JTextPane desktopPreview = new JTextPane();
        desktopPreview.setEditable(false);
        desktopPreview.setPreferredSize(new Dimension((int) (WIDTH * 0.4), (int) (HEIGHT * 0.7)));

        JTextPane androidPreview = new JTextPane();
        androidPreview.setEditable(false);
        androidPreview.setPreferredSize(new Dimension((int) (WIDTH * 0.4), (int) (HEIGHT * 0.7)));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(10, (int) (WIDTH * 0.05), 0, (int) (WIDTH * 0.05)));
        top.add(desktopPath, BorderLayout.LINE_START);
        top.add(androidPath, BorderLayout.LINE_END);


        JPanel middle = new JPanel();
        middle.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        middle.add(desktopPreview, BorderLayout.LINE_START);
        middle.add(androidPreview, BorderLayout.LINE_END);

        JButton setDesktopPathButton = new JButton("Select desktop path");
        JButton syncSongsButton = new JButton("First choose a folder");
        syncSongsButton.setEnabled(false);
        syncSongsButton.setBackground(Color.RED);

        JPanel bottom = new JPanel();
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
            new AndroidBrowserView().getFrame();

           // FileManager.sync(desktopPath.getText(), null); //TODO implements JList for selecting folder
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
