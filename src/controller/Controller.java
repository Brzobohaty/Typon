//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package controller;

import com.sun.media.jai.codec.ImageCodec;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import model.FunctionSetup;
import model.Model;
import model.MyUndoManager;
import net.coobird.thumbnailator.Thumbnails;
import rtf.AdvancedRTFEditorKit;
import view.EditorViewes.IconMyView;
import view.ErrorDialog;
import view.MyTextPane;
import view.MyTextPane.MyCaret;
import view.MyView;
import view.ResizableImage;


/**
 * Třída představuje controller celé aplikace.
 */
public class Controller
{
    /************************************************************************************************
    Deklarace statickcých proměnných.
    ************************************************************************************************/
    
    /**
     * Instance této třídy. (Singleton) Contorller
     */
    private static Controller controller;
    
    /************************************************************************************************
    Deklarace proměnných.
    ************************************************************************************************/
    
    /**
     * GUI editoru
     */
    private final MyView view;

    /**
     * Model editoru
     */
    private final Model model;
    
    /**
     * Slouží pro zapínání a vypínání listenerů na tlačítkové liště.
     */
    private boolean activeListeners = true;
    
    /**
     * Indikátor, že pozice caretu byla změněna, protože proběhla editace dokumentu.
     */
    private boolean caretWasUsedForChanges = false;
    
    /**
     * Indikátor, zda je povoleno vkládat obsah klávesovou zkratkou.
     */
    private boolean allowPaste = true;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    private Controller(MyView view, Model model) {
        this.view = view;
        this.model = model;
        model.setEditor(view.getEditor(), view.getViewFactory());
        setKeys();
        setMainListeners();
        setPasteButton();
        view.enableCopyCutButtons(false);
        setAssociation();
    }

    /**
     * Tovární metoda. (Singleton)
     * @return instanci Controlleru
     */
    public static Controller getController() {
        if(controller == null){
            try {
                ErrorLogger.setup();
            } catch (IOException ex) {
                Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
            }
            checkAccessToLibraries();
            controller = new Controller(MyView.getView(), Model.getModel());
        }
        return controller;
    }
    
    /************************************************************************************************
    Deklarace statických metod.
    ************************************************************************************************/
    
    /**
     * Zkontroluje, zda jsou přístupné všechny externí knihovny.
     * Pokud ne, tak vyhodí varovnou hlášku a ukončí aplikaci.
     */
    static void checkAccessToLibraries() {
        try {
            new AdvancedRTFEditorKit();
            Thumbnails.of("nevim");
            ImageCodec.getCodecs();
        } catch (NoClassDefFoundError | Exception ex) {
            LOGGER.log(Level.SEVERE, "Ve složce s exe souborem nebyly nalezeny knihovny ve složce \"lib\" nebo jsou knihovny narušeny. ("+ex.getMessage()+")", ex);
            new ErrorDialog(new JFrame(),"Ve složce s exe souborem nebyly nalezeny knihovny (lib) nebo jsou soubory narušeny.");
            ErrorLogger.close();
            System.exit(0);
        }
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * Zobrazí GUI aplikace.
     * @param file soubor, který se má v editoru otevřít
     */
    public void setVisible(String file) {
        model.clearDocument();
        
        if(file != null){
            try {
                model.readFile(new File(file));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        
        if(model.isRemoteFunctionSettingsSetup()){
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleAtFixedRate(new functionSetupPeriodicChecker(), 0, 2, TimeUnit.SECONDS);
        }
        
        raiseNameForm();
        view.focusEditor();
        refreshButtonPanel();
        
        view.setVisible(true);
        
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        model.transformContetnOfSystemClipboardToLocalClipboard();
        model.ownClipBoard();
    }

    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Nastaví všechny listenery pro hlavní tlačítka editoru.
     */
    private void setMainListeners() {
        view.addMenuNovyListener(new NewFileListener());
        view.addComboBoxFontListener(new FontListenerComboBox());
        view.addMenuUlozitJakoListener(new SaveButtonListener());
        view.addMenuUlozitListener(new SaveButtonListener());
        view.addFileChooserListener(new FileChooserListener());
        view.addImageChooserListener(new ImageChooserListener());
        view.addMainFrameListener(new MainFrameListener());
        view.addMenuOtevritListener(new OpenButtonListener());
        view.addSelectionListener(new MyCaretListener());
        model.addDocumentListener(new MyDocumentListener());
        view.addComboBoxFontSizeListener(new FontSizeComboBoxListener());
        view.addToggleButtonBoldListener(new BoldButtonListener());
        view.addToggleButtonAlignLeftListener(new ButtonAlignLeftListener());
        view.addToggleButtonAlignCenterListener(new ButtonAlignCenterListener());
        view.addButtonInsertPictureListener(new InsertImageButtonListener());
        view.setUndoAction(MyUndoManager.getUndoManager().getUndoAction());
        view.addEditorListeners(new EditorMouseListener(),new MyKeyListener(), new EditorFocusListener());
        view.addPopUpMenuItemsListeners(new CutPopupMenuItemListener(), new CopyPopupMenuItemListener(), new PastePopupMenuItemListener());
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new ClipBoardListener());
    }
    
    /**
     * Zobrazí formulář pro zadání jména uživatele.
     * Pokud již bylo jednou jméno zadáno, tak to zobrazí formulář předvyplněný.
     */
    private void raiseNameForm(){
        if(model.isRemoteFileSetup()){
            if(model.getUserSurename().equals("")){
                view.raiseNameForm(new NameFormOKListener());
            }else{
                view.raiseNameFormAgain();
            }
        }
    }
    
    /**
     * Nastaví celý tlačítkový panel tak, aby odpovídal právě označenému textu.
     */
    private void refreshButtonPanel(){
        MyTextPane editor = view.getEditor();
        if(!caretWasUsedForChanges){
            try {
                editor.updateInputOrSelectedAttributes();
            } catch (BadLocationException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        caretWasUsedForChanges = false;
        activeListeners=false;
        view.setFontComboBox(StyleConstants.getFontFamily(editor.getSelectedAtrributes()));
        view.setFontSizeComboBox(StyleConstants.getFontSize(editor.getSelectedAtrributes()));
        view.setBoldButton(StyleConstants.isBold(editor.getSelectedAtrributes()));
        switch(StyleConstants.getAlignment(editor.getSelectedAtrributes())){
            case StyleConstants.ALIGN_LEFT: view.setAlignLeftButton(true);break;
            case StyleConstants.ALIGN_CENTER: view.setAlignCenterButton(true);break;
            default: view.clearAlignButtonsSelection();
        }
        activeListeners=true;
    }
    
    /**
     * Uloží obsah editoru do daného souboru a zároveň také na nastavené sdílené uložiště.
     * @param file soubor, do kterého chceme data uložit (může být null, pokud existuje locaFile)
     */
    private void saveFile(File file){
        view.setWorkingCursor(true);
        try {
            model.saveLocalFile(file);
        } catch (FileNotFoundException ex) {
            view.raiseErrorDialog("Nelze otevřít soubor: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
            view.raiseErrorDialog("Při zapisování nastala chyba: " + ex.getLocalizedMessage());
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        try {
            model.saveRemoteFile();
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage()+" Je potřeba spustit administrační nástroj a nastavit správně obsah konfiguračního souboru.", ex);
        } catch (IOException | BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        view.setWorkingCursor(false);
    }
    
    /**
     * Uloží soubor nebo vyvolá fomulář pro uložení v případě, že došlo ke změnám v dokumentu.
     * @return true pokud byl stisknut cancel (nemá být dokončena zvolená akce)
     */
    private boolean saveFileIfNeed(){
        if(model.isHasChanged()){
            int value = view.raiseOptionDialogWithCancel("Chcete uložit změny v dokumentu?", "Uložit změny", "Ano", "Ne", "Cancel");
            switch(value){
                case 0: if(model.getLocalFile()==null){
                            view.showSaveDialog();
                        }else{
                            saveFile(null);
                        }
                        break;
                case 2: return true;
            }
        }
        return false;
    }
    
    /**
     * Zapne nebo vypne tlačítko pro vložení obsahu schránky podle toho, zda tam nějaký obsah je.
     */
    private void setPasteButton(){
        Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable contents = clip.getContents(this);
        if(contents == null){
            view.enablePasteButton(false);
        }else{
            if(contents.isDataFlavorSupported(DataFlavor.imageFlavor) && !contents.isDataFlavorSupported(DataFlavor.stringFlavor)){
                view.enablePasteButton(true);
            }else if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)){
                try {
                    if(contents.getTransferData(DataFlavor.stringFlavor).toString().length() != 0){
                        view.enablePasteButton(true);
                    }else{
                        view.enablePasteButton(false);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                }
            }else{
                view.enablePasteButton(false);
            }
        }
    }
    
    /**
     * přepsání key listeneru pro editor, aby volal metody z modelu a ne přímo svoje
     */
    private void setKeys(){
        InputMap im = view.getEditor().getInputMap();
        ActionMap am = view.getEditor().getActionMap();
        im.put(KeyStroke.getKeyStroke("control C"), "preventCopy");
        am.put("preventCopy", new AbstractAction(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 view.setWorkingCursor(true);
                 model.copy();
                 view.setWorkingCursor(false);
              }
        });
        
        im.put(KeyStroke.getKeyStroke("control X"), "preventCut");
        am.put("preventCut", new AbstractAction(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 view.setWorkingCursor(true); 
                 model.cut();
                 view.setWorkingCursor(false);
              }
        });
        
        im.put(KeyStroke.getKeyStroke("control V"), "preventPaste");
        am.put("preventPaste", new AbstractAction(){
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     if(allowPaste){
                        view.setWorkingCursor(true);
                        model.paste();
                        view.setWorkingCursor(false);
                     }
                 } catch (BadLocationException ex) {
                     LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                 }
              }
        });
    }
    
    /**
     * Nastaví v systému souborům s koncovkou .typon, aby se automaticky otevíraly v tomto editoru.
     */
    private void setAssociation(){
        try {
            String command = "Assoc .typon=typonfile";
            String path = Controller.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceFirst("/", "").replace("/", "\\");
            String command2 = "Ftype typonfile=\""+path+"\" %1";
            Runtime.getRuntime().exec("cmd /c "+command); 
            Runtime.getRuntime().exec("cmd /c "+command2);
        }catch(IOException ex){
            LOGGER.log(Level.SEVERE, "Asociace souborů se nezdařila. " + ex.getMessage(), ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Listener pro tlačítko "Nový", který vytvoří nový dokument.
     */
    private class NewFileListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            raiseNameForm();
            if(saveFileIfNeed()){
                return;
            }
            model.clearDocument();
            refreshButtonPanel();
        }
    }
    
    /**
     * Listener pro tlačítko OK na formuláři pro zadání jména uživatele.
     */
    private class NameFormOKListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            String jmeno = view.getFirstName();
            String prijmeni = view.getSurname();
            JDialog dialogName = (JDialog)((JComponent)e.getSource()).getParent().getParent().getParent().getParent();
            
            if(jmeno.equals("")||prijmeni.equals("")){
                view.raiseErrorDialog("Některé pole zůstalo prázdné!");
            }else if(!deAccent(jmeno).matches("[a-zA-Z0-9]+")||!deAccent(prijmeni).matches("[a-zA-Z0-9]+")){
                view.raiseErrorDialog("Některé pole obsahuje nepovolené znaky!");
            }else{
                model.setUserName(jmeno,prijmeni);
                dialogName.setVisible(false);
            }
        }
        
        /**
         * Převod národních znaků v řetězci na anglické znaky.
         * @param str řetězec s libovolnými národními znaky
         * @return řtězec bez pouze s anglickými znaky
         */
        private String deAccent(String str) {
            String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            return pattern.matcher(nfdNormalizedString).replaceAll("");
        }
    }
    
    /**
     * Listener pro rozbalovací seznam fontů. (Při výběru fontu změnit označený a následující text.)
     */
    private class FontListenerComboBox implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e) {
            if(activeListeners){
                String font = (String) ((JComboBox)e.getSource()).getSelectedItem();
                model.setFont(font);
            }
        }
    }
    
    /**
     * Listener pro tlačítko Uložit a Uložit jako
     */
    private class SaveButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if(((JMenuItem)e.getSource()).getText().equals("Uložit") && model.getLocalFile()!=null){
                saveFile(null);
            }else{
                view.showSaveDialog();
            }
        }
    }
    
    /**
     * Listener pro tlačítka v okně pro výběr souboru.
     */
    private class FileChooserListener implements ActionListener{
        File file;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)){
                JFileChooser fc = (JFileChooser) e.getSource(); 
                if(fc.getDialogType()==JFileChooser.SAVE_DIALOG){
                    saveFile(fc.getSelectedFile());
                }else{
                    file = fc.getSelectedFile();
                    try {
                        view.setWorkingCursor(true);
                        model.readFile(file);
                        try {
                            model.saveLocalFile(file);
                        }catch (FileNotFoundException ex){
                            //otevře se jako kopie, nebude vázáno na soubor
                        }
                        view.setWorkingCursor(false);
                        raiseNameForm();
                    }catch (BadLocationException ex){
                        LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
                    }catch (FileNotFoundException ex) {
                        view.raiseErrorDialog("Soubor "+file.getAbsolutePath()+" neexistuje.");
                    }catch (IOException ex) {
                        view.raiseErrorDialog("Při čtení souboru došlo k chybě.");
                    }
                }
                
            }
        }
    }
    
    /**
     * Listener pro tlačítko Otevřít
     */
    private class OpenButtonListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            if(saveFileIfNeed()){
                return;
            }
            view.showOpenDialog();
            refreshButtonPanel();
        }      
    }
    
    /**
     * Listener pro posuvník měnící velikost písma.
     */
    private class FontSizeComboBoxListener implements ActionListener{  

        @Override
        public void actionPerformed(ActionEvent e) {
            if(activeListeners){
                int fontSize = Integer.valueOf((String)((JComboBox)e.getSource()).getSelectedItem());
                model.setFontSize(fontSize);
            }
        }
    }
    
    /**
     * Listener pro tlačítko Vložit obrázek
     */
    private class InsertImageButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            view.showImageOpenDialog();
        }
    }
    
    /**
     * Listener pro tlačítka v okně pro výběr obrázku ze souborového systému.
     */
    private class ImageChooserListener implements ActionListener{
        File file;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if(e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)){
                JFileChooser fc = (JFileChooser) e.getSource();
                file = fc.getSelectedFile();
                try{
                    model.openImage(file);
                }catch(IOException ex){
                    view.raiseErrorDialog("Při čtení obrázku došlo k chybě.");
                }
            }
        }
    }
    
    /**
     * Listener pro stisknutí položky vyjmout ve vyskakovacím menu.
     */
    private class CutPopupMenuItemListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            view.setWorkingCursor(true);
            model.cut();
            view.setWorkingCursor(false);
        }
    }
    
    /**
     * Listener pro stisknutí položky kopírovat ve vyskakovacím menu.
     */
    private class CopyPopupMenuItemListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            view.setWorkingCursor(true);
            model.copy();
            view.setWorkingCursor(false);
        }
    }
    
    /**
     * Listener pro stisknutí položky vložit ve vyskakovacím menu.
     */
    private class PastePopupMenuItemListener implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                view.setWorkingCursor(true);
                model.paste();
                view.setWorkingCursor(false);
            } catch (BadLocationException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Listener pro vypínač tučného písma.
     */
    private class BoldButtonListener implements ItemListener{  
        boolean bold;
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            if(activeListeners){
                
                if(e.getStateChange()==ItemEvent.SELECTED){
                    bold = true;
                } else if(e.getStateChange()==ItemEvent.DESELECTED){
                    bold = false;
                }
                
                model.setBold(bold);
                view.focusEditor();
            }
        }
    }
    
    /**
     * Listener pro vypínač zarovnání odstavce doleva.
     */
    private class ButtonAlignLeftListener implements ItemListener{  
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            if(activeListeners){
                model.setAlign(StyleConstants.ALIGN_LEFT);
                view.focusEditor();
            }
        }
    }
    
    /**
     * Listener pro vypínač zarovnání odstavce na střed.
     */
    private class ButtonAlignCenterListener implements ItemListener{  
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            if(activeListeners){
                model.setAlign(StyleConstants.ALIGN_CENTER);
                view.focusEditor();
            }
        }
    }

    /**
     * Listener pro označení textu.
     */
    private class MyCaretListener implements CaretListener{
        @Override
        public void caretUpdate(CaretEvent e) {
            refreshButtonPanel();
            
            try {
                view.checkCursor(e.getMark(),e.getDot());
            } catch (BadLocationException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            
            if(e.getMark()==e.getDot()){
                view.enableCopyCutButtons(false);
            }else{
                view.enableCopyCutButtons(true);
            }
        }
    }
    
    /**
     * Listener pro uzavření hlavního okna. (Ukončení aplikace.)
     */
    private class MainFrameListener extends WindowAdapter{
        @Override
        public void windowClosing(WindowEvent e) {
            if(saveFileIfNeed()){
                return;
            }
            System.exit(0);
            ErrorLogger.close();
        }
    }
    
    /**
     * Listener pro změny v dokumentu.
     */
    private class MyDocumentListener implements DocumentListener{

        @Override
        public void insertUpdate(DocumentEvent e) {
            model.hasChanged();
            view.setCaretWasUsedForEdit();
            view.getEditor().updateSelectedAttributesAfterChanges();
            caretWasUsedForChanges = true;
            refreshButtonPanel();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            model.hasChanged();
            view.getEditor().updateSelectedAttributesAfterChanges();
            caretWasUsedForChanges = true;
            refreshButtonPanel();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            model.hasChanged();
        }
    }
    
    /**
     * Listener pro změnu obsahu schránky OS.
     */
    private class ClipBoardListener implements FlavorListener{ 
        @Override 
        public void flavorsChanged(FlavorEvent e) {
            setPasteButton();
        }
    }
    
    /**
     * Listener pro stisknutí, pohyb atd. myši na editoru.
     * Slouží k dvoum hlavním účelům.
     * 1. vyskakovací popupmenu pro práci se schránkou.
     * 2. Veškerá práce s obrázkem (označování, zvětšování, změna kurzoru)
     */
    private class EditorMouseListener extends MouseInputAdapter{
        /**
         * Vyskakovací menu pro práci se schránkou.
         */
        private JPopupMenu popupMenu;

        /**
         * View obrázku, na kterém bylo stisknuto tlačítko..
         */
        private IconMyView pressedView;
        
        /**
         * Maximální velikosti obrázku tak, aby nepřelézali stránku.
         */
        private final int MAX_WIDTH = IconMyView.MAX_WIDTH;
        private final int MAX_HIGHT = IconMyView.MAX_HIGHT;

        /**
         * Zvětšovatelný náhled právě zvětšovaného obrázku.
         */
        private ResizableImage resizable;

        /**
         * Poslední pozice kurzoru myši. 
         */
        private Point lastPos = null;

        /**
         * Aktuálně nastavený kurzor myši.
         */
        private int cursor;
        
        /**
         * Právě změněné rozměry obrázku.
         */
        private int w, h;
        
        /**
         * Indikátor, že naposledy byl obrázek tažen.
         */
        private boolean wasDragged = false;
        
        /**
         * Pozice karetu, když začneme zvětšovat obrázek. (pozice obrázku)
         */
        private int caretPosition;
        
        /**
         * Caret editoru.
         */
        private final MyCaret caret = (MyCaret)view.getEditor().getCaret();
        
        public EditorMouseListener() {
            popupMenu = view.getPopupMenu();
        }
        
        @Override
        public void mouseClicked(MouseEvent e){
            //odstranit focusy ze všech ostatních obrázků
            //Pokud není kliknuto na obrázek, tak obnovit selektování a kurzor.
            view.setSelectionVisible();
            caret.setInvisible(false);
            
            IconMyView imageView = view.getImageOnPosition(e.getPoint());
            if(imageView!=null){
                //Pokud je kliknuto na obrázek, tak dát obrázku focus.
                view.setPictureFocus(imageView);
                caret.setInvisible(true);
            }

            view.repaintEditor();
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
              
            lastPos = e.getPoint();
            IconMyView imageView = view.getImageOnPosition(lastPos);
            pressedView = imageView;
            if(imageView!=null){
                //nastavení správného kurzoru
                cursor = view.getCursor(lastPos,imageView);
                if(cursor != Cursor.HAND_CURSOR){
                    caretPosition = view.getEditor().getCaretPosition();
                    resizable = view.showThumbnail(imageView, cursor);
                    caret.setInvisible(true);
                }
            }else{
                view.setSelectionVisible();
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
            view.setSelectionVisible();
            caret.setInvisible(false);
            
            if(resizable != null && wasDragged){
                view.removeThumbnail(resizable);
                view.hideAspectRatioWarning();
                resizable = null;
                
                MyUndoManager undoManager = MyUndoManager.getUndoManager();
                undoManager.isGroup(true);
                    view.changeSizeOfPicture(pressedView,w,h);
                undoManager.isGroup(false);
                
                lastPos = null;
                pressedView = null;
                wasDragged = false;
                view.repaintFrame();
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
            //nastavení správného kurzoru
            if(resizable==null){
                IconMyView imageView = view.getImageOnPosition(e.getPoint());
                if (imageView!=null) {
                    view.setCursor(Cursor.getPredefinedCursor(view.getCursor(e.getPoint(),imageView)));
                }else{
                    view.setCursor(new Cursor(Cursor.TEXT_CURSOR));
                }
            }
        }
        
        @Override
        public void mouseDragged(MouseEvent me) {
            if (lastPos != null && resizable!=null && SwingUtilities.isLeftMouseButton(me)) {
                wasDragged = true;
                
                int x = resizable.getX();
                int y = resizable.getY();
                w = resizable.getWidth();
                h = resizable.getHeight();

                int dx = me.getX() - lastPos.x;
                int dy = me.getY() - lastPos.y;
                
                int rw = pressedView.getRatioWidth();
                int rh = pressedView.getRatioHeight();
                
                boolean keepAspectRatio = me.getModifiersEx()-MouseEvent.BUTTON1_DOWN_MASK==MouseEvent.SHIFT_DOWN_MASK;
                Rectangle bounds;
                
                //Podle kurzoru je rozpoznáno, jakým směrem se má obrázek zvětšovat nebo změnšovat.
                switch (cursor) {
                    case Cursor.N_RESIZE_CURSOR:
                        h -= dy;
                        if(keepAspectRatio){          
                            w = Math.round(h * ((float)rw/rh));
                        }
                        resizeThumbnail(me, x, y+dy, w, h, keepAspectRatio);
                        break;

                    case Cursor.S_RESIZE_CURSOR:
                        h += dy;
                        if(keepAspectRatio){       
                            w = Math.round(h * ((float)rw/rh));
                        }
                        resizeThumbnail(me, x, y, w, h, keepAspectRatio);
                        break;

                    case Cursor.W_RESIZE_CURSOR:
                        w -= dx;
                        if(keepAspectRatio){
                            h = Math.round(w * ((float)rh/rw));
                        }
                        resizeThumbnail(me, x+dx, y, w, h, keepAspectRatio);
                        break;

                    case Cursor.E_RESIZE_CURSOR:
                        w += dx;
                        if(keepAspectRatio){
                            h = Math.round(w * ((float)rh/rw));
                        }
                        resizeThumbnail(me, x, y, w, h, keepAspectRatio);
                        break;

                    case Cursor.NW_RESIZE_CURSOR:
                        w -= dx;
                        h -= dy;
                        if(keepAspectRatio){
                            if(-dx > -dy){
                                h = Math.round(w * ((float)rh/rw));
                                dy = dx;
                            }else if(-dy > -dx){            
                                w = Math.round(h * ((float)rw/rh));
                                dx = dy;
                            }
                        }
                        resizeThumbnail(me, x+dx, y+dy, w, h, keepAspectRatio);
                        bounds = view.getBoundsRelativeToMainFrame(resizable.getBounds());
                        lastPos = new Point(bounds.x+4+8,bounds.y+25+8);
                        break;

                    case Cursor.NE_RESIZE_CURSOR:
                        w += dx;
                        h -= dy;
                        if(keepAspectRatio){
                            if(dx > -dy){
                                h = Math.round(w * ((float)rh/rw));
                                dy = -dx;
                            }else if(-dy > dx){            
                                w = Math.round(h * ((float)rw/rh));
                            }
                        }
                        resizeThumbnail(me, x, y+dy, w, h, keepAspectRatio);
                        bounds = view.getBoundsRelativeToMainFrame(resizable.getBounds());
                        lastPos = new Point(bounds.x+bounds.width+4,bounds.y+25+8);
                        break;

                    case Cursor.SW_RESIZE_CURSOR:
                        w -= dx;
                        h += dy;
                        if(keepAspectRatio){
                            if(-dx > dy){
                                h = Math.round(w * ((float)rh/rw));
                            }else if(dy > -dx){            
                                w = Math.round(h * ((float)rw/rh));
                                dx = -dy;
                            }
                        }
                       
                        resizeThumbnail(me, x+dx, y, w, h, keepAspectRatio);
                        bounds = view.getBoundsRelativeToMainFrame(resizable.getBounds());
                        lastPos = new Point(bounds.x+4+8,bounds.y+bounds.height+25);
                        break;

                    case Cursor.SE_RESIZE_CURSOR:
                        w += dx;
                        h += dy;
                        if(keepAspectRatio){
                            if(dx>dy){
                                h = Math.round(w * ((float)rh/rw));
                            }else if(dy > dx){            
                                w = Math.round(h * ((float)rw/rh));
                            }
                        }
                        resizeThumbnail(me, x, y, w, h, keepAspectRatio);
                        bounds = view.getBoundsRelativeToMainFrame(resizable.getBounds());
                        lastPos = new Point(bounds.x+bounds.width+4,bounds.y+bounds.height+25);
                        break;
                }
                view.setCursor(Cursor.getPredefinedCursor(cursor));
            }
        }
        
        /**
         * Zobrazí popupmenu pro práci se shcránkou, pokud se jedná o pravé tlačítko myši.
         * @param me 
         */
        private void showPopup(MouseEvent me) {
            if (me.isPopupTrigger() && popupMenu!=null) {
                popupMenu.show(me.getComponent(), me.getX(), me.getY());
            }
        }
        
        /**
         * Zvětší nebo zmenší velikost náhledu obrázku.
         */
        private void resizeThumbnail(MouseEvent me, int x, int y, int w, int h, boolean keepAspectRatio){
           //Nastavit pozici caretu, tam kde má být = před obrázkem (protože jinak skáče skrolování)
           view.getEditor().setCaretPosition(caretPosition);
            
           if(h>=MAX_HIGHT){
               h = MAX_HIGHT;
           }
           if(w>=MAX_WIDTH){
               w = MAX_WIDTH;
           }
           if (!(h < 50) && !(w < 50)) {
               if(keepAspectRatio && h<MAX_HIGHT && w<MAX_WIDTH){
                   resizable.setBounds(x, y, w, h);
                   lastPos = me.getPoint();
               }else if(!keepAspectRatio){
                   resizable.setBounds(x, y, w, h);
                   lastPos = me.getPoint();
               }
           }
           
           //zobrazení hlášky o změně poměru stran v případě potřeby
           if(!keepAspectRatio){
                view.showAspectRatioWarning(me.getPoint(), cursor);
           }else{
                view.hideAspectRatioWarning();
           }
        }
    }
    
    /**
     * Listener pro stisknutí šipek na editoru.
     */
    private class MyKeyListener extends KeyAdapter{
        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            //pokud se jedná o šipky
            if(keyCode==KeyEvent.VK_UP || keyCode==KeyEvent.VK_DOWN || keyCode==KeyEvent.VK_LEFT || keyCode==KeyEvent.VK_RIGHT){
                //Zrušit označení všech obrázků a obnovit viditelnost kurzoru.
                view.setSelectionVisible();
            }
        }
    }
    
    /**
     * Třída představuje periodický hlídač změny nastavení globálního souboru funkcí.
     * Každých deset sekund zkontroluje, zda nastaly změny v souboru na sdíleném uložišti.
     * Pokud změny nastaly, tak podle nich nastaví zobrazení funkcí v editoru.
     */
    private class functionSetupPeriodicChecker implements Runnable{
        @Override
        public void run() {
            if(model.wasRemoteFunctionSetupModified()){
                FunctionSetup setup = model.readGlobalFunctionsSetup();
                if(setup!=null){
                    view.setupFunctions(setup);
                    allowPaste = setup.clipboard;
                }
            }
        }
    }
    
    /**
     * Listener pro přidělení focusu editoru.
     * Stará se o udržení stejného stavu.
     */
    private class EditorFocusListener implements FocusListener{
        /**
         * Viditelnost kurzoru
         */
        boolean visible = true;
        
        @Override
        public void focusGained(FocusEvent e) {
            view.getEditor().getCaret().setVisible(visible);
            view.getEditor().getCaret().setSelectionVisible(visible);
        }

        @Override
        public void focusLost(FocusEvent e) {
            //Zjistit, zda má být kurzor viditelný nebo ne.
            if(IconMyView.getCurrentImageView()!=null){
                visible = !IconMyView.getCurrentImageView().hasFocus();
            }else{
                visible = true;
            }
        }
    }
}

