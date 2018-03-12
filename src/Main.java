
import jmtp.PortableDevice;
import jmtp.PortableDeviceManager;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import musicsync.MusicSyncView;

import java.io.File;

public class Main {

        public static void main(String[] args) throws Throwable {
            MusicSyncView.drawGUI();
        }

    /*private static void jMTPeMethodTest()
    {
        PortableDeviceManager manager = new PortableDeviceManager();
        if(manager.getDevices().length==0){
            System.out.println("No device connected");
            return;
        }
        PortableDevice device = manager.getDevices()[0];
        // Connect to USB tablet
        device.open();
        System.out.println(device.getModel());
        System.out.println("---------------");
        // Iterate over deviceObjects
        for (PortableDeviceObject object : device.getRootObjects())
        {
            // If the object is a storage object
            if (object instanceof PortableDeviceStorageObject)
            {
                PortableDeviceStorageObject storage = (PortableDeviceStorageObject) object;
                for (PortableDeviceObject o2 : storage.getChildObjects())
                {
                    System.out.println(o2.getOriginalFileName());
                }
            }
        }

        manager.getDevices()[0].close();
    }*/

}

