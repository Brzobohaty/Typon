//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

import java.awt.Color;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.ViewFactory;
import view.EditorViewes.IconMyView;
import view.MyTextPane;

/**
 * Třída představuje model celé aplikace.
 */
public class Model implements ClipboardOwner{
    /************************************************************************************************
    Deklarace statickcých proměnných.
    ************************************************************************************************/
    
    /**
     * Instance této třídy. (Singleton) Model
     */
    private static Model model;

    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    /**
     * Konstanty představující defaultní nastavení.
     */
    public static final int FONT_SIZE = 12;
    public static final String FONT_FAMILY = "Times New Roman";
    public static final boolean BOLD = false;
    public static final int ALIGN = StyleConstants.ALIGN_LEFT;
    public static final float LINE_SPACING = (float) 0;
    public static final float FIRST_LINE_INDENT = 0;
    public static final int BIDI_LEVEL = 0;
    public static final float SPACE_ABOVE = 0;
    public static final float SPACE_BELOW = 0;
    public static final float LEFT_INDENT = 0;
    public static final float RIGHT_INDENT = 0;
    public static final Color BACKGROUND = Color.white;
    public static final Color FOREGROUND = Color.black;
    public static final boolean ITALIC = false;
    public static final boolean STRIKE_THROUGH = false;
    public static final boolean UNDERLINE = false;
    public static final boolean SUBSCRIPT = false;
    public static final boolean SUPERSCRIPT = false;
    
    /**
     * Stará se o veškerou práci se soubory.
     */
    private final FileHandler fileHandler = new FileHandler();
    
    /**
     * Manager pro funkci Zpět.
     */
    private final MyUndoManager undoManager = MyUndoManager.getUndoManager();
    
    /**
     * Křestní jméno uživatele
     */
    private String userFirstName = "";

    /**
     * Příjmení uživatele
     */
    private String userSurename = "";
    
    /**
     * Model dokumentu, který se v editovacím okně zobrazuje.
     */
    private MyDocument document;

    /**
     * Indikuje, zda došlo ke změnám v dokumentu od posledního uložení.
     */
    private boolean hasChanged = false;
    
    /**
     * Model editovacího okna.
     */
    private MyEditorKit editorKit;
    
    /**
     * Editovací okno.
     */
    private MyTextPane editor;
    
    /**
     * Lokální schránka pouze pro tento editor.
     */
    private final Clipboard localClipboard = LocalClipboard.getClipboard();
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    private Model(){}
    
    /**
     * Tovární metoda. (Singleton)
     * @return Instance třídy Model
     */
    public static Model getModel() {
        if(model == null){
            model = new Model();
        }
        return model;
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * @param editor editovací okno
     * @param viewFactory továrna na view v editovacím okně
     */
    public void setEditor(MyTextPane editor, ViewFactory viewFactory){
        this.editor = editor;
        editorKit = new MyEditorKit(viewFactory);
        editor.setEditorKit(editorKit);
        document = (MyDocument)editor.getDocument();
        fileHandler.setEditorKit(editorKit);
        document.addUndoableEditListener(undoManager);
    }
    
    /**
     * @return křestní jméno uživatele      
     */
    public String getUserFisrtName() {
        return userFirstName;	
    }

    /**
     * @return příjmení uživatele
     */
    public String getUserSurename() {
        return userSurename;	
    }

    /**
     * Uložení jména a příjmení uživatele.
     * @param firstName křestní jméno uživatele
     * @param surename  příjmení uživatele
     */
    public void setUserName(String firstName, String surename) {
        userFirstName = firstName;
        userSurename = surename;
    }
    
    /**
     * Vymaže veškerý obsah dokumentu (editoru) a referenci na uložený soubor.
     */
    public void clearDocument(){
        try {
            document.remove(0, document.getLength());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        setDefaultStyle();
        hasChanged=false;
        fileHandler.clearLocalFile();
        undoManager.clearManager();
/*************************************************************************************************************************/
//        try {
//            openImage(new File("images.jpg"));
//        } catch (IOException ex) {
//            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    /**
     * Uloží obsah editoru do zvoleného souboru.
     * @param file soubor, do kterého se má uložit obsah editoru (může být null)
     * @throws java.io.IOException Pokud nastala chyba při zapisování.
     * @throws java.io.FileNotFoundException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws javax.swing.text.BadLocationException Pokud byla nastavena pozice, která v dokumentu neexistuje.
     */
    public void saveLocalFile(File file) throws IOException, FileNotFoundException, BadLocationException{
        fileHandler.saveFile(file, document);
        hasChanged=false;
    }
    
    /**
     * Uloží soubor do nastaveného sdíleného úložiště s názvem „RRRR_MM_DD_HH_MM_SS_přijmení_jméno“.
     * @throws java.io.IOException Pokud nastala chyba při zapisování.
     * @throws java.io.FileNotFoundException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws javax.swing.text.BadLocationException Pokud byla nastavena pozice, která v dokumentu neexistuje.
     */
    public void saveRemoteFile() throws IOException, FileNotFoundException, BadLocationException{
        fileHandler.saveRemoteFile(document, userFirstName, userSurename);
    }
    
    /**
     * @return soubor pro lokální uložení
     */
    public File getLocalFile(){
        return fileHandler.getLocalFile();
    }
    
    /**
     * Indikuje, zda došlo ke změnám v dokumentu od posledního uložení.
     * @return true pokud ke změnám došlo
     */
    public boolean isHasChanged(){
        return hasChanged;
    }
    
    /**
     * Nastavuje proměnou tak, aby indikovala, že došlo ke změnám.
     */
    public void hasChanged(){
        hasChanged=true;
    }
    
    /**
     * Nastaví listener pro změny v dokumentu.
     * @param listener 
     */
    public void addDocumentListener(DocumentListener listener){
        document.addDocumentListener(listener);
    }
    
    /**
     * Metoda nastaví font vybrané části textu nebo následně psaným znakům.
     * @param fontName název fontu
     */
    public void setFont(String fontName) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontFamily(attrs, fontName);
        editor.setCharacterAttributes(attrs, false);
    }
    
    /**
     * Metoda nastaví velikost fontu vybrané části textu nebo následně psaným znakům.
     * @param fontSize velikost fontu
     */
    public void setFontSize(int fontSize) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, fontSize);
        editor.setCharacterAttributes(attrs, false);
    }
    
    /**
     * Metoda nastaví tučnost písma vybrané části textu nebo následně psaným znakům.
     * @param bold tučnost/netučnost
     */
    public void setBold(boolean bold) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setBold(attrs, bold);
        editor.setCharacterAttributes(attrs, false);
    }
    
    /**
     * Metoda nastaví zarovnání odstavce vybrané části textu nebo následně psaným znakům.
     * @param align zarovnání (StyleConstants)
     */
    public void setAlign(int align) {
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attrs, align);
        editor.setParagraphAttributes(attrs, false);
        editor.repaint();
    }
    
    /**
     * Otevře obrázek vloží ho do dokumentu.
     * @param file soubor, ze kterého se má obrázek načíst
     * @throws IOException Pokud nastala chyba při čtení.
     */
    public void openImage(File file) throws IOException{
        BufferedImage image = fileHandler.openImage(file);
        editor.insertIcon(new ImageIcon(image));
    }
    
    /**
     * Otevře v editoru obsah RTF souboru.
     * @param file soubor s RTF obsahem
     * @throws IOException pokud nastane chyba při čtení ze souboru
     * @throws FileNotFoundException pokud daný soubor nebyl nalezen
     */
    public void readFile(File file) throws FileNotFoundException, IOException{
        clearDocument();
        fileHandler.readFile(file, document);
        setUnsupportedAttributes(null);
        undoManager.clearManager();
        hasChanged = false;
    }
    
    /**
     * Vyjme označený obsah do schránky.
     */
    public void cut(){
        editor.cut();
        transferToLocalClipboard();
        document.clipBoardWasChanged(true);
    }
    
    /**
     * Zkopíruje označený obsah do schránky.
     */
    public void copy(){
        editor.copy();
        transferToLocalClipboard();
        document.clipBoardWasChanged(true);
    }
    
    /**
     * Vloží obsah schránky na místo kurzoru.
     * @throws BadLocationException Pokud bylo přistupováno k neexistujícímu indexu v dokumentu.
     */
    public void paste() throws BadLocationException{
        if (localClipboard.getContents(null) != null){
            undoManager.isGroup(true);    
                pasteCorectly();
                setUnsupportedAttributes(null);

                //Pokud se nachází za vložením enter (a není to obrázek), tak změnit jeho atributy na atributy posledního znaku vložení
                if(document.getText(editor.getCaretPosition(), 1).contains("\n") && !document.getCharacterElement(editor.getCaretPosition()-1).getName().equals("icon")){
                    document.setCharacterAttributes(editor.getCaretPosition(), 1, document.getCharacterElement(editor.getCaretPosition()-1).getAttributes(), false);
                }
            undoManager.isGroup(false);
        }
    }
    
    /**
     * @return zda existuje soubor s nastavením vzdáleného uložiště 
     */
    public boolean isRemoteFileSetup(){
        return fileHandler.isRemoteFileSetup();
    }
    
    /**
     * @return zda existuje soubor s cestou ke globálnímu nastavením funkcí 
     */
    public boolean isRemoteFunctionSettingsSetup(){
        return fileHandler.isRemoteFunctionSettingsSetup();
    }
    
    /**
     * Přečte soubor s globálním nastavením funkcí a vrátí jeho obsah v podobě nastavení funkcí.
     * @return nastavení funkcí
     */
    public FunctionSetup readGlobalFunctionsSetup(){
        return fileHandler.readGlobalFunctionsSetup();
    }
    
    /**
     * @return zda byl modifikován soubor s globálním nastavením funkcí od poslední aktualizace
     * Pokud nebyl nastaven takový soubor, tak vrací false.
     */
    public boolean wasRemoteFunctionSetupModified(){
        return fileHandler.wasRemoteFunctionSetupModified();
    }
    
    /**
     * Metoda je zavolána v případě, že do systémové schránky je nahrán obsah z jiné aplikace. 
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        //Musí se chvilku počkat, aby nedošlo k souběhu
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        document.clipBoardWasChanged(false);
        transformContetnOfSystemClipboardToLocalClipboard();
    }
    
    /**
     * Přetransformování obsahu ze systémové schránky do lokální schránky.
     * Tato metoda by se měla volat pouze v případě, že obsah byl změněn jiným programem.
     */
    public void transformContetnOfSystemClipboardToLocalClipboard(){
        try {
            Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
            if (transferable != null){
                //jedná se čistě jenom o obrázek
                if(transferable.isDataFlavorSupported(DataFlavor.imageFlavor) && !transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
                    localClipboard.setContents(transferable, model);
                //jedná se o cokoli v čem je text
                }else if(transferable.isDataFlavorSupported(DataFlavor.stringFlavor)){
                    //převést na text
                    localClipboard.setContents(new StringSelection((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)), model);
                }else{
                    localClipboard.setContents(null, model);
                }
            }else{
                localClipboard.setContents(null, model);
            }
        } catch (UnsupportedFlavorException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * Přivlastní systémovou schránku (vlastníkem bude Typoň).
     */
    public void ownClipBoard(){
        Clipboard systemCliboard = Toolkit.getDefaultToolkit().getSystemClipboard(); 

        //Zapamatujeme si obsah systémové schránky
        Transferable content = systemCliboard.getContents(this);
        
        //Je tam nějaký bug, takže se musí chvilku počkat před dalším použitím schránky.
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        //vrácení původního obsahu do systémové schránky
        systemCliboard.setContents(content, this);
        
        //Tohle případně systémovou schránku jakoby uzavře. (stejně jako u souboru)
       Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Nastavuje defaultní styl dokumentu.
     */
    private void setDefaultStyle(){
        MutableAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setFontSize(attrs, FONT_SIZE);
        //StyleConstants.setAlignment(attrs, ALIGN);
        StyleConstants.setFontFamily(attrs, FONT_FAMILY);
        StyleConstants.setBold(attrs, BOLD);
        setUnsupportedAttributes(attrs);
    }
    
    /**
     * Nastaví atributy, které nejdou v editoru měnit uživatelem na defaultní hodnoty.
     * @param attrs set atributů (může být null)
     */
    private void setUnsupportedAttributes(MutableAttributeSet attrs) {
        if(attrs == null){
            attrs = new SimpleAttributeSet();
        }
        StyleConstants.setFirstLineIndent(attrs, FIRST_LINE_INDENT);
        StyleConstants.setBidiLevel(attrs, BIDI_LEVEL);
        StyleConstants.setLeftIndent(attrs, LEFT_INDENT);
        StyleConstants.setLineSpacing(attrs, LINE_SPACING);
        StyleConstants.setSpaceAbove(attrs, SPACE_ABOVE);
        StyleConstants.setSpaceBelow(attrs, SPACE_BELOW);
        StyleConstants.setBackground(attrs, BACKGROUND);
        StyleConstants.setForeground(attrs, FOREGROUND);
        StyleConstants.setItalic(attrs, ITALIC);
        StyleConstants.setRightIndent(attrs, RIGHT_INDENT);
        StyleConstants.setStrikeThrough(attrs, STRIKE_THROUGH);
        StyleConstants.setUnderline(attrs, UNDERLINE);
        StyleConstants.setSubscript(attrs, SUBSCRIPT);
        StyleConstants.setSuperscript(attrs, SUPERSCRIPT);
        editor.setCharacterAttributes(attrs, false);
        editor.setParagraphAttributes(attrs, false);
        document.setParagraphAttributes(0, document.getLength()+1, attrs, false);
        document.setCharacterAttributes(0, document.getLength()+1, attrs, false);
    }
    
    /**
     * Buďto převede schránku na string bez formátování a nebo v případě obrázku do ní vloží pouze samotný obrázek.
     * @param clipBoard schránka
     */
    private void stringOrImageToClipboard(Clipboard clipBoard){
        //Je tam nějaký bug, takže se musí chvilku počkat před dalším použitím schránky.
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        //Vložit do schránky obrázek nebo text?
        Caret caret = editor.getCaret();
        if (caret.getDot()==(caret.getMark()-1) && document.getCharacterElement(editor.getCaret().getDot()).getName().equals("icon")){
            //jestliže se jedná o samotný obrázek, tak ho vložit do schránky
            clipBoard.setContents(new ImageSelection(IconMyView.getCurrentImageView().getOriginal()), this);
        }else{
            //Odstraní ze schránky formátování textu a různé objekty (obrázky) => nechá pouze čistý text
            try {
                clipBoard.setContents(new StringSelection((String) clipBoard.getData(DataFlavor.stringFlavor)), this);
            } catch (UnsupportedFlavorException | IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        } 
    }
    
    /**
     * Převede obsah systémové schránky do lokální schránky této aplikace a v systémové schránce nechá pouze holý text bez formátování a obrázků
     */
    private void transferToLocalClipboard(){
        Clipboard systemCliboard = Toolkit.getDefaultToolkit().getSystemClipboard(); 
        
        //překopírování obsahu ze systémové schránky do lokální schránky
        localClipboard.setContents(systemCliboard.getContents(this), this);
        
        stringOrImageToClipboard(systemCliboard);
        
        //Tohle případně systémovou schránku jakoby uzavře. (stejně jako u souboru)
        Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    
    /**
     * Vloží do editoru data ze schránky. 
     * (Pokud pocházejí data z tohoto editoru, tak se vloží i s formátováním a pokud zvenčí tak bez.)
     */
    private void pasteCorectly(){
        Clipboard systemCliboard = Toolkit.getDefaultToolkit().getSystemClipboard(); 

        //Zapamatujeme si obsah systémové schránky
        Transferable content = systemCliboard.getContents(this);
        
        //překopírování obsahu z lokální schránky do systémové schránky
        systemCliboard.setContents(localClipboard.getContents(this), this);

        //bere data ze systémové schránky
        pasteStringOrImage();
        
        //Je tam nějaký bug, takže se musí chvilku počkat před dalším použitím schránky.
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        //vrácení původního obsahu do systémové schránky
        systemCliboard.setContents(content, this);
        
        //Tohle případně systémovou schránku jakoby uzavře. (stejně jako u souboru)
       Toolkit.getDefaultToolkit().getSystemClipboard();
    }
    
    /**
     * Rozliší, zda je ve schránce obrázek nebo text a vloží obsach schránky.
     */
    private void pasteStringOrImage(){
        Transferable transferable = localClipboard.getContents(null);
        if(transferable != null){
            if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)){
                try {
                    editor.insertIcon(new ImageIcon((Image) transferable.getTransferData(DataFlavor.imageFlavor)));
                } catch (UnsupportedFlavorException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                } catch (IOException ex) {
                    editor.paste();
                }
            }else{
                editor.paste();
            }
        }
    }
   
    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Třída představuje obrázek ve schránce.
     */
    private static class ImageSelection implements Transferable{
        private final Image image;

        public ImageSelection(Image image){
            this.image = image;
        }

        // Returns supported flavors
        @Override
        public DataFlavor[] getTransferDataFlavors(){
            return new DataFlavor[] { DataFlavor.imageFlavor };
        }

        // Returns true if flavor is supported
        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor){
            return DataFlavor.imageFlavor.equals(flavor);
        }

        // Returns image
        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException{
            if (!DataFlavor.imageFlavor.equals(flavor)){
                throw new UnsupportedFlavorException(flavor);
            }
            return image;
        }
    }
}

