package musicsync;

import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class AndroidBrowserView {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private AndroidBrowserModel model;
    private ActionListener okEvent;

    void drawGui() {
        JFrame frame = createFrame();
        frame.pack();
        frame.setLocation(200, 100);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        AndroidBrowserList list;
        JFrame frame = new JFrame("Folder selection");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        // create person model and list component
        model = new AndroidBrowserModel();
        list = new AndroidBrowserList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frame.getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);

        // setting up the input controls and buttons
        JPanel northPnl = new JPanel();
        frame.getContentPane().add(northPnl, BorderLayout.NORTH);
        JButton upBtn = new JButton("Back");
        JButton downBtn = new JButton("Go into");
        JButton selectBtn = new JButton("Select folder");
        northPnl.add(upBtn);
        northPnl.add(downBtn);
        northPnl.add(selectBtn);

        //moves back to previous folders
        upBtn.addActionListener(a -> model.goBackToPrevList());

        //tunnels into a folder
        downBtn.addActionListener(a -> {
            int selected = list.getSelectedIndex();
            if (selected < 0) {
                return;
            }
            model.stepInto(selected);
        });


        //selects a folder as the music folder and hides the gui
        selectBtn.addActionListener(a -> {
            int selected = list.getSelectedIndex();
            if (selected < 0) {
                return;
            }
            model.setMusicFolder(selected);
            handleOkButtonClick(a);
            frame.setVisible(false);
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                if (evt.getClickCount() > 1) {
                    // multi-click detected
                    int index = list.locationToIndex(evt.getPoint());
                    model.stepInto(index);
                }
            }
        });
        list.setCellRenderer(new AndroidFolderRenderer()); //using renderer to "map" PortableDeviceObjects to Strings
        return frame;
    }

    PortableDeviceFolderObject getSelectedFolder() {
        return model.getMusicFolder();
    }

    void setOnOk(ActionListener event) {
        okEvent = event;
    }

    private void handleOkButtonClick(ActionEvent e) {
        if (okEvent != null) {
            okEvent.actionPerformed(e);
        }
    }

    private static class AndroidFolderRenderer implements ListCellRenderer<PortableDeviceObject> {
        private final JLabel label;

        AndroidFolderRenderer() {
            label = new JLabel();
            label.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PortableDeviceObject> list, PortableDeviceObject value, int index, boolean isSelected, boolean cellHasFocus) {
            label.setText("          " + value.getName());
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
            } else {
                label.setBackground(list.getBackground());
            }
            return label;
        }

    }

}
