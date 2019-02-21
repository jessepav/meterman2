package com.illcode.meterman2.ui;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

class TextDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JTextArea textArea;
    JButton pageButton;

    TextDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/TextDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            textArea = cr.getTextArea("textArea");
            pageButton = cr.getButton("pageButton");

            pageButton.addActionListener(this);
            dialog.getRootPane().setDefaultButton(pageButton);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "TextDialog()", ex);
        }
    }

    void show(String header, String text, String buttonLabel) {
        headerLabel.setText(header);
        textArea.setText(text);
        pageButton.setText(buttonLabel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        pageButton.requestFocusInWindow();
        dialog.setVisible(true);  // blocks until hidden
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == pageButton)
            dialog.setVisible(false);
    }

    public void dispose() {
        dialog.dispose();
    }
}
