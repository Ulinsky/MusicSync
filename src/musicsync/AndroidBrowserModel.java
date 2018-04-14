package musicsync;

import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

class AndroidBrowserModel extends AbstractListModel<PortableDeviceObject> {

    private List<PortableDeviceObject> folders;
    private PortableDeviceFolderObject musicFolder;

    AndroidBrowserModel() {
        this.folders = new ArrayList<>();
        PortableDeviceObject[] rootObjects = FileManager.getDevice().getRootObjects();
        for (PortableDeviceObject folder : rootObjects) {
                folders.add(folder);
            }
        }


    @Override
    public int getSize() {
        return folders.size();
    }

   void levelDown(int i) {   //TODO after method is called, go one level deeper
       System.out.println("TODO");
        }


    //TODO probably should also make levelUp
     void levelUp(){
        System.out.println("TODO");
        //TODO
    }

    public PortableDeviceFolderObject getMusicFolder() {
        return musicFolder;
    }

    void setMusicFolder(int i) {
       //TODO set selected music folder
    }

    @Override
    public PortableDeviceObject getElementAt(int i) {
        return folders.get(i);
    }
}
