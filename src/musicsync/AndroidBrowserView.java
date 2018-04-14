package musicsync;

import jmtp.PortableDeviceObject;

import javax.swing.*;
import java.awt.*;

public class AndroidBrowserView {
    private static JFrame frame;
    private static JButton upBtn;
    private static JButton downBtn;
    private static JButton selectBtn;
    private static AndroidBrowserModel model;
    private static AndroidBrowserList list;

    public JFrame getFrame() {
        frame = createFrame();
        frame.pack();
        frame.setLocation(200, 200);
        frame.setVisible(true);
        return frame;
    }

    private static JFrame createFrame() {
        JFrame frame = new JFrame("Folder selection");
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // create person model and list component
        model = new AndroidBrowserModel();
        list = new AndroidBrowserList(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        frame.getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);

        // setting up the input controls and buttons
        JPanel northPnl = new JPanel();
        frame.getContentPane().add(northPnl, BorderLayout.NORTH);
        upBtn = new JButton("Up");
        downBtn = new JButton("Down");
        selectBtn = new JButton("Select");
        northPnl.add(upBtn);
        northPnl.add(downBtn);
        northPnl.add(selectBtn);

        // handling the add button by adding a person to the list
        upBtn.addActionListener(a -> {
            model.levelUp();
        });

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
