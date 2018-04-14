package musicsync;

import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;

import javax.swing.*;

public class AndroidBrowserList extends JList<PortableDeviceObject> {

    public AndroidBrowserList(ListModel<PortableDeviceObject> listModel) {
        super(listModel);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}
