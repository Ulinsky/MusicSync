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

public class FileManager {

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

    public static PortableDevice getDevice(){
        PortableDeviceManager manager = new PortableDeviceManager();
        PortableDevice device;
        
        if (manager.getDevices().length == 0) {
            showMessageDialog(null, "No connected android device found.");
            return null;
        }
        else{
        	device = manager.getDevices()[0];
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

        PortableDeviceObject[] songs = musicFolder.getChildObjects();
       /* for (PortableDeviceObject song : songs) {
            copyFileFromDeviceToComputerFolder(song,device,desktopPath);
            }*/ //TODO EXPORT TO ANOTHER METHOD
        manager.getDevices()[0].close();
        return device.getFriendlyName() + "\\" + musicFolder.getParent().getName() + "\\" + musicFolder.getName(); //TODO make it NOT hardcoded
    }
    
    private static void copyFileFromComputerToDeviceFolder(PortableDeviceFolderObject targetFolder, String desktopPath){
                BigInteger bigInteger1 = new BigInteger("123456789");
                File file = new File(desktopPath);
                try {
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
    }

   static public List<PortableDeviceFolderObject> findMusicFolders(PortableDevice device) {
    	List<PortableDeviceFolderObject> allMusicFolders = new ArrayList<PortableDeviceFolderObject>();
        //Iterate through root folders
    	
    	ArrayList<PortableDeviceObject> storageObjects = new ArrayList<PortableDeviceObject>();
    	
    	
    	Arrays.stream(device.getRootObjects()).forEach((f)->{
    		Arrays.stream(((PortableDeviceStorageObject)f).getChildObjects()).forEach((s)->{
    			findMusicFolders(s, allMusicFolders);
    		});
    	});
//        Stream allFolders = Arrays.stream(array)
//        {
//           findMusicFolders(object, allMusicFolders);
//        }
        //No music folder was found
        return allMusicFolders;
    }
    
    private static List<PortableDeviceFolderObject> findMusicFolders(Object source, List<PortableDeviceFolderObject> folderList){
    	 
         if (source instanceof PortableDeviceFolderObject){//TODO storage.getName().equals("Music") || storage.getName().equals("music")) {
             //Found the music folder
        	 PortableDeviceFolderObject storage = (PortableDeviceFolderObject) source;
        	 
        	 if(storage.getOriginalFileName().equalsIgnoreCase("music") || storage.getOriginalFileName().equalsIgnoreCase("musik"))
        		 folderList.add(storage);
        	 //System.out.println("file: " + getFolderPath(storage));
           	 
        	 if(! hasNoMedia(storage)){
	             for (PortableDeviceObject o2 : storage.getChildObjects()) {
	            	 findMusicFolders(o2, folderList);
	             }
        	 }
           	 return folderList;
        }
             
        // If the object is a storage object
         else if (source instanceof PortableDeviceStorageObject) {
             PortableDeviceStorageObject storage = (PortableDeviceStorageObject) source;
             
             //Iterate through sub-folders
             for (PortableDeviceObject o2 : storage.getChildObjects()) {
            	 findMusicFolders(o2, folderList);
             }
         }
		return folderList;
    	
    }
    
    public static String getFolderPath(PortableDeviceObject object){
    	StringBuilder path = new StringBuilder();
    	
    	while(!(object instanceof PortableDeviceStorageObject)){
    		path.insert(0, object.getOriginalFileName());
    		path.insert(0, '/');
    		object = object.getParent();
    	}
    	path.insert(0, object.getName());
    	
    	return path.toString();
    }
    
    private static boolean hasNoMedia(PortableDeviceFolderObject object){
    	for (PortableDeviceObject o2 : object.getChildObjects()) {
       		if(o2.getOriginalFileName().equalsIgnoreCase(".nomedia"))
       			return true;
        }
    	return false;
    }

	public static void sync(String desktopPath, PortableDeviceFolderObject androidPath, PortableDevice device) {

		System.out.println("syncing desktop");
		List<File> desktopMusic = listFilesForFolder(new File(desktopPath));
		List<PortableDeviceObject> androidMusic = listFilesForFolder(androidPath);
		
		Stream<PortableDeviceObject> desktopMissingMusic;
		Stream<String> androidMissingMusic;

    	Long time2 = System.nanoTime();
		androidMissingMusic = desktopMusic.stream().filter((d)->{
			for(PortableDeviceObject a : androidMusic){
				if(a.getOriginalFileName().equalsIgnoreCase(d.getName()))
					return false;
			}
			return true;
		}).map(d -> {
			return d.getAbsolutePath();
		});
		
		desktopMissingMusic = androidMusic.stream().filter((a)->{
			for(File d : desktopMusic){
				if(a.getOriginalFileName().equalsIgnoreCase(d.getName()))
					return false;
			}
			return true;
		}).map(d -> {
			return d;
		});
        System.out.println(System.nanoTime() - time2);
		
//		boolean contained = false;
//		for(File f : desktopMusic){
//			contained = false;
//			for(PortableDeviceObject p : androidMusic){
//				if(p.getOriginalFileName().equalsIgnoreCase(f.getName())){
//					contained = true;
//					break;
//				}
//				
//			}
//				if(! contained)
//					androidMissingMusic.add(f.getAbsolutePath());
//		}
//		
//		for(PortableDeviceObject p : androidMusic){
//			contained = false;
//			for(File f : desktopMusic){
//				if(p.getOriginalFileName().equalsIgnoreCase(f.getName())){
//					contained = true;
//					break;
//				}
//			}
//				if(! contained)
//					desktopMissingMusic.add(p);
//		}

    	time2 = System.nanoTime();
		androidMissingMusic.forEach((a)->{
			copyFileFromComputerToDeviceFolder(androidPath, a);
			System.out.println("copying to device: " + a);});
		
		desktopMissingMusic.forEach((a)->{
			copyFileFromDeviceToComputerFolder(a, device, desktopPath);
			System.out.println("copying to computer: " + getFolderPath(a));});
        System.out.println(System.nanoTime() - time2);
		
	}

	private static List<PortableDeviceObject> listFilesForFolder(PortableDeviceFolderObject androidPath) {
    	ArrayList<PortableDeviceObject> allMusicFiles = new ArrayList<PortableDeviceObject>();
    	
		for (PortableDeviceObject o2 : androidPath.getChildObjects()) {
			allMusicFiles.add(o2);
        }
		return allMusicFiles;
	}
}
