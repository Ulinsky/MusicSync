package musicsync;

import jmtp.PortableDeviceObject;

import javax.swing.*;
import java.awt.*;

class AndroidBrowserView {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 600;

    void drawGui() {
        JFrame frame = createFrame();
        frame.pack();
        frame.setLocation(200, 100);
        frame.setMinimumSize(new Dimension(WIDTH,HEIGHT));
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        AndroidBrowserModel model;
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
        JButton upBtn = new JButton("Up");
        JButton downBtn = new JButton("Down");
        JButton selectBtn = new JButton("Select");
        northPnl.add(upBtn);
        northPnl.add(downBtn);
        northPnl.add(selectBtn);

        // handling the add button by adding a person to the list
        upBtn.addActionListener(a -> model.levelUp());

        // handling the remove button by removing the selected a person from the list
        downBtn.addActionListener(a -> {
            int selected = list.getSelectedIndex();
            if (selected < 0) {
                return;
            }
            model.levelDown(selected);
        });


        // handling the update button by changing the data of the selected a person
        selectBtn.addActionListener(a -> {
            int selected = list.getSelectedIndex();
            if (selected < 0) {
                return;
            }
            model.setMusicFolder(selected);
            frame.setVisible(false);
        });
        list.setCellRenderer(new AndroidFolderRenderer());
        return frame;
    }

    @SuppressWarnings("unused")
    private static class AndroidFolderRenderer implements ListCellRenderer<PortableDeviceObject> {
        private final JLabel label;

        /**
         * Constructor setting up the label used as rendering component.
         */
        AndroidFolderRenderer() {
            label = new JLabel();
            label.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PortableDeviceObject> list, PortableDeviceObject value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            label.setText(value.getName());
            if (isSelected) {
                label.setBackground(list.getSelectionBackground());
            } else {
                label.setBackground(list.getBackground());
            }
            return label;
        }

    }
}
