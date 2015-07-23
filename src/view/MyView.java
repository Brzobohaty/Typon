//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.WindowAdapter;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.ViewFactory;
import model.FunctionSetup;
import model.MyDocument;
import view.EditorViewes.IconMyView;
import view.EditorViewes.PageableViewFactory;

/**
 * Třída, která představuje kompletní GUI.
 */
public class MyView
{
    /************************************************************************************************
    Deklarace statickcých proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Instance této třídy (singleton) View.
     */
    private static MyView view;
    
    /**
     * Povolené koncovky vkládaných obrázků.
     */
    private static final String[] ALLOWED_EXTENSIONS_PICTURE = {"jpg", "png", "gif"};
    
    /**
     * Povolené koncovky otevíraných a ukládaných souborů.
     */
    private static final String[] ALLOWED_EXTENSIONS_FILE = {"typon"};
    
    /**
     * Pole kurzorů pro hrany obrázku. Kurzory zvětšování a zmenšování obrázku.
     */
    private static final int cursors[] = {
           Cursor.N_RESIZE_CURSOR, Cursor.S_RESIZE_CURSOR, Cursor.W_RESIZE_CURSOR,
           Cursor.E_RESIZE_CURSOR, Cursor.NW_RESIZE_CURSOR, Cursor.NE_RESIZE_CURSOR,
           Cursor.SW_RESIZE_CURSOR, Cursor.SE_RESIZE_CURSOR
           };
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Hlavní okno editoru.
     */
    private final MainFrame mainFrame;
    
    /**
     * Formulář pro výběr souboru.
     */
    private final MyFileChooser fileChooser;
    
    /**
     * Formulář pro výběr obrázku.
     */
    private final MyFileChooser imageChooser;
    
    /**
     * Formulář pro zádání jména uživatele.
     */
    private NameForm nameForm;

    /**
     * Editovací okno.
     */
    private final MyTextPane editor;
    
    /**
     * Varovná hláška pro případ, že uživatel mění velikost obrázku v nepoměru stran.
     */
    private final JLabel warningLabel = new JLabel("<html><style> div{"+
                                                            "border: 2px solid #a1a1a1;" +
                                                            "padding: 2px 2px;" +
                                                            "background: #dddddd;" +
                                                            "color: red;"+
                                                         "}</style>"+
                                                    "<div>Deformujete obrázek. <br>Pokud si to nepřejete, držte shift.</div></html>");
            
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    private MyView(){
        mainFrame = MainFrame.getMainFrame();
        setLocalization();
        fileChooser = new MyFileChooser(ALLOWED_EXTENSIONS_FILE);
        imageChooser = new MyFileChooser(ALLOWED_EXTENSIONS_PICTURE);
        this.editor = mainFrame.getEditor();
    }

    /**
     * Tovární metoda. (Singleton)
     * @return instanci View
     */
    public static MyView getView() {
        if(view == null){
            view = new MyView();
        }
        return view;
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * @return editovací okno 
     */
    public MyTextPane getEditor(){
        return editor;
    }
    
    /**
     * @return vyskakovací menu v editoru 
     */
    public JPopupMenu getPopupMenu(){
        return mainFrame.getPopupMenu();
    }
    
    /**
     * Zviditelní GUI
     * @param visible pokud je true, tak je GUI zobrazeno a naopak
     */
    public void setVisible(boolean visible) {
        mainFrame.setExtendedState(MainFrame.MAXIMIZED_BOTH);    
        mainFrame.setVisible(visible);   
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku "vložit obrázek".
     * @param listener
     */
    public void addButtonInsertPictureListener(ActionListener listener) {
        mainFrame.addButtonInsertPictureListener(listener);
    }

    /**
     * Metoda pro přidělen listeneru výběru fontů.
     * @param actionListener listener pro výběr z rozbalovacího seznamu
     */
    public void addComboBoxFontListener(ActionListener actionListener) {
        mainFrame.addComboBoxFontListener(actionListener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku "Nový".
     * @param listener
     */
    public void addMenuNovyListener(ActionListener listener) {
        mainFrame.addMenuNovyListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku "Otevřít".
     * @param listener
     */
    public void addMenuOtevritListener(ActionListener listener) {
        mainFrame.addMenuOtevritListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku "Uložit".
     * @param listener
     */
    public void addMenuUlozitListener(ActionListener listener) {
        mainFrame.addMenuUlozitListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku "Uložit jako".
     * @param listener
     */
    public void addMenuUlozitJakoListener(ActionListener listener) {
        mainFrame.addMenuUlozitJakoListener(listener);
    }

    /**
     * Metoda pro přidělení Listeneru změny stavu pro posuvník velikosti písma.
     * @param listener
     */
    public void addComboBoxFontSizeListener(ActionListener listener) {
        mainFrame.addComboBoxFontSizeListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka "Zarovnání na střed". (stisknuto/nestisknuto)
     * @param listener
     */
    public void addToggleButtonAlignCenterListener(ItemListener listener) {
        mainFrame.addToggleButtonAlignCenterListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka "Zarovnání doleva". (stisknuto/nestisknuto).
     * @param listener
     */
    public void addToggleButtonAlignLeftListener(ItemListener listener) {
        mainFrame.addToggleButtonAlignLeftListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka "tučné písmo". (stisknuto/nestisknuto)
     * @param listener
     */
    public void addToggleButtonBoldListener(ItemListener listener) {
        mainFrame.addToggleButtonBoldListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro označování textu.
     * @param listener
     */
    public void addSelectionListener(CaretListener listener) {
        editor.addCaretListener(listener);
    }
    
    /**
     * Metoda pro přidělení listeneru pro file chooser.
     * @param listener
     */
    public void addFileChooserListener(ActionListener listener) {
        fileChooser.addActionListener(listener);
    }
    
    /**
     * Metoda pro přidělení listeneru pro hlavní okno.
     * @param listener
     */
    public void addMainFrameListener(WindowAdapter listener) {
        mainFrame.addWindowListener(listener);
    }
    
    /**
     * Metoda pro přidělení listeneru pro image chooser.
     * @param listener
     */
    public void addImageChooserListener(ActionListener listener) {
        imageChooser.addActionListener(listener);
    }
    
    /**
     * Metoda pro přidělení listeneru pro eventy myši na editoru a stisknutí kláves.
     * @param mouseListener 
     * @param keyListener 
     */
    public void addEditorListeners(MouseAdapter mouseListener, KeyListener keyListener, FocusListener focusListener){
        editor.addMouseMotionListener(mouseListener);
        editor.addMouseListener(mouseListener);
        editor.addKeyListener(keyListener);
        editor.addFocusListener(focusListener);
    }
    
    /**
     * Metoda pro předělení listenerů položkám vyskakovacího menu pro práci se schránkou.
     * (Zároveň přiděluje ten samí listener tlačítkům na liště.)
     * @param cutListener
     * @param copyListener
     * @param pasteListener 
     */
    public void addPopUpMenuItemsListeners(ActionListener cutListener, ActionListener copyListener, ActionListener pasteListener){
        mainFrame.addPopUpMenuItemsListeners(cutListener, copyListener, pasteListener);
        
    }
    
    /**
     * @param action akce při stisknutí tlačítka Undo
     */
    public void setUndoAction(Action action){
        mainFrame.setUndoAction(action);
    }
    
    /**
     * Vyvolá znovu formulář pro zadání jména uživatele, s předešlími hodnotami.
     */
    public void raiseNameFormAgain() {
        nameForm.setVisible(true);
    }

    /**
     * Vyvolá formulář pro zadání jména uživatele.
     * @param listener pro tlačítko OK
     */
    public void raiseNameForm(ActionListener listener) {
        nameForm = new NameForm(mainFrame);
        nameForm.addButtonOKNameFormListener(listener);
        nameForm.setVisible(true);
    }

    /**
     * Vrátí křestní jméno uživatele z formuláře.
     * @return křestní jméno uživatele z formuláře
     */
    public String getFirstName() {
        return nameForm.getFirstName();	
    }

    /**
     * Vrátí příjmení uživatele z formuláře.
     * @return příjmení uživatele z formuláře
     */
    public String getSurname() {
        return nameForm.getSurname();
    }

    /**
     * Vytvoří chybový dialog s daným textem a tlačítkem OK.
     * @param text který se zobrazí na dialogu jako chybová hláška 
     */
    public void raiseErrorDialog(String text){
        new ErrorDialog(mainFrame,text);
    }
    
    /**
     * Vytvoří dialog pro volbu "ANO" a "NE", který bude obsahovat daný text.
     * @param text který se zobrazí na dialogu 
     * @param title titulek dialogu
     * @return true pokud uživatel stiskl "ANO" a false pokud "NE"
     */
    public boolean raiseOptionDialog(String text, String title){
        Object[] options = {"Ano", "Ne"};
        int confirm = JOptionPane.showOptionDialog(mainFrame, text,title, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null,options,null);
        return confirm == JOptionPane.YES_OPTION;
    }
    
    /**
     * Vytvoří dialog pro volbu "ANO", "NE" a "Cancel" , který bude obsahovat daný text.
     * @param text který se zobrazí na dialogu 
     * @param title titulek dialogu
     * @param textAgreeButton text potvrzovacího tlačítka
     * @param textAbortButton text zamítacího tlačítka
     * @param textCancelButton text stornovacího tlačítka
     * @return 0 = AgreeButton, 1 = AbortButton, 2 = CancelButton
     */
    public int raiseOptionDialogWithCancel(String text, String title, String textAgreeButton, String textAbortButton, String textCancelButton){
        Object[] options = {textAgreeButton, textAbortButton, textCancelButton};
        JOptionPane pane = new JOptionPane(text,JOptionPane.WARNING_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION, null,options,null);
        JDialog dialog = pane.createDialog(mainFrame, title);
        dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        dialog.show();
        String value = pane.getValue().toString();
        if(value.equals(textAgreeButton)){
            return 0;
        }else if(value.equals(textAbortButton)){
            return 1;
        }else{
            return 2;
        }
    }

    //Zobrazí dialog pro uložení souboru.
    public void showSaveDialog() {
        fileChooser.setApproveButtonToolTipText("Uloží obsah editoru do zvoleného souboru.");
        fileChooser.showSaveDialog(mainFrame);
    }
    
    //Zobrazí dialog pro otevření souboru.
    public void showOpenDialog() {
        fileChooser.setApproveButtonToolTipText("Otevře zvolený soubor v editoru.");
        fileChooser.showOpenDialog(mainFrame);
    }
    
    /**
     * Dá focus editovacímu oknu.
     */
    public void focusEditor() {
        editor.requestFocus();
    }

    /**
     * Nastaví hodnotu rozbalovacího seznamu fontů.
     * @param fontName název fontu, který se má nastavit v rozbalovacím menu
     */
    public void setFontComboBox(String fontName){
        mainFrame.setFontComboBox(fontName);
    }
    
    /**
     * Nastaví hodnotu posuvníku velikosti fontů.
     * @param fontSize velikost fontu, který se má nastavit v posuvníku
     */
    public void setFontSizeComboBox(int fontSize){
        mainFrame.setFontSizeComboBox(fontSize);
    }
    
    /**
     * Nastaví stav vypínače pro tučnost písma.
     * @param selected stisknuto/nestiknuto
     */
    public void setBoldButton(boolean selected){
        mainFrame.setBoldButton(selected);
    }
    
    /**
     * Nastaví stav vypínače pro zarovnání doleva.
     * @param selected stisknuto/nestiknuto
     */
    public void setAlignLeftButton(boolean selected){
        mainFrame.setAlignLeftButton(selected);
    }
    
    /**
     * Nastaví stav vypínače pro zarovnání na střed.
     * @param selected stisknuto/nestiknuto
     */
    public void setAlignCenterButton(boolean selected){
        mainFrame.setAlignCenterButton(selected);
    }
    
    /**
     * Nastaví všechny vypínače zarovnání na vypnuto.
     */
    public void clearAlignButtonsSelection(){
        mainFrame.clearAlignButtonsSelection();
    }

    /**
     * Posune kurzor o daný počet znaků dopředu.
     * @param shift 
     */
    public void moveCaretPositionBy(int shift){
        editor.setCaretPosition(editor.getCaretPosition()+shift);
    }
    
    /**
     * Zobrazí dialog pro vložení obrázku.
     */
    public void showImageOpenDialog() {
        imageChooser.setApproveButtonToolTipText("Vloží obrázek.");
        imageChooser.showOpenDialog(mainFrame);
    }
    
    /**
     * Povolí zobrazovaní označení textu a kurzoru.
     */
    public void setSelectionVisible(){
        editor.getCaret().setSelectionVisible(true);
        editor.getCaret().setVisible(true);
        IconMyView.clearAllFocus();
        editor.repaint();
    }
    
    /**
     * Zkontroluje, zda se kurzor nenechází před obrázkem a případně upraví jeho polohu.
     * @param startPosition pozice začátku selekce kurzoru
     * @param endPosition pozice konce selekce kurzoru
     * @throws BadLocationException v případě, že zvolené pozice v dokumentu neexistují
     */
    public void checkCursor(int startPosition, int endPosition) throws BadLocationException{
        editor.checkCursor(startPosition, endPosition);
    }
    
    /**
     * Nastaví indikátor poslední pozice tak, aby se následná korekce pozice chovala správně. 
     */
    public void setCaretWasUsedForEdit(){
        editor.setCaretWasUsedForEdit();
    }
    
    /**
     * Zapne nebo vypne tlačítka pro práci se schránkou (Kopírovat a vložit.).
     * @param enable 
     */
    public void enableCopyCutButtons(boolean enable){
        mainFrame.enableCopyCutButtons(enable);
    }
    
    /**
     * Zapne nebo vypne tlačítko pro vložení obsahu schránky.
     * @param enable 
     */
    public void enablePasteButton(boolean enable){
        mainFrame.enablePasteButton(enable);
    }
    
    /**
     * Znovu vykreslí grafiku editovacího okna.
     */
    public void repaintEditor(){
        editor.repaint();
    }
    
    /**
     * Znovu vykreslí grafiku celého okna.
     */
    public void repaintFrame(){
        mainFrame.repaint();
    }
    
    /**
     * Vykreslí rám okolo obrázku, pokud je na dané potici obrázek.
     * @param point reálné souřadnice v okně
     */
    public void setImageFocus(Point point) {
        IconMyView imageView = getImageOnPosition(point);
        if(imageView!=null){
            //Pokud je kliknuto na obrázek, tak dát obrázku focus.
            setPictureFocus(imageView);
        }
    }
    
    /**
     * Získání informací z původního obrázku, vymaže ho a nahradí větším.
     * @param pressedView view obrázku
     * @param w nová šířka
     * @param h nová výška
     */
    public void changeSizeOfPicture(IconMyView pressedView,int w,int h){
        MyDocument doc = (MyDocument)editor.getDocument();
        BufferedImage original = pressedView.getOriginal();
        int p = pressedView.viewToModel();
        pressedView.setSize(w, h, false);
        try {
            AttributeSet attr = doc.getParagraphElement(p).getAttributes();
            doc.remove(p, 1);
            doc.insertPicture(new ImageIcon(pressedView.getImage()), p);
            MutableAttributeSet at = new SimpleAttributeSet();
            StyleConstants.setAlignment(at, StyleConstants.getAlignment(attr));
            doc.setParagraphAttributes(p, 1, attr, false);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        setNewPicture(p, original);
    }

    /**
     * Dá focus obrázku, označí ho a zruší zobrazování caretu.
     * @param imageView obrázku 
     */
    public void setPictureFocus(IconMyView imageView){ 
        editor.enableCheckingCursor(false);
            editor.getCaret().setDot(imageView.getStartOffset()+1);
            editor.getCaret().moveDot(imageView.getStartOffset());
        editor.enableCheckingCursor(true);
        editor.getCaret().setSelectionVisible(false);
        editor.getCaret().setVisible(false);
        imageView.setFocus(true);
    }

    /**
     * Vrátí příslušný kurzor pro oblast nad obrázkem (ručička nebo zvětšovací kurzory).
     * @param p událost pohybu myši
     * @param view view současného obrázku
     * @return kurzor z třídy Cursor
     */
    public int getCursor(Point p, IconMyView view) {
       if(view!=null && view.hasFocus()){
           for (int i = 0; i < view.locations.length; i++) {
               Rectangle rect = view.getRectangle(view.locations[i]);
               if (rect.contains(p)) {
                   return cursors[i];
               }
           }
       }
       return Cursor.HAND_CURSOR;
    }
    
    /**
     * Zjistí, zda je na dané pozici přímo obrázek (bez rámu) a případně ho rovnou vrátí. 
     * @param mouseLocation pozice v reálných souřadnicíh
     * @return null pokud se nejedná o obrázek
     */
    public IconMyView getImageOnPosition(Point mouseLocation){
       if(mouseLocation!=null){    
           int pos = editor.viewToModel(mouseLocation);
           if (isImage(pos)){
               try {
                   Rectangle rect = editor.modelToView(pos);
                   if(rect.contains(mouseLocation)){
                       return IconMyView.getCurrentImageView();
                   }
               } catch (BadLocationException ex) {
                   LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
               }
           }
       }
       return null;
    }
    
    /**
     * Odstraní právě zobrazený zvětšovatelný náhled obrázku.
     * @param resizable zvětšovatelný náhled obrázku
     */
    public void removeThumbnail(ResizableImage resizable){
        mainFrame.getLayeredPane().remove(resizable);
        editor.repaint();
    }
    
    /**
     * Vytvoří a zobrazí zvětšovatelný náhled obrázku.
     * @param view view obrázku, z kterého má náhled vzniknout
     * @param cursor jaký kurzor se má zobrazot s náhledem
     * @return zvětšovatelný náhled obrázku
     */
    public ResizableImage showThumbnail(IconMyView view, int cursor) {
        Rectangle bounds = SwingUtilities.convertRectangle(editor, view.getBounds(), mainFrame);
        bounds.translate(-8, -30);
        ResizableImage resizable = new ResizableImage(view.getOriginal(),bounds, cursor);
        mainFrame.getLayeredPane().add(resizable);
        view.setFocus(false);
        return resizable;
    }
    
    /**
     * Zobrazí varovnou hlášku, že uživatel mění velikost stran v nepoměru.
     * @param point pozice myši
     * @param cursor typ kurzoru, který právě myš má
     */
    public void showAspectRatioWarning(Point point, int cursor) {
        point = SwingUtilities.convertPoint(editor, point, mainFrame);
        if(cursor==Cursor.W_RESIZE_CURSOR || cursor==Cursor.NW_RESIZE_CURSOR || cursor==Cursor.SW_RESIZE_CURSOR){
            point.translate(-230, 0);
        }else if(cursor==Cursor.N_RESIZE_CURSOR){
            point.translate(0, -130);
        }
        hideAspectRatioWarning();
        warningLabel.setBounds(point.x,point.y,200,100);
        mainFrame.getLayeredPane().add(warningLabel);
    }
    
    /**
     * Skryje hlášku, že uživatel mění velikost obrázku v nepoměru stran.
     */
    public void hideAspectRatioWarning() {
        if(warningLabel!=null){
            mainFrame.getLayeredPane().remove(warningLabel);
            mainFrame.repaint();
        }
    }

    /**
     * Nastaví daný kurzor myši.
     * @param cursor 
     */
    public void setCursor(Cursor cursor) {
        editor.setCursor(cursor);
    }

    /**
     * Transformuje hranice z editoru do hranic hlavního okna.
     * @param bounds původní hranice v editoru
     * @return hranice v hlavním okně
     */
    public Rectangle getBoundsRelativeToMainFrame(Rectangle bounds) {
        return SwingUtilities.convertRectangle(mainFrame, bounds, editor);
    }
    
    /**
     * @return vytvoří novou továrnu na view v editovacím okně 
     */
    public ViewFactory getViewFactory(){
        return new PageableViewFactory();
    }
    
    /**
     * Zobrazí pouze funkce, které jsou v daném nastavení.
     * @param setup nastavení funkcí
     */
    public void setupFunctions(FunctionSetup setup){
        mainFrame.setupFunctions(setup);
    }
    
    /**
     * Změní kurzor na pracující nebo standartní.
     * @param working pracuje právě aplikace?
     */
    public void setWorkingCursor(boolean working){
        if(working){
            mainFrame.setCursor(Cursor.WAIT_CURSOR);
            editor.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        }else{
            mainFrame.setCursor(Cursor.DEFAULT_CURSOR);
            editor.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Nastaví českou lokalizaci některým prvkům.
     */
    private void setLocalization(){
        UIManager.put("FileChooser.lookInLabelText", "Prohledat:");
        UIManager.put("FileChooser.upFolderToolTipText", "O složku výše");
        UIManager.put("FileChooser.homeFolderToolTipText", "Plocha");
        UIManager.put("FileChooser.newFolderToolTipText", "Vytvořit novou složku");
        UIManager.put("FileChooser.listViewButtonToolTipText", "Seznam");
        UIManager.put("FileChooser.detailsViewButtonToolTipText", "Podrobnosti");
        UIManager.put("FileChooser.fileNameLabelText", "Název souboru:");
        UIManager.put("FileChooser.filesOfTypeLabelText", "Typ souboru:");
        UIManager.put("FileChooser.cancelButtonText", "Storno");
        UIManager.put("FileChooser.cancelButtonToolTipText", "Zruší výběr souboru");
        UIManager.put("FileChooser.openDialogTitleText","Otevřít");
        UIManager.put("FileChooser.saveDialogTitleText","Uložit");
        UIManager.put("FileChooser.viewMenuLabelText", "Zobrazení");
        UIManager.put("FileChooser.listViewActionLabelText","Seznam");
        UIManager.put("FileChooser.detailsViewActionLabelText", "Podrobnosti");
        UIManager.put("FileChooser.refreshActionLabelText", "Obnovit");
        UIManager.put("FileChooser.newFolderActionLabelText", "Vytvořit novou složku");
    }

    /**
     * Zjistí zda je na dané pozici obrázek i s rámem. 
     * @param pos pozice v dokumentu
     * @return true, pokud se jedná o obrázek
     */
    private boolean isImage(int pos){
       if (pos >= 0) {
           StyledDocument doc = editor.getStyledDocument();
           Element element = doc.getCharacterElement(pos);
           AttributeSet style = element.getAttributes();
           return style != null && StyleConstants.getIcon(style) != null;
       }
       return false;
    }

    /**
     * Vrátí view obrázku na dané pozici v dokumentu.
     * @param pos pozice obrázku v dokumentu
     * @return view obrázku
     */
    private IconMyView getImageViewOnPosition(int pos) throws BadLocationException{
        editor.modelToView(pos).getLocation();
        return IconMyView.getCurrentImageView();
    }

    /**
     * Vložení obrázku s jinými rozměry do editoru.
     * @param pos pozice obrázku
     * @param original původní obrázek
     */
    private void setNewPicture(int pos, BufferedImage original){
        try {
            IconMyView newView = getImageViewOnPosition(pos);
            newView.setOriginal(original);
            setPictureFocus(newView);
        } catch (BadLocationException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}

