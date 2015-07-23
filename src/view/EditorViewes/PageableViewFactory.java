//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view.EditorViewes;

import java.awt.Insets;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import rtf.view.RTFViewFactory;

/**
 * The view factory class creates custom views for pagination
 * root view (SectionView class) and paragraph (PageableParagraphView class)
 */
public class PageableViewFactory extends RTFViewFactory{
    /**
     * Šířka stránky
     */
    public static final int PAGE_WIDTH = 798;
    
    /**
     * Výška jedné stránky
     */
    public static final int PAGE_HEIGHT = 1129;
    
    /**
     * Odsazení obsahu od okrajů stránky.
     */
    public static final int PAGE_INSET = 18;
    
    /**
     * Šířka pole okolo stránky.
     */
    public static final Insets PAGE_MARGINS = new Insets(50, 50, 50, 50);

    /**
     * Creates view for specified element.
     * @param elem Element parent element
     * @return View created view instance.
     */
    @Override
    public View create(Element elem){
        String kind = elem.getName();
        if (kind != null) {
            switch (kind) {
                case AbstractDocument.ContentElementName:
                    return new WrapLabelView(elem);
                case AbstractDocument.ParagraphElementName:
                    return new PageableParagraphView(elem);
                case AbstractDocument.SectionElementName:
                    return new SectionView(elem, View.Y_AXIS);
                case StyleConstants.ComponentElementName:
                    return super.create(elem);
                case StyleConstants.IconElementName:
                    return new IconMyView(elem);
            }
        }
        return super.create(elem);
    }
}
