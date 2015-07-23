//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import rtf.AdvancedRTFDocument;

/**
 * Třída představuje model dat, představující obsah editoru.
 * @author Jan Brzobohatý
 */
public class MyDocument extends AdvancedRTFDocument{
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Indikátor, že naposledy byl přidán obrázek.
     */
    private boolean lastWasPicture = false;
    
    /**
     * Indikátor, že právě probíhá čtení ze schránky nebo ze souboru. 
     */
    private boolean readingInProgress = false;
    
    /**
     * Seznam pozic znaků, které mají po čtení odstraněny.
     */
    private final ArrayList<Integer> uncorrectPositions = new ArrayList();
    
    /**
     * Indikátor, zda byla schránka změněna samotným editorem.
     */
    private boolean clipBoardWasChangedByThisApp = false;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    public MyDocument() {
        super();
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    public void setReadingInProgress(boolean readingInProgress) {
        this.readingInProgress = readingInProgress;
    }
    
    @Override
    public Font getFont(AttributeSet attr){
        StyleContext styles = (StyleContext) getAttributeContext();
        MutableAttributeSet attrs = new SimpleAttributeSet(attr);
        //Zvětšujeme velikost fontu, aby vypadal jako v ostatních editorech.
        //StyleConstants.setFontSize(attrs, (StyleConstants.getFontSize(attr)*96/72));
        StyleConstants.setFontSize(attrs, (StyleConstants.getFontSize(attr)*100/70));
        return styles.getFont(attrs);
    }

    /**
     * Vloží obrázek do dokumentu na danou pozici.
     * @param icon obrázek
     * @param pos pozice v dokumentu
     */
    @Override
    public void insertPicture(ImageIcon icon, int pos) {
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setIcon(attr, icon);
        try {
            insertPictureString(pos, attr);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Smaže řetězec na dané pozici a dané délky v dokumentu.
     * Je zahrnuta i politika mazání okolo obrázku.
     * @param offset pozice mazané části v dokumentu
     * @param length délka mazané části v dokumentu
     * @throws BadLocationException v případě, že daná pozice v dokumentu neexistuje
     */
    @Override
    public void remove(int offset, int length) throws BadLocationException{
        MyUndoManager.getUndoManager().setCurrentRemoveValue(getText(offset, 1));
        
        //jedna se o mazani jednoho znaku
        if(length==1){
            //jedna se o znak enteru a zároveň je dalším znakem obrázek
            if(getText(offset, 1).contains("\n") && getCharacterElement(offset+1).getName().equals("icon")){
                //a zároveň se nejedná o obrázek
                if(!getCharacterElement(offset).getName().equals("icon")){
                    correctlyRemovePicture(offset);
                    return;
                }
            }
            //jedná se o obrázek
            //tohle je ošetření případu, kdy je zvětšován obrázek (je smazán a nahrezen jiným)
            //v tu chvíli, ale odstavec za ním dostane na chvilku mezi smazání a přidáním jeho atributy
            if(getCharacterElement(offset).getName().equals("icon")){
                AttributeSet attr = getParagraphElement(offset+1).getAttributes();
                super.remove(offset, 1);
                setParagraphAttributes(offset, 1, attr, false);
                return;
            }
        }
        
        //jedná se o selekci více znaků, další znak za selekcí je obrázek
        if(length>1 && getCharacterElement(offset+length).getName().equals("icon")){
            correctlyRemoveSelectionBeforePicture(offset, length);
            return;
        }
        
        //dalším znakem je enter a předchozím znakem není enter
        if(getLength()>0 && getText(offset+length, 1).contains("\n") && offset!=0 && !getText(offset-1, 1).contains("\n")){
            MyUndoManager.getUndoManager().isGroup(true);
                //aby enter za řádkem vždy atributově korespondoval s atributy řádku
                super.remove(offset, length);
                setCharacterAttributes(offset, 1, getCharacterElement(offset-1).getAttributes(), false);
            MyUndoManager.getUndoManager().isGroup(false);
            return;
        }
        
        super.remove(offset, length);
    }

    /**
     * Metoda vloží řetězec s danými atributy na danou pozici v dokumentu.
     * Ošetřeno vkládání obrázku a politika vkládání řetězců okolo obrázku.
     * @param offs pozice v dokumentu
     * @param str obsah
     * @param a atributy vkládaného obsahu
     * @throws BadLocationException v případě, že je obsah vkládán na neexistující pozici v dokumentu
     */
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        if(str.length()==0){
            return;
        }
        if(a==null){
            a = new SimpleAttributeSet();
        }
        
        //vkládáme obrázek
        if(StyleConstants.getIcon(a)!=null){
            insertPictureString(offs, a);
            return;
        }
        
        logCorrectionIfNeeded(offs, str);
        lastWasPicture = false;
        
        insertNewLineBeforePictureIfNeeded(offs, str, a);
        super.insertString(offs, str, a);
    }

    /**
     * Provede opravu dokumentu po vložení obsahu. (Smaže všechny nové řádky, které byly vloženy navíc za obrázky.)
     */
    public void correctDocument(){
        Iterator iterator = uncorrectPositions.iterator();
        int shift = 0;
        while (iterator.hasNext()){
            int pos = (int) iterator.next();
            try {
                super.remove(pos-shift,1);
            } catch (BadLocationException ex) {
                //Tohle je čuňárna, ale situace si to žádala!!!
                //Ošetřuje to chybu, kdy jsem zkopíroval obrázek před obrázkem a po vložení se mi vložili dva obrázky.
                //Tohle ten jeden obrázek navíc dodatečně odstraní.
                if(clipBoardWasChangedByThisApp){
                    if(getCharacterElement(pos-shift-1).getName().equals("icon")){
                        try {
                            super.remove(pos-shift-1,1);
                        } catch (BadLocationException ex1) {
                            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                        }
                    }else{
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }
            shift++;
        }
        uncorrectPositions.clear();
    }
    
    /**
     * Nastaví iínidkátor, že schránka byla změněna.
     * @param byThisApp Byla změněna tímto editorem?
     */
    public void clipBoardWasChanged(boolean byThisApp){
        clipBoardWasChangedByThisApp = byThisApp;
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Vloží do dokumentu obrázek (ve formě atributu) na dané místo. 
     * @param offs pozice v dokumentu
     * @param a atributy s obrázkem
     * @throws BadLocationException v případě, že je obsah vkládán na neexistující pozici v dokumentu
     */
    private void insertPictureString(int offs, AttributeSet a) throws BadLocationException{
        int plus = 0;
        
        //převzetí atributů z předchozí znaku
        AttributeSet set = getCharacterElement(offs-1).getAttributes();
        MutableAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attr, StyleConstants.getFontFamily(set));
        StyleConstants.setFontSize(attr, StyleConstants.getFontSize(set));
        StyleConstants.setBold(attr, StyleConstants.isBold(set));
        
        //Pokud nejsme na začátku dokumentu nebo se před obrázkem nenachází znak nového řádku, tak vložit znak nového řádku
        if(offs!=0 && !getText(offs-1, 1).contains("\n")){
            super.insertString(offs, "\n", attr);
            plus = 1;
        }
        
        StyleConstants.setIcon(attr, StyleConstants.getIcon(a));
        super.insertString(offs+plus, "\n", attr);
        lastWasPicture = true;
    }
    
    /**
     * Zalogování pozice vkládaných nových řádků za obrázky při čtení souboru nebo vkládání ze schránky.  
     * @param offs pozice v dokumentu
     * @param str vkládaný řezězec (nezáleží na něm, protože bude smazán později)
     * @throws BadLocationException v případě, že je obsah vkládán na neexistující pozici v dokumentu
     */
    private void logCorrectionIfNeeded(int offs, String str) throws BadLocationException{
        if(readingInProgress && lastWasPicture && str.equals("\n")){
            uncorrectPositions.add(offs);
        }
    }
    
    /**
     * V případě vložení znaku před obrázek metoda rozhodne, zda je potřeba vložit odřádkování před obrázek a případně ho rovnou vloží.
     * @param offs pozice v dokumentu
     * @param str vkládaný řezězec
     * @param a atributy vkládaného řetězce
     * @throws BadLocationException v případě, že je obsah vkládán na neexistující pozici v dokumentu
     */
    private void insertNewLineBeforePictureIfNeeded(int offs, String str, AttributeSet a) throws BadLocationException{
        //nejdříve zjistit, zda je potřeba vložit enter před obrázek
        if(!readingInProgress && getCharacterElement(offs).getName().equals("icon") && !str.equals("\n")){
            //v případě, že jsme na začátku dokumentu, tak vlož enter při vložení nějakého znaku před obrázek
            //nebo
            //v případě, že znak před obrázkem není znak nového řádku nebo to není obrázek, tak vložit znak nové řádky
            if(offs==0 || (!(getText(offs-1, 1).contains("\n")&&!getCharacterElement(offs-1).getName().equals("icon")))){
                super.insertString(offs, "\n", a);
            }
        }
    }
    
    /**
     * Smaže selekci, ale v případě potřeby vloží před obrázek nový řádek.
     * @param offset pozice mazání
     * @param length délka selekce
     * @throws BadLocationException v případě, že daná pozice v dokumentu neexistuje
     */
    private void correctlyRemoveSelectionBeforePicture(int offset, int length) throws BadLocationException{
        super.remove(offset, length);
        //v případě, že před mazáním je enter nebo dokonce začátek dokumentu, tak prostě smazat selekci
        if(!(offset==0 || getText(offset-1, 1).contains("\n"))){
            //pokud před selekcí není znak nového řádku, tak ho tam vložit
            super.insertString(offset,"\n",null);
        }
    }
    
    /**
     * Odstraní obrázek i s novými řádkami okolo.
     * @param offset pozice mazání
     * @throws BadLocationException v případě, že daná pozice v dokumentu neexistuje
     */
    private void correctlyRemovePicture(int offset) throws BadLocationException{
        //v případě, že před mazáním je enter nebo dokonce začátek dokumentu, tak pouze smazat znak
        if(offset==0 || getText(offset-1, 1).contains("\n")){
            super.remove(offset, 1);
        }else if(getCharacterElement(offset+2).getName().equals("icon")){
            MyUndoManager.getUndoManager().isGroup(true);
                //pokud je za obrázkem hned další obrázek, tak nemazat enter a zachovat zarovnání odstavce toho dalšího obrázku
                AttributeSet attr = getParagraphElement(offset+2).getAttributes();
                super.remove(offset+1, 1);
                setParagraphAttributes(offset+1, 1, attr, false);
            MyUndoManager.getUndoManager().isGroup(false);
        }else{
            //pokud za obrázkem už další obrázek není, tak smazat enter i obrázek za ním
            super.remove(offset, 2);
        }
    }
}
