/*
 * Autopsy Forensic Browser
 *
 * Copyright 2011-2013 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.corecomponents;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.sleuthkit.autopsy.coreutils.Logger;
import javax.swing.SwingUtilities;
import org.openide.nodes.Node;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.sleuthkit.autopsy.corecomponentinterfaces.DataContentViewer;
import org.sleuthkit.datamodel.AbstractFile;
import org.sleuthkit.datamodel.TskData.TSK_FS_NAME_FLAG_ENUM;

/**
 * Media content viewer for videos, sounds and images.
 */
@ServiceProviders(value = {
    @ServiceProvider(service = DataContentViewer.class, position = 5)
})
public class DataContentViewerMedia extends javax.swing.JPanel implements DataContentViewer {

    private String[] IMAGES; // use javafx supported 
    private static final String[] VIDEOS = new String[]{".mov", ".m4v", ".flv", ".mp4", ".3gp", ".avi", ".mpg", ".mpeg", ".wmv"};
    private static final String[] AUDIOS = new String[]{".mp3", ".wav", ".wma"};

    private static final Logger logger = Logger.getLogger(DataContentViewerMedia.class.getName());

    private AbstractFile lastFile;
    //UI
    private final MediaViewVideoPanel videoPanel;
    private final MediaViewImagePanel imagePanel;
    private boolean videoPanelInited;
    private boolean imagePanelInited;
    
    private static final String IMAGE_VIEWER_LAYER = "IMAGE";
    private static final String VIDEO_VIEWER_LAYER = "VIDEO";

    /**
     * Creates new form DataContentViewerVideo
     */
    public DataContentViewerMedia() {

        initComponents();
        
        videoPanel = new MediaViewVideoPanel();
        imagePanel = new MediaViewImagePanel();
        videoPanelInited = videoPanel.isInited();
        imagePanelInited = imagePanel.isInited();
        
        customizeComponents();
        logger.log(Level.INFO, "Created MediaView instance: " + this);
    }

    private void customizeComponents() {
        logger.log(Level.INFO, "Supported image formats by javafx image viewer: ");
        //initialize supported image types
        //TODO use mime-types instead once we have support
        String[] fxSupportedImagesSuffixes = ImageIO.getReaderFileSuffixes();
        IMAGES = new String[fxSupportedImagesSuffixes.length];
        for (int i = 0; i < fxSupportedImagesSuffixes.length; ++i) {
            String suffix = fxSupportedImagesSuffixes[i];
            logger.log(Level.INFO, "suffix: " + suffix);
            IMAGES[i] = "." + suffix;
        }

        add(imagePanel, IMAGE_VIEWER_LAYER);
        add(videoPanel, VIDEO_VIEWER_LAYER);
        
        switchPanels(false);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.CardLayout());
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DataContentViewerMedia.class, "DataContentViewerMedia.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public void setNode(Node selectedNode) {
        
        if (selectedNode == null) {
             videoPanel.reset();
            return;
        }

        AbstractFile file = selectedNode.getLookup().lookup(AbstractFile.class);
        if (file == null) {
            return;
        }

        if (file.equals(lastFile)) {
            return; //prevent from loading twice if setNode() called mult. times
        } else {
            lastFile = file;
        }
        
                  videoPanel.reset();
        
        final Dimension dims = DataContentViewerMedia.this.getSize();
        
        if (imagePanelInited && containsExt(file.getName(), IMAGES)) {
            imagePanel.showImageFx(file, dims);
                        this.switchPanels(false);
        } else if (videoPanelInited
                && (containsExt(file.getName(), VIDEOS) || containsExt(file.getName(), AUDIOS))) {
     
            videoPanel.setupVideo(file, dims);
                   this.switchPanels(true);
        }
    }

    /**
     * switch to visible video or image panel
     *
     * @param showVideo true if video panel, false if image panel
     */
    private void switchPanels(boolean showVideo) {
        CardLayout layout = (CardLayout)this.getLayout();
        if (showVideo) {
            layout.show(this, VIDEO_VIEWER_LAYER);
        } else {
            layout.show(this, IMAGE_VIEWER_LAYER);
        }
    }

    @Override
    public String getTitle() {
        return "Media View";
    }

    @Override
    public String getToolTip() {
        return "Displays supported multimedia files (images, videos, audio)";
    }

    @Override
    public DataContentViewer createInstance() {
        return new DataContentViewerMedia();
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public void resetComponent() {
        // we don't want this to do anything
        // because we already reset on each selected node
    }

  

    @Override
    public boolean isSupported(Node node) {

        if (node == null) {
            return false;
        }
        
        AbstractFile file = node.getLookup().lookup(AbstractFile.class);
        if (file == null) {
            return false;
        }


        if (file.getSize() == 0) {
            return false;
        }

        String name = file.getName().toLowerCase();

        if (imagePanelInited && containsExt(name, IMAGES)) {
            return true;
        } //for gstreamer formats, check if initialized first, then
        //support audio formats, and video formats
        else if (videoPanelInited && videoPanel.isInited()
                && (containsExt(name, AUDIOS)
                || (containsExt(name, VIDEOS)))) {
            return true;
        }

        return false;
    }

    @Override
    public int isPreferred(Node node, boolean isSupported) {
        if (isSupported) {
            //special case, check if deleted video, then do not make it preferred
            AbstractFile file = node.getLookup().lookup(AbstractFile.class);
            if (file == null) {
                return 0;
            }
            String name = file.getName().toLowerCase();

            boolean deleted = file.isDirNameFlagSet(TSK_FS_NAME_FLAG_ENUM.UNALLOC);
            if (containsExt(name, VIDEOS) && deleted) {
                return 0;
            } else {
                return 7;
            }
        } else {
            return 0;
        }
    }

    private static boolean containsExt(String name, String[] exts) {
        int extStart = name.lastIndexOf(".");
        String ext = "";
        if (extStart != -1) {
            ext = name.substring(extStart, name.length()).toLowerCase();
        }
        return Arrays.asList(exts).contains(ext);
    }
}

  
   
    

       



