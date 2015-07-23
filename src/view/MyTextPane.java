//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import model.MyDocument;
import view.EditorViewes.IconMyView;

/**
 * Třída představuje editovací okno.
 * @author Jan Brzobohatý
 */
public class MyTextPane extends JTextPane{
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Poslední pozice kurzoru. 
     */
    private int lastCaretPosition;
    
    /**
     * Indikátor, zda se má kontrolovat pozice kurzoru. (vzhledem k obrázku)
     */
    private boolean enableCheckingCursor = true;
    
    /**
     * Atributy právě vybraného textu. (nebo polohy kurzoru v případě žádné selekce)
     */
    private AttributeSet selectedAtrrs = new SimpleAttributeSet();
    
    /**
     * Indikátor, zda se má kontrolovat podbarvení obrázku.
     */
    private boolean enableCheckingHighlight = true;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /************************************************************************************************
    Deklarace kontruktorů a továrních metod.
    ************************************************************************************************/
    
    MyTextPane(){
        super();
        setHighlighter( new CustomHighlighter());
        
        MyCaret c=new MyCaret();
        c.setBlinkRate(getCaret().getBlinkRate());
        setCaret(c);
    }
    
    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * Zkontroluje, zda se kurzor nenechází před obrázkem a případně upraví jeho polohu.
     * @param startPosition pozice začátku selekce kurzoru
     * @param endPosition pozice konce selekce kurzoru
     * @throws BadLocationException v případě, že zvolené pozice v dokumentu neexistují
     */
    public void checkCursor(int startPosition, int endPosition) throws BadLocationException{
        enableCheckingHighlight = true;
        
        //nepřesouvat kurzor, pokud je označen přímo samotný obrázek
        IconMyView view = IconMyView.getCurrentImageView();
        if(view!=null && view.hasFocus()){
            return;
        }
        
        if(enableCheckingCursor){
            //směr pohybu
            int direction = 1;
            //nejsme na začátku dokumentu, předchozí znak je znak nového řádku, předchozí znak není obrázek, současný znak je obrázek
            if(endPosition!=0 && getDocument().getText(endPosition-1, 1).contains("\n") && !((StyledDocument)getDocument()).getCharacterElement(endPosition-1).getName().equals("icon") && ((StyledDocument)getDocument()).getCharacterElement(endPosition).getName().equals("icon")){
                if(endPosition<lastCaretPosition){
                   direction = -1;
                }
                enableCheckingCursor = false;
                if(startPosition!=endPosition){
                    getCaret().moveDot(endPosition+direction);
                    enableCheckingHighlight = false;
                }else{
                    setCaretPosition(endPosition+direction);
                }
                enableCheckingCursor = true;
                return;
            }
        }
        lastCaretPosition = endPosition;
    }
    
    /**
     * Nastaví indikátor poslední pozice tak, aby se následná korekce pozice chovala správně. 
     */
    public void setCaretWasUsedForEdit(){
        lastCaretPosition = getDocument().getLength();
    }
    
    /**
     * Vloží daný obrázek do dokumentu na pozici kurzoru.
     * @param g obrázek
     */
    @Override
    public void insertIcon(Icon g) {
        replaceSelection("");
        ((MyDocument)getDocument()).insertPicture((ImageIcon) g, getSelectionStart());
    }
    
    /**
     * Aktualizuje atributy právě zvoleného textu nebo pozice kurzoru v případě nulové selekce.
     * @throws BadLocationException v případě neexistující pozice v dokumentu
     */
    public void updateInputOrSelectedAttributes() throws BadLocationException{
        int dot = getCaret().getDot();
        int mark = getCaret().getMark();
        
        if(dot==mark){
            updateInputAttributess(dot);
        }else{
            if(dot<mark){
                updateSelectedAttributes(dot, mark);
            }else{
                updateSelectedAttributes(mark, dot);
            }
        }
    }
    
    /**
     * Změní atributy vybraného řetězce na vstupní atributy editoru (žádný řetězec není vybrán).
     * (Tato metoda nesmí být volána z CaretListeneru, jinak budou atributy odpovídat oposlední a ne současné poloze kuzoru.)
     */
    public void updateSelectedAttributesAfterChanges(){
        selectedAtrrs = getInputAttributes();
    }
    
    /**
     * @return atributy právě označeného textu nebo polohy kurzoru 
     */
    public AttributeSet getSelectedAtrributes() {
        return selectedAtrrs;
    }
    
    /**
     * Applies the given attributes to character
     * content.  If there is a selection, the attributes
     * are applied to the selection range.  If there
     * is no selection, the attributes are applied to
     * the input attribute set which defines the attributes
     * for any new text that gets inserted.
     *
     * @param attr the attributes
     * @param replace if true, then replace the existing attributes first
     */
    @Override
    public void setCharacterAttributes(AttributeSet attr, boolean replace) {
        int p0 = getSelectionStart();
        int p1 = getSelectionEnd();
        StyledDocument doc = getStyledDocument();
        
        //přidělení stejných atrributů enteru za textem
        try {
            if(doc.getText(p1, 1).contains("\n")){
                doc.setCharacterAttributes(p1, 1, attr, replace);
            }
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }

        //tohle je standartní součást metody
        if (p0 != p1) {
            doc.setCharacterAttributes(p0, p1 - p0, attr, replace);
        } else {
            MutableAttributeSet inputAttributes = getInputAttributes();
            if (replace) {
                inputAttributes.removeAttributes(inputAttributes);
            }
            inputAttributes.addAttributes(attr);
        }
    }
    
    /**
     * Zapne nebo vypne kontrolování pozice kurzoru.
     * @param enableCheckingCursor 
     */
    public void enableCheckingCursor(boolean enableCheckingCursor){
        this.enableCheckingCursor = enableCheckingCursor;
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Aktualizuje atributy vybraného textu.
     * @param pos pozice kurzoru v dokumentu
     * @throws BadLocationException v případě neexistující pozice v dokumentu
     */
    private void updateSelectedAttributes(int firstCharIndex, int lastCharIndex) throws BadLocationException{
        boolean bold, firstBold;
        String font, firstFont;
        int fontSize, firstSize;
        int align, firstAlign;
        AttributeSet attrs;
        Element element;
        StyledDocument document = (StyledDocument) getDocument();        
        
        //ignorovat označené prázdné řádky na začátku označení
        while(firstCharIndex<lastCharIndex && document.getText(firstCharIndex, 1).contains("\n") && !document.getCharacterElement(firstCharIndex).getName().equals("icon")){
            firstCharIndex++;
        }
        
        //zapamatování si atributů prvního znaku
        element  = document.getCharacterElement(firstCharIndex);
        attrs = element.getAttributes();
        bold = firstBold = StyleConstants.isBold(attrs);
        font = firstFont = StyleConstants.getFontFamily(attrs);
        fontSize = firstSize = StyleConstants.getFontSize(attrs);
        align = firstAlign = StyleConstants.getAlignment(element.getParentElement().getAttributes());
        
        if(!(lastCharIndex-firstCharIndex==1 && element.getName().equals("icon"))){
            //zjištění, zda je celý označený text stejného typu
            for(int i=element.getEndOffset(); i <= lastCharIndex; i+=element.getEndOffset()-element.getStartOffset()){
                element = document.getCharacterElement(i);

                //přeskočit konce řádků
                if((element.getEndOffset()-element.getStartOffset())==1 && document.getText(i, 1).contains("\n")){
                    if(firstAlign!=StyleConstants.getAlignment(element.getParentElement().getAttributes())){
                        align = -1;
                    }
                    continue;
                }

                attrs = element.getAttributes();
                if(firstBold!=StyleConstants.isBold(attrs)){
                    bold = false;
                }
                if(!firstFont.equals(StyleConstants.getFontFamily(attrs))){
                    font = "";
                }
                if(firstSize!=StyleConstants.getFontSize(attrs)){
                    fontSize = 0;
                }
                if(firstAlign!=StyleConstants.getAlignment(element.getParentElement().getAttributes())){
                    align = -1;
                }
            }
        }
        
        
        MutableAttributeSet selectedAtrrsTemp = new SimpleAttributeSet();
        StyleConstants.setBold(selectedAtrrsTemp, bold);
        StyleConstants.setFontFamily(selectedAtrrsTemp, font);
        StyleConstants.setFontSize(selectedAtrrsTemp, fontSize);
        StyleConstants.setAlignment(selectedAtrrsTemp, align);
        selectedAtrrs = selectedAtrrsTemp;
    }
    
    /**
     * Aktualizuje atributy vybraného textu podle vstupních atributů editoru na dané pozici kurzoru.
     * @param pos pozice kurzoru v dokumentu
     * @throws BadLocationException v případě neexistující pozice v dokumentu
     */
    private void updateInputAttributess(int pos) throws BadLocationException{
        StyledDocument document = (StyledDocument) getDocument();
        if(pos<=0){
            //pokud jsme na začátku dokumentu, tak vrať atributy prvního znaku
            selectedAtrrs = document.getCharacterElement(pos).getAttributes();
        }else if(document.getText(pos-1, 1).contains("\n")){
            //pokud jsme na začátku řádku
            if(document.getText(pos, 1).contains("\n")){
                //a k tomu je řádek prázdný, tak vem atributy posledního znaku z minulého řádku
                selectedAtrrs = document.getCharacterElement(pos).getAttributes();
            }else{
                //a řádek není prázdný, tak vem atributy prvního znaku v řádku
                selectedAtrrs = document.getCharacterElement(pos).getAttributes();
            }
        }else{
            //jsme někde uvnitř řádky
            selectedAtrrs = document.getCharacterElement(pos-1).getAttributes();
        }
        
        //zarovnání odstavce
        MutableAttributeSet selectedAtrrsTemp = new SimpleAttributeSet(selectedAtrrs);
        StyleConstants.setBold(selectedAtrrsTemp, StyleConstants.isBold(selectedAtrrs));
        StyleConstants.setFontFamily(selectedAtrrsTemp, StyleConstants.getFontFamily(selectedAtrrs));
        StyleConstants.setFontSize(selectedAtrrsTemp, StyleConstants.getFontSize(selectedAtrrs));
        StyleConstants.setAlignment(selectedAtrrsTemp, StyleConstants.ParagraphConstants.getAlignment(document.getParagraphElement(pos).getAttributes()));
        selectedAtrrs = selectedAtrrsTemp;
    }
    
    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Třída představuje podbarvovač textu při selekci.
     * Tento se konkrétně postará i o podbarvení obrázku.
     */
    private final class CustomHighlighter extends DefaultHighlighter {

        @Override
        public Object addHighlight( int p0, int p1, HighlightPainter p ) throws BadLocationException {
            Object tag = super.addHighlight(p0, p1, p);
            if(enableCheckingHighlight){
                checkImageSelection(p0, p1);
            }
            return tag;
        }

        @Override
        public void removeHighlight( Object tag ) {
            super.removeHighlight(tag);
            IconMyView.clearAllSelection(); 
            repaint();
        }

        @Override
        public void removeAllHighlights() { 
            super.removeAllHighlights();
            IconMyView.clearAllSelection();
            repaint();
        }

        @Override
        public void changeHighlight( Object tag, int p0, int p1 ) throws BadLocationException {
            super.changeHighlight(tag, p0, p1);
            if(enableCheckingHighlight){
                checkImageSelection(p0, p1);
            } 
        }

        /**
         * Projde všechny elementy v selekci a pokud se tam nachází obrázek, tak ho podbarví.
         * @param p0 začínající pozice selekce
         * @param p1 konečná pozice selekce
         * @throws BadLocationException 
         */
        private void checkImageSelection(int p0, int p1) throws BadLocationException{
            IconMyView.clearAllSelection();

            Element element  = getStyledDocument().getCharacterElement(p0);
            AttributeSet attrs = element.getAttributes();
            if(StyleConstants.getIcon(attrs)!=null){
                modelToView(p0);
                IconMyView.getCurrentImageView().setSelected(true);
            }
            for(int i=element.getEndOffset(); i < p1; i+=element.getEndOffset()-element.getStartOffset()){
                element = getStyledDocument().getCharacterElement(i);
                attrs = element.getAttributes();
                if(StyleConstants.getIcon(attrs)!=null){
                    modelToView(i);
                    IconMyView.getCurrentImageView().setSelected(true);
                }
            }
            repaint();
        }
    }
    
    /**
     * Caret editoru
     */
    public static class MyCaret extends DefaultCaret {
        /**
         * Hodnota, která se odečte od šířky vyhrazené pro caret.
         */
        int corection;

        /**
         * Nastaví caretu nulovou šířku, takže bude vidět, ale nebude nikde dělat žádné bílé pruhy.
         * @param corection 
         */
        public void setInvisible(boolean corection) {
            if(corection){
                this.corection = 9;
            }else{
                this.corection = 0;
            }
        }
        
        @Override
        protected synchronized void damage(Rectangle r) {
            if (r != null) {
                x = r.x - 4;
                y = r.y;
                width = 9 - corection;
                height = r.height;
                repaint();
            }
        }
    }
}
