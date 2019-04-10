package com.illcode.meterman2.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.illcode.meterman2.Utils;
import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

final class ChooseFontsDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JButton buttonButton, dialogButton, headerButton, labelButton, listButton, mainButton;
    JLabel buttonLabel, dialogLabel, headerLabel, labelLabel, listLabel, mainLabel;
    JButton okButton, cancelButton;

    private JButton[] chooseButtons;
    private JLabel[] labels;
    private String[] prefs;
    private static final int NUM_CONTROLS = 6;

    private FontChooser fontChooser;

    private boolean okButtonPressed;

    ChooseFontsDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/ChooseFontsDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            buttonButton = cr.getButton("buttonButton");
            dialogButton = cr.getButton("dialogButton");
            headerButton = cr.getButton("headerButton");
            labelButton = cr.getButton("labelButton");
            listButton = cr.getButton("listButton");
            mainButton = cr.getButton("mainButton");
            buttonLabel = cr.getLabel("buttonLabel");
            dialogLabel = cr.getLabel("dialogLabel");
            headerLabel = cr.getLabel("headerLabel");
            labelLabel = cr.getLabel("labelLabel");
            listLabel = cr.getLabel("listLabel");
            mainLabel = cr.getLabel("mainLabel");
            okButton = cr.getButton("okButton");
            cancelButton = cr.getButton("cancelButton");

            chooseButtons = new JButton[] {mainButton, headerButton, listButton, labelButton, buttonButton, dialogButton};
            labels = new JLabel[] {mainLabel, headerLabel, listLabel, labelLabel, buttonLabel, dialogLabel};
            prefs = new String[] {"main-text-font", "header-font", "list-font", "label-font", "button-font", "dialog-text-font"};

            for (JButton b : chooseButtons)
                b.addActionListener(this);
            okButton.addActionListener(this);
            cancelButton.addActionListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "ChooseFontsDialog()", ex);
        }
    }

    /** Return true if user pressed OK and font preferences were updated. */
    boolean show() {
        for (int i = 0; i < NUM_CONTROLS; i++) {
            final String fontStr = Utils.getPref(prefs[i]);
            labels[i].setText(fontStr);
            labels[i].setFont(Font.decode(fontStr));
        }
        dialog.pack();
        okButtonPressed = false;
        dialog.setVisible(true);   // blocks until hidden
        return okButtonPressed;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        int idx;
        if (source == okButton) {
            okButtonPressed = true;
            for (int i = 0; i < NUM_CONTROLS; i++)
                Utils.setPref(prefs[i], labels[i].getText());
            dialog.setVisible(false);
        } else if (source == cancelButton) {
            dialog.setVisible(false);
        } else if ((idx = ArrayUtils.indexOf(chooseButtons, source)) != -1) {
            if (fontChooser == null)
                fontChooser = new FontChooser(new String[] {"8", "9", "10", "11", "12", "13", "14", "15", "16", "18", "20", "24"});
            fontChooser.setSelectedFont(Font.decode(labels[idx].getText()));
            if (fontChooser.showDialog(dialog) == FontChooser.OK_OPTION) {
                final Font font = fontChooser.getSelectedFont();
                labels[idx].setText(GuiUtils.fontString(font));
                labels[idx].setFont(font);
            }
        }
    }
}
