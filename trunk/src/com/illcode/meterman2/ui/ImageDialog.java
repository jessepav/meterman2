package com.illcode.meterman2.ui;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.*;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

public class ImageDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JLabel imageLabel;
    JTextArea textArea;
    JButton closeButton;

    ImageIcon imageIcon;
    BufferedImage emptyImage;

    public ImageDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/ImageDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            imageLabel = cr.getLabel("imageLabel");
            textArea = cr.getTextArea("textArea");
            closeButton = cr.getButton("closeButton");

            imageIcon = new ImageIcon();
            imageLabel.setIcon(imageIcon);

            closeButton.addActionListener(this);
            dialog.getRootPane().setDefaultButton(closeButton);

            emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "ImageDialog()", ex);
        }
    }

    /**
     * Show the ImageDialog. If 'image' == null, no image is displayed.
     * @see MMUI#showImageDialog(String, String, int, String, String)
     */
    void show(String header, BufferedImage image, String text, String buttonLabel) {
        headerLabel.setText(header);
        imageIcon.setImage(image == null ? emptyImage : image);
        textArea.setText(text);
        closeButton.setText(buttonLabel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        closeButton.requestFocusInWindow();
        dialog.setVisible(true);  // blocks until hidden
        imageIcon.setImage(emptyImage);  // allow 'image' to be GC'd
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == closeButton)
            dialog.setVisible(false);
    }

    public void dispose() {
        imageLabel.setIcon(null);
        dialog.dispose();
    }

}
