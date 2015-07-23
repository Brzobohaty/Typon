//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.ViewFactory;
import rtf.AdvancedRTFEditorKit;

/**
 * Třída představuje základní funkce editovacího okna.
 */
class MyEditorKit extends AdvancedRTFEditorKit{
    private final ViewFactory factory;
    
    MyEditorKit(ViewFactory viewFactory) {
        super();
        this.factory = viewFactory;
    }
    
    @Override
    public ViewFactory getViewFactory() {
        return factory;
    }

    @Override
    public Document createDefaultDocument() {
        return new MyDocument();
    }
    
    @Override
    public void read(InputStream in, Document doc, int pos) throws IOException, BadLocationException {
        ((MyDocument)doc).setReadingInProgress(true);
        super.read(in, doc, pos);
        ((MyDocument)doc).setReadingInProgress(false);
        ((MyDocument)doc).correctDocument();
    }

    @Override
    public void read(Reader in, Document doc, int pos) throws IOException, BadLocationException {
        ((MyDocument)doc).setReadingInProgress(true);
        super.read(in, doc, pos);
        ((MyDocument)doc).setReadingInProgress(false);
        ((MyDocument)doc).correctDocument();
    }    
}
