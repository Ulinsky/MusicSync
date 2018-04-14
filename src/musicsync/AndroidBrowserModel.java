package musicsync;

import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AndroidBrowserModel extends AbstractListModel<PortableDeviceObject> {

    private List<PortableDeviceFolderObject> folders;
    private PortableDeviceFolderObject musicFolder;

    @SuppressWarnings("ConstantConditions")
    AndroidBrowserModel() {
        this.folders = new ArrayList<>();
        if(FileManager.getDevice()==null)return;
        PortableDeviceObject[] rootObjects = FileManager.getDevice().getRootObjects();
        for (PortableDeviceObject folder : rootObjects) {
            if (folder.getName().equalsIgnoreCase("internal storage")) {
                PortableDeviceStorageObject internalStorage = (PortableDeviceStorageObject) folder;
                for (PortableDeviceObject subFolder : internalStorage.getChildObjects()) {
                    if (subFolder instanceof PortableDeviceFolderObject) {
                        PortableDeviceFolderObject storage = (PortableDeviceFolderObject) subFolder;
                        folders.add(storage);
                    }
                }
            }
        }
        FileManager.getDevice().close();
    }


    @Override
    public int getSize() {
        return folders.size();
    }

    @SuppressWarnings("ConstantConditions")
    void levelDown(int i) {   //TODO after method is called, go one level deeper
        PortableDeviceFolderObject folderObject = folders.get(i);
        folders.clear();
        for (PortableDeviceObject subFolder : folderObject.getChildObjects()) {
            if (subFolder instanceof PortableDeviceFolderObject) {
                PortableDeviceFolderObject storage = (PortableDeviceFolderObject) subFolder;
                folders.add(storage);
            }
        }
        this.fireContentsChanged(ListDataEvent.CONTENTS_CHANGED,0,folders.size());
        if(FileManager.getDevice()!=null)FileManager.getDevice().close();
    }


    //TODO probably should also make levelUp
    void levelUp() {
        System.out.println("TODO LEVEL UP");
    }


    public PortableDeviceFolderObject getMusicFolder() {
        return musicFolder;
    }

    void setMusicFolder(int i) {
        if(i<0||i>folders.size())return;
        musicFolder = folders.get(i);
    }

    @Override
    public PortableDeviceObject getElementAt(int i) {
        return folders.get(i);
    }
}
