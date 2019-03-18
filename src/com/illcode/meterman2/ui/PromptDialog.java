package com.illcode.meterman2.ui;

import com.jformdesigner.model.FormModel;
import com.jformdesigner.runtime.FormCreator;
import com.jformdesigner.runtime.FormLoader;

import javax.swing.*;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import static com.illcode.meterman2.MMLogging.logger;

final class PromptDialog implements ActionListener
{
    Window owner;

    JDialog dialog;
    JLabel headerLabel;
    JTextArea textArea;
    JLabel promptLabel;
    JTextField textField;
    JButton okButton;

    PromptDialog(Window owner) {
        this.owner = owner;
        try {
            FormModel formModel = FormLoader.load("com/illcode/meterman2/ui/PromptDialog.jfd");
            FormCreator cr = new FormCreator(formModel);

            dialog = (JDialog) cr.createWindow(owner);
            headerLabel = cr.getLabel("headerLabel");
            textArea = cr.getTextArea("textArea");
            promptLabel = cr.getLabel("promptLabel");
            textField = cr.getTextField("textField");
            okButton = cr.getButton("okButton");

            okButton.addActionListener(this);
            textField.addActionListener(this);
        } catch (Exception ex) {
            logger.log(Level.WARNING, "PromptDialog()", ex);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == textField || source == okButton)
            dialog.setVisible(false);
    }

    String show(String header, String text, String prompt, String initialText) {
        headerLabel.setText(header);
        textArea.setText(text);
        promptLabel.setText(prompt);
        textField.setText(initialText);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        textField.requestFocusInWindow();
        dialog.setVisible(true);  // blocks until hidden
        return textField.getText();
    }

    public void dispose() {
        dialog.dispose();
    }
}
