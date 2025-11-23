package fr.tp.inf112.projects.robotsim.persistence;

import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RemoteFileCanvasChooser extends FileCanvasChooser {

    private RemoteFactoryPersistenceManager remoteManager;

    public RemoteFileCanvasChooser(String fileExtension, String documentTypeLabel) {
        super(fileExtension, documentTypeLabel);
    }

    public RemoteFileCanvasChooser(Component viewer, String fileExtension, String documentTypeLabel) {
        super(viewer, fileExtension, documentTypeLabel);
    }

    protected void setRemoteManager(RemoteFactoryPersistenceManager manager){
        this.remoteManager = manager;
    }

    @Override
    protected String browseCanvases(boolean open) {
        if(open){
            if(this.remoteManager != null){
                List<String> existingFiles = this.remoteManager.getExistingSavedCanvas();
                return (String) JOptionPane.showInputDialog(null, "Choose", "Choose", JOptionPane.PLAIN_MESSAGE, null, existingFiles.toArray(), existingFiles.get(0));
            }
        }
        return (String) JOptionPane.showInputDialog("Name this factory");
    }
}
