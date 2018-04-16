package musicsync;

import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import be.derycke.pieter.com.COMException;
import jmtp.PortableDevice;
import jmtp.PortableDeviceFolderObject;
import jmtp.PortableDeviceManager;
import jmtp.PortableDeviceObject;
import jmtp.PortableDeviceStorageObject;
import jmtp.PortableDeviceToHostImpl32;

class FileManager {

    @SuppressWarnings("ConstantConditions")
    private static List<File> listFilesForFolder(final File folder) {
        if (folder == null) {
            showMessageDialog(null, "Error.");
            return null;
        }
        List<File> songs = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) { //for each file in the folder
            if (fileEntry.isDirectory()) { //another folder found, recursive call
                songs.addAll(listFilesForFolder(fileEntry));
            } else if (fileEntry.getName().contains(".mp3")) {  //only files with .mp3 accepted
                songs.add(fileEntry);
            }
        }
        return songs;
    }

    static PortableDevice getDevice() {
        PortableDeviceManager manager = new PortableDeviceManager();
        PortableDevice device;

        if (manager.getDevices().length == 0) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        } else {
            device = manager.getDevices()[0]; //TODO allow user to select device, if multiple are connected (manager.getDevices.length>1)
            device.open();
            return device;
        }
    }

    String findAndroidPath() {
        PortableDeviceManager manager = new PortableDeviceManager();
        if (manager.getDevices().length == 0) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        }
        //First connected device (support multiple in future?)
        PortableDevice device = manager.getDevices()[0];
        // Connect to USB device
        device.open();
        PortableDeviceFolderObject musicFolder = (PortableDeviceFolderObject) findMusicFolders(device);
        if (musicFolder == null) {
            showMessageDialog(null, "No music folder found.");
            return null;
        }
        manager.getDevices()[0].close();
        return device.getFriendlyName() + "\\" + musicFolder.getParent().getName() + "\\" + musicFolder.getName(); //TODO make it NOT hardcoded
    }

    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder, String desktopPath) {
        BigInteger bigInteger1 = new BigInteger("123456789");
        File file = new File(desktopPath);
        try { //TODO clean up method to not print placeholders ("jj" and 123456789)
            targetFolder.addAudioObject(file, "jj", "jj", bigInteger1);
        } catch (Exception e) {
            System.out.println("Exception e = " + e);
        }
    }

    private static void copyFileFromDeviceToComputerFolder(PortableDeviceObject pDO, PortableDevice device, String path) {
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
        try {
            copy.copyFromPortableDeviceToHost(pDO.getID(), path, device);
        } catch (COMException ex) {
            ex.printStackTrace();
        }
        device.close();
    }

    private static List<PortableDeviceFolderObject> findMusicFolders(PortableDevice device) {
        if (device == null) {
            return null;
        }
        List<PortableDeviceFolderObject> allMusicFolders = new ArrayList<>();
        //Iterate through root folders
        Arrays.stream(device.getRootObjects()).forEach((f) -> Arrays.stream(((PortableDeviceStorageObject) f).getChildObjects()).forEach((s) -> findMusicFolders(s, allMusicFolders)));
        device.close();
        return allMusicFolders;
    }

    //TODO replace these find folder methods with Jlist androidBrowser / androidBrowserModel
    private static void findMusicFolders(Object source, List<PortableDeviceFolderObject> folderList) {

        if (source instanceof PortableDeviceFolderObject) {//TODO storage.getName().equals("Music") || storage.getName().equals("music")) {
            //Found the music folder
            PortableDeviceFolderObject storage = (PortableDeviceFolderObject) source;

            if (storage.getOriginalFileName().equalsIgnoreCase("music") || storage.getOriginalFileName().equalsIgnoreCase("musik"))
                folderList.add(storage);
            if (!hasNoMedia(storage)) {
                for (PortableDeviceObject o2 : storage.getChildObjects()) {
                    findMusicFolders(o2, folderList);
                }
            }
        }

        // If the object is a storage object
        else if (source instanceof PortableDeviceStorageObject) {
            PortableDeviceStorageObject storage = (PortableDeviceStorageObject) source;

            //Iterate through sub-folders
            for (PortableDeviceObject o2 : storage.getChildObjects()) {
                findMusicFolders(o2, folderList);
            }
        }
    }

    static String getFolderPath(PortableDeviceObject object) {
        StringBuilder path = new StringBuilder();

        while (!(object instanceof PortableDeviceStorageObject)) {
            path.insert(0, object.getOriginalFileName());
            path.insert(0, '/');
            object = object.getParent();
        }
        path.insert(0, object.getName());
        return path.toString();
    }

    private static boolean hasNoMedia(PortableDeviceFolderObject object) {
        for (PortableDeviceObject o2 : object.getChildObjects()) {
            if (o2.getOriginalFileName().equalsIgnoreCase(".nomedia"))
                return true;
        }

        return false;

    }

    //TODO check size, it could be that the files are too big for current android storage
    static void sync(String desktopPath, PortableDeviceFolderObject androidPath) {
        if (desktopPath == null || androidPath == null) return;
        List<File> desktopMusic = listFilesForFolder(new File(desktopPath));
        List<PortableDeviceObject> androidMusic = listFilesForFolder(androidPath);

        Stream<PortableDeviceObject> desktopMissingMusic;
        Stream<String> androidMissingMusic;
        //TODO null pointers possible, if desktop music == null|| empty, copy whole android music to desktop, and reverse hold too
        androidMissingMusic = desktopMusic.stream().filter((d) -> {
            for (PortableDeviceObject a : androidMusic) {
                if (a.getOriginalFileName().equalsIgnoreCase(d.getName()))
                    return false;
            }
            return true;
        }).map(File::getAbsolutePath);

        desktopMissingMusic = androidMusic.stream().filter((a) -> {
            for (File d : desktopMusic) {
                if (a.getOriginalFileName().equalsIgnoreCase(d.getName()))
                    return false;
            }
            return true;
        });
        androidMissingMusic.forEach((a) -> copyFileFromComputerToDeviceFolder(androidPath, a));
        desktopMissingMusic.forEach((a) -> copyFileFromDeviceToComputerFolder(a, getDevice(), desktopPath));
        if (getDevice() != null) getDevice().close();
    }

    private static List<PortableDeviceObject> listFilesForFolder(PortableDeviceFolderObject androidPath) {
        ArrayList<PortableDeviceObject> allMusicFiles = new ArrayList<>();
        allMusicFiles.addAll(Arrays.asList(androidPath.getChildObjects()));
        return allMusicFiles;
    }
}
