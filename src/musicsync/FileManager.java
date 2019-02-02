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
	
	private static String[] fileEndings= {".mp3",".wav",".wma"};


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

    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder, String desktopPath) {
        if (targetFolder == null || desktopPath == null || desktopPath.isEmpty()) return;
        File file = new File(desktopPath);
        try {
            targetFolder.addAudioObject(file, null, file.getName(), BigInteger.valueOf(file.getTotalSpace()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void copyFileFromDeviceToComputerFolder(PortableDeviceObject pDO, PortableDevice device, String path) {
        if (pDO == null || device == null || path == null || path.isEmpty()) return;
        PortableDeviceToHostImpl32 copy = new PortableDeviceToHostImpl32();
        try {
            copy.copyFromPortableDeviceToHost(pDO.getID(), path, device);
        } catch (COMException ex) {
            ex.printStackTrace();
        }
        device.close();
    }


    static String getFolderPath(PortableDeviceObject object) {
        StringBuilder path = new StringBuilder();

        while (!(object instanceof PortableDeviceStorageObject)) {
            path.insert(0, object.getOriginalFileName());
            path.insert(0, '/');
            object = object.getParent();
        }
        path.insert(0, object.getName());
        PortableDevice d = getDevice();
        if (d == null) return path.toString();
        path.insert(0, '/');
        path.insert(0, getDevice().getFriendlyName());
        return path.toString();
    }

    static void sync(String desktopPath, PortableDeviceFolderObject androidPath) {
        if (desktopPath == null || androidPath == null) return;
        List<File> desktopMusic = listFilesForFolder(new File(desktopPath));
        List<PortableDeviceObject> androidMusic = listFilesForFolder(androidPath);
        Stream<PortableDeviceObject> desktopMissingMusic;
        Stream<String> androidMissingMusic;
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
        showMessageDialog(null, "Music sync is done");
    }

    //TODO check if these methods are needed, or if the operating system automatically cancels the transfer if there is not enough space
    /*private BigInteger calculateAndroidListSize( List<PortableDeviceObject> androidMusic){
        BigInteger totalSize=BigInteger.valueOf(0);
        for(PortableDeviceObject obj:androidMusic ){
          totalSize = totalSize.add(obj.getSize());
        }
        return totalSize;
    }
    private BigInteger calculateDesktopListSize( List<File> desktopMusic){
        BigInteger totalSize=BigInteger.valueOf(0);
        for(File obj:desktopMusic ){
            totalSize = totalSize.add(BigInteger.valueOf(obj.getTotalSpace()));
        }
        return totalSize;
    }*/

    static List<PortableDeviceObject> listFilesForFolder(PortableDeviceFolderObject androidPath) {
        ArrayList<PortableDeviceObject> allMusicFiles = new ArrayList<>();
        if(hasNoMedia(androidPath)) return allMusicFiles;
        allMusicFiles.addAll(Arrays.asList(androidPath.getChildObjects()));
        return allMusicFiles;
    }

    private static boolean hasNoMedia(PortableDeviceFolderObject object){
        for (PortableDeviceObject o2 : object.getChildObjects()) {
           if(o2 instanceof PortableDeviceFolderObject){
               PortableDeviceFolderObject storage = (PortableDeviceFolderObject) o2;
               return hasNoMedia(storage);
           }
            if(o2.getOriginalFileName().equalsIgnoreCase(".nomedia"))
                return true;
        }
        return false;
    }

    /*
     * Searches the files recursively ! Got be a problem when to many folders
     * 
     */
    @SuppressWarnings("ConstantConditions")
    static List<File> listFilesForFolder(File folder) {
        List<File> songs = new ArrayList<>();
        for ( File fileEntry : folder.listFiles()) { //for each file in the folder
            if (fileEntry.isDirectory()) { //another folder found, recursive call
                songs.addAll(listFilesForFolder(fileEntry));
            } 
            
           for (int i = 0; i < fileEndings.length; i++) //now i can add new FileTypes just by increasing the Array
           {  
        	   if (fileEntry.getName().endsWith(fileEndings[i])) {
        		   songs.add(fileEntry);
        	   }
              
           
           }
        }
        return songs;
    }
}
