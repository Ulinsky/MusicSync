package musicsync;

import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

class AndroidBrowserModel extends AbstractListModel<PortableDeviceObject> {

    private List<PortableDeviceFolderObject> folders;
    private PortableDeviceFolderObject musicFolder;
    private Stack<List<PortableDeviceFolderObject>> undo;

    @SuppressWarnings("ConstantConditions")
    AndroidBrowserModel() {
        this.folders = new ArrayList<>();
        this.undo = new Stack<>();
        if (FileManager.getDevice() == null) return;
        PortableDeviceObject[] rootObjects = FileManager.getDevice().getRootObjects();
        for (PortableDeviceObject folder : rootObjects) {
            if (folder instanceof PortableDeviceStorageObject) {
                PortableDeviceStorageObject internalStorage = (PortableDeviceStorageObject) folder;
                for (PortableDeviceObject subFolder : internalStorage.getChildObjects()) {
                    if (subFolder instanceof PortableDeviceFolderObject) {
                        PortableDeviceFolderObject storage = (PortableDeviceFolderObject) subFolder;
                        folders.add(storage);
                        folders.sort(Comparator.comparing(PortableDeviceObject::getName));
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
    void stepInto(int i) {
        PortableDeviceFolderObject folderObject = folders.get(i);
        List<PortableDeviceFolderObject> copyList = new ArrayList<>();
        copyList.addAll(folders);
        undo.push(copyList);
        folders.clear();
        for (PortableDeviceObject subFolder : folderObject.getChildObjects()) {
            if (subFolder instanceof PortableDeviceFolderObject) {
                PortableDeviceFolderObject storage = (PortableDeviceFolderObject) subFolder;
                folders.add(storage);
            }
        }
        this.fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, folders.size());
        if (FileManager.getDevice() != null) FileManager.getDevice().close();
    }


    void goBackToPrevList() {
        if (undo.empty()) return;
        folders = undo.pop();
        this.fireContentsChanged(ListDataEvent.CONTENTS_CHANGED, 0, folders.size());
    }


    PortableDeviceFolderObject getMusicFolder() {
        return musicFolder;
    }

    void setMusicFolder(int i) {
        if (i < 0 || i > folders.size()) return;
        musicFolder = folders.get(i);
    }

    @Override
    public PortableDeviceObject getElementAt(int i) {
        return folders.get(i);
    }
}
