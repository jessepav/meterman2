package com.illcode.meterman2.ui;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Window;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

final class WaitDialog
{
    Window owner;

    JPanel panel1;
    JDialog dialog;
    JLabel messageLabel;

    WaitDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/WaitDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            panel1 = cr.getPanel("panel1");
            messageLabel = cr.getLabel("messageLabel");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "WaitDialog()", ex);
        }
    }

    void show(String message) {
        messageLabel.setText(message);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        GuiUtils.repaintImmediately(panel1);
    }

    void hide() {
        dialog.setVisible(false);
    }

    void dispose() {
        dialog.dispose();
    }
}
