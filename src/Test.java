import sun.applet.Main;

import javax.swing.*;
import java.io.File;

public class Test {
    public static void main(String[] args) {
       /* JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
       File[] files =  fileChooser.getSelectedFiles();

       for(File f : files){
           System.out.println(f.getName());
       }*/

        MainPanel.showGui();

    }
}
