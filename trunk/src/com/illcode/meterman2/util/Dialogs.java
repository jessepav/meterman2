package com.illcode.meterman2.util;

import com.illcode.meterman2.Meterman2;
import com.illcode.meterman2.Utils;
import com.illcode.meterman2.bundle.XBundle;
import com.illcode.meterman2.text.TextSource;
import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle the loading and display of dialogs from bundle elements.
 */
public final class Dialogs
{
    private static final int DIALOG_DELAY_MS = Math.max(0, Utils.intPref("dialog-delay-ms", 200));

    /**
     * Load a dialog passage, which may specify its own header string, button label, image, and scale.
     * @param id ID of the dialog passage element in the system bundle group
     * @return a DialogPassage instance, or null if it could not be loaded
     */
    public static DialogPassage loadDialogPassage(String id) {
        final Pair<Element, XBundle> pair = Meterman2.bundles.getElementAndBundle(id);
        if (pair == null)
            return null;
        else
            return loadDialogPassage(pair.getRight(), pair.getLeft());
    }

    /**
     * Load a dialog passage, which may specify its own header string, button label, image, and scale.
     * @param b the bundle containing the element
     * @param e the element with the dialog passage definition
     * @return a DialogPassage instance, or null if it could not be loaded
     */
    public static DialogPassage loadDialogPassage(XBundle b, Element e) {
        if (b == null || e == null)
            return null;
        final String header = e.getAttributeValue("header", "");
        final String button = e.getAttributeValue("button", "Okay");
        final String image = e.getAttributeValue("image");
        final int scale = Utils.parseInt(e.getAttributeValue("scale"), 1);
        return new DialogPassage(header, button, image, scale, b.elementTextSource(e));
    }

    /**
     * Load a sequence of dialog passages, each of which may specify its own
     * header string, button label, image, and scale.
     * @param id ID of the dialog sequence element in the system bundle group.
     * @return a list of DialogPassage instances, or null if the sequence could not be loaded
     */
    public static DialogSequence loadDialogSequence(String id) {
        final Pair<Element, XBundle> pair = Meterman2.bundles.getElementAndBundle(id);
        if (pair == null)
            return null;
        else
            return loadDialogSequence(pair.getRight(), pair.getLeft());
    }

    /**
     * Load a sequence of dialog passages, each of which may specify its own
     * header string, button label, image, and scale.
     * @param b the bundle containing the element
     * @param e the element with the sequence definition
     * @return a list of DialogPassage instances, or null if the sequence could not be loaded
     */
    public static DialogSequence loadDialogSequence(XBundle b, Element e) {
        if (b == null || e == null)
            return null;
        final String defaultHeader = e.getAttributeValue("defaultHeader", "");
        final String defaultButton = e.getAttributeValue("defaultButton", "Okay");
        final String defaultImage = e.getAttributeValue("defaultImage");
        final int defaultScale = Utils.parseInt(e.getAttributeValue("defaultScale"), 1);
        final boolean noEscape = Utils.parseBoolean(e.getAttributeValue("noEscape"));

        final List<Element> entries = e.getChildren();
        final List<DialogPassage> dialogs = new ArrayList<>(entries.size());
        for (Element item : entries) {
            final String header = item.getAttributeValue("header", defaultHeader);
            final String button = item.getAttributeValue("button", defaultButton);
            final String image = item.getAttributeValue("image", defaultImage);
            final int scale = Utils.parseInt(item.getAttributeValue("scale"), defaultScale);
            final TextSource text = b.elementTextSource(item);
            dialogs.add(new DialogPassage(header, button, image, scale, text));
        }
        return new DialogSequence(dialogs, noEscape);
    }

    /** Show a sequence of dialog passages. */
    public static void showDialogSequence(DialogSequence sequence) {
        if (sequence == null)
            return;
        boolean first = true;
        for (DialogPassage dialog : sequence.dialogs) {
            if (DIALOG_DELAY_MS != 0) {
                // sleeping a little avoids a jarring flicker as each dialog transitions to the next
                if (!first) Utils.sleep(DIALOG_DELAY_MS);
                else first = false;
            }
            if (dialog.show() == -1 && !sequence.noEscape)
                break;  // allow the user to interrupt the sequence
        }
    }

    /**
     * A passage that can be displayed in a UI dialog.
     */
    public static final class DialogPassage
    {
        final String header;
        final String button;
        final String image;
        final int scale;
        final TextSource text;

        public DialogPassage(String header, String button, String image, int scale, TextSource text) {
            this.header = header;
            this.button = button;
            this.image = image;
            this.scale = scale;
            this.text = text;
        }

        /**
         * Shows the dialog passage.
         * @return 0 if the user closed the dialog by clicking the button, or -1 if the dialog
         *         was closed without selecting a button.
         */
        public int show() {
            if (image == null || image.equals("none"))
                return Meterman2.ui.showTextDialog(header, text.getText(), button);
            else
                return Meterman2.ui.showImageDialog(header, image, scale, text.getText(), button);
        }
    }

    public static final class DialogSequence
    {
        final List<DialogPassage> dialogs;
        final boolean noEscape;

        public DialogSequence(List<DialogPassage> dialogs, boolean noEscape) {
            this.dialogs = dialogs;
            this.noEscape = noEscape;
        }
    }
}
