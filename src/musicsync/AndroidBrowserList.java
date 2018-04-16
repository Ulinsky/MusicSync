package musicsync;

import jmtp.PortableDeviceObject;

import javax.swing.*;

class AndroidBrowserList extends JList<PortableDeviceObject> {

    AndroidBrowserList(ListModel<PortableDeviceObject> listModel) {
        super(listModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
