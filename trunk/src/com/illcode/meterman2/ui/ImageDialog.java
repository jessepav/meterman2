package com.illcode.meterman2.ui;

import com.illcode.meterman2.Utils;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

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
    JButton[] buttons;
    private int selectedButtonIdx;

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
            buttons = new JButton[3];
            buttons[0] = cr.getButton("button1");
            buttons[1] = cr.getButton("button2");
            buttons[2] = cr.getButton("button3");

            imageIcon = new ImageIcon();
            imageLabel.setIcon(imageIcon);

            for (JButton b : buttons)
                b.addActionListener(this);

            emptyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "ImageDialog()", ex);
        }
    }

    /**
     * Show the ImageDialog. If 'image' == null, no image is displayed.
     * @see MMUI#showImageDialog(String, String, int, String, String)
     */
    int show(String header, BufferedImage image, String text, String... buttonLabels) {
        headerLabel.setText(header);
        imageIcon.setImage(image == null ? emptyImage : image);
        textArea.setText(text);
        setButtonsText(buttonLabels);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        requestButtonFocus();
        selectedButtonIdx = -1;
        dialog.setVisible(true);  // blocks until hidden
        imageIcon.setImage(emptyImage);  // allow 'image' to be GC'd
        return selectedButtonIdx;
    }

    private void setButtonsText(String... labels) {
        if (labels == null)
            labels = Utils.EMPTY_STRING_ARRAY;
        for (int i = 0; i < buttons.length; i++) {
            String label = labels.length > i ? labels[i] : null;
            if (label != null) {
                buttons[i].setText(labels[i]);
                buttons[i].setVisible(true);
            } else {
                buttons[i].setVisible(false);
            }
        }
    }

    // Have the first visible button request focus in the window.
    private void requestButtonFocus() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].isVisible()) {
                buttons[i].requestFocusInWindow();
                dialog.getRootPane().setDefaultButton(buttons[i]);
                break;
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int buttonIdx;
        if ((buttonIdx = ArrayUtils.indexOf(buttons, source)) != -1) {
            selectedButtonIdx = buttonIdx;
            dialog.setVisible(false);
        }
    }

    public void dispose() {
        imageLabel.setIcon(null);
        dialog.dispose();
    }

}
