//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import model.FunctionSetup;

/**
 * Třída představuje hlavní okno aplikace.
 * @author Jan Brzobohatý
 */
class MainFrame extends JFrame{
    /************************************************************************************************
    Deklarace statickcých proměnných.
    ************************************************************************************************/
    
    /**
     * Instance této třídy. (Singleton) MainFrame
     */
    private static MainFrame mainFrame;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Editovací okno. (všechny stránky najednou)
     */
    private final MyTextPane editor;
    
    /**
     * Skupina tlačítek pro zarovnání odstavce.
     */
    private final ButtonGroup buttonGroupAlign;
    
    /**
     * Vyskakovací menu v editoru. (práce se schránkou)
     */
    private JPopupMenu popupMenu;
    
    /**
     * Položky ve vyskakovacím menu pro práci se schránkou.
     */
    private JMenuItem cutItem, copyItem, pasteItem;
    
    /**
     * Indikuje, zda má být popupmenu pro schránku aktivní.
     */
    private boolean popupMenuActive = true;
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    /**
     * Creates new MainFrame
     */
    private MainFrame() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        }catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        //</editor-fold>
        
        initComponents();
        
        editor = new MyTextPane();
        panelUnderEditorPane.add(editor);
        
        buttonGroupAlign = new ButtonGroup();
        buttonGroupAlign.add(toggleButtonAlignLeft);
        buttonGroupAlign.add(toggleButtonAlignCenter);
        
        setPopUpMenu();
        setToolTipTexts();
        
        creatFont();
    }
    
    /**
     * Tovární metoda. (Singleton)
     * @return instanci MainFrame
     */
    static MainFrame getMainFrame() {
        if(mainFrame == null){
            mainFrame = new MainFrame();
        }
        return mainFrame;
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * @return editovací pole 
     */
    public MyTextPane getEditor(){
        return editor;
    } 
    
    /**
     * Metoda pro přidělení ActionListeneru tlačítku.
     * @param listener
     */
    public void addButtonInsertPictureListener(ActionListener listener) {
        buttonInsertPicture.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení listenerů výběru fontu.
     * @param listener
     */
    public void addComboBoxFontListener(ActionListener actionListener) {
        comboBoxFont.addActionListener(actionListener); 
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku.
     * @param listener
     */
    public void addMenuNovyListener(ActionListener listener) {
        menuNovy.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku.
     * @param listener
     */
    public void addMenuOtevritListener(ActionListener listener) {
        menuOtevrit.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku.
     * @param listener
     */
    public void addMenuUlozitListener(ActionListener listener) {
        menuUlozit.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení ActionListeneru tlačítku.
     * @param listener
     */
    public void addMenuUlozitJakoListener(ActionListener listener) {
        menuUlozitJako.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení Listeneru změny stavu pro volbu velikosti písma.
     * @param listener
     */
    public void addComboBoxFontSizeListener(ActionListener listener) {
        comboBoxFontSize.addActionListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka "Zarovnání na střed". (stisknuto/nestisknuto)
     * @param listener
     */
    public void addToggleButtonAlignCenterListener(ItemListener listener) {
        toggleButtonAlignCenter.addItemListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka "Zarovnání doleva". (stisknuto/nestisknuto).
     * @param listener
     */
    public void addToggleButtonAlignLeftListener(ItemListener listener) {
        toggleButtonAlignLeft.addItemListener(listener);
    }

    /**
     * Metoda pro přidělení listeneru pro změnu stavu tlačítka. (stisknuto/nestisknuto)
     * @param listener
     */
    public void addToggleButtonBoldListener(ItemListener listener) {
        toggleButtonBold.addItemListener(listener);
    }
    
    /**
     * Metoda pro předělení listenerů položkám vyskakovacího menu pro práci se schránkou.
     * (Zároveň přiděluje ten samí listener tlačítkům na liště.)
     * @param cutListener
     * @param copyListener
     * @param pasteListener 
     */
    public void addPopUpMenuItemsListeners(ActionListener cutListener, ActionListener copyListener, ActionListener pasteListener){
        cutItem.addActionListener(cutListener);
        copyItem.addActionListener(copyListener);
        pasteItem.addActionListener(pasteListener);
        buttonCopy.addActionListener(copyListener);
        buttonCut.addActionListener(cutListener);
        buttonPaste.addActionListener(pasteListener);
    }
    
    /**
     * Nastaví hodnotu rozbalovacího seznamu fontů.
     * @param fontName název fontu, který se má nastavit v rozbalovacím menu
     */
    public void setFontComboBox(String fontName){
        switch (fontName) {
            case "Times New Roman":
                comboBoxFont.setSelectedIndex(0);
                break;
            case "Arial":
                comboBoxFont.setSelectedIndex(1);
                break;
            case "Monotype Corsiva":
                comboBoxFont.setSelectedIndex(2);
                break;
            default:
                comboBoxFont.setSelectedIndex(-1);
                break;
        }
    }
    
    /**
     * Nastaví hodnotu posuvníku velikosti fontů.
     * @param fontSize velikost fontu, který se má nastavit v posuvníku
     */
    public void setFontSizeComboBox(int fontSize){
        switch (fontSize) {
            case 8:
                comboBoxFontSize.setSelectedIndex(0);
                break;
            case 9:
                comboBoxFontSize.setSelectedIndex(1);
                break;
            case 10:
                comboBoxFontSize.setSelectedIndex(2);
                break;
            case 11:
                comboBoxFontSize.setSelectedIndex(3);
                break;
            case 12:
                comboBoxFontSize.setSelectedIndex(4);
                break;
            case 14:
                comboBoxFontSize.setSelectedIndex(5);
                break;
            case 16:
                comboBoxFontSize.setSelectedIndex(6);
                break;
            case 18:
                comboBoxFontSize.setSelectedIndex(7);
                break;
            case 20:
                comboBoxFontSize.setSelectedIndex(8);
                break;
            case 22:
                comboBoxFontSize.setSelectedIndex(9);
                break;
            case 24:
                comboBoxFontSize.setSelectedIndex(10);
                break;
            case 26:
                comboBoxFontSize.setSelectedIndex(11);
                break;
            case 28:
                comboBoxFontSize.setSelectedIndex(12);
                break;
            case 36:
                comboBoxFontSize.setSelectedIndex(13);
                break;
            case 48:
                comboBoxFontSize.setSelectedIndex(14);
                break;
            case 72:
                comboBoxFontSize.setSelectedIndex(15);
                break;
            default:
                comboBoxFontSize.setSelectedIndex(-1);
                break;
        }
    }
    
    /**
     * Nastaví stav vypínače pro tučnost písma.
     * @param selected stisknuto/nestiknuto
     */
    public void setBoldButton(boolean selected){
        toggleButtonBold.setSelected(selected);
    }
    
    /**
     * Nastaví stav vypínače pro zarovnání doleva.
     * @param selected stisknuto/nestiknuto
     */
    public void setAlignLeftButton(boolean selected){
        toggleButtonAlignLeft.setSelected(selected);
    }
    
    /**
     * Nastaví stav vypínače pro zarovnání na střed.
     * @param selected stisknuto/nestiknuto
     */
    public void setAlignCenterButton(boolean selected){
        toggleButtonAlignCenter.setSelected(selected);
    }
    
    /**
     * Nastaví všechny vypínače zarovnání na vypnuto.
     */
    public void clearAlignButtonsSelection(){
        buttonGroupAlign.clearSelection();
    }
    
    /**
     * @param action akce při stisknutí tlačítka Undo
     */
    public void setUndoAction(Action action){
        buttonUndo.setAction(action);
        buttonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Undo-icon.png")));
        String key = "undo";
        buttonUndo.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), key);
        buttonUndo.getActionMap().put(key, action);
        setMyToolTipText(buttonUndo, "Funkce zpět", "Slouží k vrácení poslední změny v dokumentu.", null,"Ctrl+Z");
    }
    
    /**
     * @return vyskakovací menu v editoru 
     */
    public JPopupMenu getPopupMenu(){
        if(popupMenuActive){
            return popupMenu; 
        }
        return null;
    }
    
    /**
     * Zapne nebo vypne tlačítka pro práci se schránkou (Kopírovat a vložit.).
     * @param enable
     */
    public void enableCopyCutButtons(boolean enable){
        buttonCopy.setEnabled(enable);
        buttonCut.setEnabled(enable);
        cutItem.setEnabled(enable);
        copyItem.setEnabled(enable);
    }
    
    /**
     * Zapne nebo vypne tlačítko pro vložení obsahu schránky.
     * @param enable 
     */
    public void enablePasteButton(boolean enable){
        buttonPaste.setEnabled(enable);
        pasteItem.setEnabled(enable);
    }
    
    /**
     * Zobrazí pouze funkce, které jsou v daném nastavení.
     * @param setup nastavení funkcí
     */
    public void setupFunctions(FunctionSetup setup){
        buttonsPanel.removeAll();
        panelSettingsFont2.removeAll();
       
        if(setup.undo){
            buttonsPanel.add(panelForButtonUndo1);
        }
        
        if(setup.clipboard){
            buttonsPanel.add(panelClipBord1);
            popupMenuActive = true;
        }else{
            popupMenuActive = false;
        }
        
        setupFontPanel(setup);
        
        if(setup.align){
            buttonsPanel.add(panelSettingsParagraph1);
        }
        
        if(setup.picture){
            buttonsPanel.add(panelForButtonInsertPicture1);
        }
        myPack();
        repaint();
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Udělá pack pro celé okno, aniž by se měnila velikost.
     */
    private void myPack(){
        setMinimumSize(getSize());
        pack();
        setMinimumSize(new Dimension(900,600));
    }
    
    /**
     * Nastaví vyskakovací menu pro editor. (práce se schránkou)
     */
    private void setPopUpMenu(){
        popupMenu = new JPopupMenu("Menu");

        cutItem = new JMenuItem("Vyjmout");
        popupMenu.add(cutItem);
        
        popupMenu.addSeparator();

        copyItem = new JMenuItem("Kopírovat");
        popupMenu.add(copyItem);
        
        popupMenu.addSeparator();

        pasteItem = new JMenuItem("Vložit");
        popupMenu.add(pasteItem);
    }
    
    /**
     * Nastavuje tool tip texty tlačítkům.
     */
    private void setToolTipTexts(){
        ToolTipManager.sharedInstance().setInitialDelay(1000);
        ToolTipManager.sharedInstance().setReshowDelay(0);
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
        setMyToolTipText(buttonInsertPicture, "Vložit obrázek", "Vloží obrázek do dokumentu na místo kurzoru.", null,null);
        setMyToolTipText(toggleButtonAlignCenter, "Zarovnat na střed", "Řádky odstavce budou mít stejné množství znaků nalevo i napravo od středu řádku.", "<li>Básnička</li><li>Nadpis</li>",null);
        setMyToolTipText(toggleButtonAlignLeft, "Zarovnat doleva", "Všechny řádky odstavce budou začínat u levého okraje stránky.", "<li>Běžný text</li>",null);
        setMyToolTipText(toggleButtonBold, "Tučné písmo", "Udělá písmo tlustší.", "<li>Zvýraznění</li><li>Nadpis</li>",null);
        setMyToolTipText(comboBoxFont, "Font písma", "Změní font (typ) písma.<br> Font určuje vzhled znaků.", "<li>Patkové písmo pro běžný text (Times New Roman)</li><li>Bezpatkové písmo pro nadpisy (Arial)</li>",null);
        setMyToolTipText(comboBoxFontSize, "Velikost písma", "Změní velikost písma na nastavenou hodnotu.", "<li>12 obvykle pro běžný text</li><li>Větší pro nadpisy</li>",null);
        menuNovy.setToolTipText("Vytvoří nový prázdný dokument.");
        menuOtevrit.setToolTipText("Otevře vybraný soubor v editoru.");
        menuUlozit.setToolTipText("Uloží současný dokument do souboru.");
        menuUlozitJako.setToolTipText("Uloží současný dokument do vybraného souboru.");
        setMyToolTipText(buttonCopy, "Kopírovat", "Zkopíruje označenou část dokumentu do schránky.<br> Označený obsah zůstane v dokumentu a zároven bude vložen do schránky.<br> Obsah schránky bude přepsán.", null, "Ctrl+C");
        setMyToolTipText(buttonCut, "Vyjmout", "Vyjme označenou část dokumentu a uloží jí do schránky.<br> Označený obsah bude smazán z dokumentu a zároven bude vložen do schránky.<br> Obsah schránky bude přepsán.", null, "Ctrl+X");
        setMyToolTipText(buttonPaste, "Vložit", "Vloží na místo kurzoru obsah schránky.", null, "Ctrl+V");
        
        //Takto se barva zobrazí i na vypnutých tlačítkách
        UIManager.put("ToolTip.background", new Color(242,242,189));
    }
    
    /**
     * Nastaví standartizovaný tool tip text pro danou komponentu.
     * @param component komponenta, pro kterou se tool tip text nastavuje
     * @param titulek nadpis tool tip textu
     * @param funkce popis funkce kompennty
     * @param pouziti příklady pouziti komponenty (každý příklad musí být uzavřen v <li></li>) (může být null)
     */
    private void setMyToolTipText(JComponent component, String titulek, String funkce, String pouziti, String zkratka){
        String pouzitiHTML;
        if(pouziti==null){
            pouzitiHTML = "";
        }else{
            pouzitiHTML = "<br><b>Příklady použití:</b><br><ul style=\"margin:0px;padding-left:20px;\">"+pouziti+"</ul>";
        }
        String zkratkaHTML;
        if(zkratka==null){
            zkratkaHTML = "";
        }else{
            zkratkaHTML = "<br><b>Zkratka: </b>"+zkratka;
        }
        component.setToolTipText("<html><h3 style=\"text-align:center\">"+titulek+"</h3> <b>Funkce:</b><br>"+funkce+pouzitiHTML+zkratkaHTML+"</html>");
    }
    
    /**
     * Nastaví konkrétně panel s fontem, velikostí fontu a tlačítkem pro tučné písmo 
     * @param setup nastavení funkcí
     */
    private void setupFontPanel(FunctionSetup setup) {
        if(setup.font || setup.fontSize || setup.bold){
            buttonsPanel.add(panelSettingsFont1);
            int width = 0;
            if(setup.font){
                panelSettingsFont2.add(comboBoxFont);
                width += 20+comboBoxFont.getPreferredSize().width;
            }
            if(setup.fontSize){
                panelSettingsFont2.add(comboBoxFontSize);
                width += 20+comboBoxFontSize.getPreferredSize().width;
            }
            if(setup.bold){
                panelSettingsFont2.add(toggleButtonBold);
                width += 20+toggleButtonBold.getPreferredSize().width;
            }
            if(width<120){
                width = 120;
            }
            panelSettingsFont1.setPreferredSize(new Dimension(width,60));
        }
    }
    
    private void creatFont(){
        //Vytvoření fontu Monotype Corsiva
        try {
            InputStream myStream = getClass().getResourceAsStream("/Graphics/MTCORSVA.ttf");
            Font font = Font.createFont(Font.TRUETYPE_FONT, myStream);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
        } catch (FontFormatException | IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonsPanel = new GradientPanel();
        panelForButtonUndo1 = new javax.swing.JPanel();
        panelForButtonUndo2 = new javax.swing.JPanel();
        buttonUndo = new MyButton();
        panelClipBord1 = new javax.swing.JPanel();
        labelSchranka = new javax.swing.JLabel();
        panelClipBord2 = new javax.swing.JPanel();
        buttonCopy = new MyButton();
        buttonCut = new MyButton();
        buttonPaste = new MyButton();
        panelSettingsFont1 = new javax.swing.JPanel();
        labelNastaveniPisma = new javax.swing.JLabel();
        panelSettingsFont2 = new javax.swing.JPanel();
        comboBoxFont = new MyComboBox();
        comboBoxFontSize = new MyComboBox();
        toggleButtonBold = new MyToggleButton();
        panelSettingsParagraph1 = new javax.swing.JPanel();
        panelSettingsParagraph2 = new javax.swing.JPanel();
        toggleButtonAlignLeft = new MyToggleButton();
        toggleButtonAlignCenter = new MyToggleButton();
        labelNastaveniOdstavce = new javax.swing.JLabel();
        panelForButtonInsertPicture1 = new javax.swing.JPanel();
        panelForButtonInsertPicture2 = new javax.swing.JPanel();
        buttonInsertPicture = new MyButton();
        labelNastaveniOdstavce1 = new javax.swing.JLabel();
        scrollPaneEditor = new javax.swing.JScrollPane();
        panelUnderEditorPane = new javax.swing.JPanel();
        bottomCoverPanel = new GradientPanel2();
        horniMenu = new javax.swing.JMenuBar();
        menuSoubor = new javax.swing.JMenu();
        menuNovy = new MyMenuItem();
        separatorMenuSoubor1 = new javax.swing.JPopupMenu.Separator();
        menuOtevrit = new MyMenuItem();
        separatorMenuSoubor2 = new javax.swing.JPopupMenu.Separator();
        menuUlozit = new MyMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuUlozitJako = new MyMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Typoň");
        setBackground(new java.awt.Color(214, 217, 223));
        setFocusCycleRoot(false);
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Graphics/Text-Editor-icon.png"))
        );
        setMinimumSize(new java.awt.Dimension(1020, 600));

        buttonsPanel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        buttonsPanel.setFocusable(false);
        buttonsPanel.setMinimumSize(new java.awt.Dimension(0, 50));
        buttonsPanel.setPreferredSize(new java.awt.Dimension(10, 80));
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING, 0, 5));

        panelForButtonUndo1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200,200,200)));
        panelForButtonUndo1.setFocusable(false);
        panelForButtonUndo1.setInheritsPopupMenu(true);
        panelForButtonUndo1.setMinimumSize(new java.awt.Dimension(20, 54));
        panelForButtonUndo1.setOpaque(false);
        panelForButtonUndo1.setPreferredSize(new java.awt.Dimension(70, 60));
        panelForButtonUndo1.setLayout(new java.awt.BorderLayout());

        panelForButtonUndo2.setFocusable(false);
        panelForButtonUndo2.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelForButtonUndo2.setMinimumSize(new java.awt.Dimension(214, 40));
        panelForButtonUndo2.setOpaque(false);
        panelForButtonUndo2.setPreferredSize(new java.awt.Dimension(300, 40));
        java.awt.FlowLayout flowLayout3 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0);
        flowLayout3.setAlignOnBaseline(true);
        panelForButtonUndo2.setLayout(flowLayout3);

        buttonUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Undo-icon.png"))); // NOI18N
        buttonUndo.setHideActionText(true);
        buttonUndo.setPreferredSize(new java.awt.Dimension(50, 40));
        buttonUndo.setRequestFocusEnabled(false);
        panelForButtonUndo2.add(buttonUndo);

        panelForButtonUndo1.add(panelForButtonUndo2, java.awt.BorderLayout.PAGE_START);

        buttonsPanel.add(panelForButtonUndo1);

        panelClipBord1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200,200,200)));
        panelClipBord1.setAlignmentX(0.0F);
        panelClipBord1.setAlignmentY(0.0F);
        panelClipBord1.setFocusable(false);
        panelClipBord1.setMinimumSize(new java.awt.Dimension(200, 54));
        panelClipBord1.setOpaque(false);
        panelClipBord1.setPreferredSize(new java.awt.Dimension(180, 60));
        panelClipBord1.setLayout(new java.awt.BorderLayout());

        labelSchranka.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelSchranka.setText("Schránka");
        labelSchranka.setFocusable(false);
        labelSchranka.setName(""); // NOI18N
        labelSchranka.setPreferredSize(new java.awt.Dimension(78, 13));
        panelClipBord1.add(labelSchranka, java.awt.BorderLayout.CENTER);

        panelClipBord2.setFocusable(false);
        panelClipBord2.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelClipBord2.setMinimumSize(new java.awt.Dimension(214, 40));
        panelClipBord2.setOpaque(false);
        panelClipBord2.setPreferredSize(new java.awt.Dimension(300, 40));
        java.awt.FlowLayout flowLayout7 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0);
        flowLayout7.setAlignOnBaseline(true);
        panelClipBord2.setLayout(flowLayout7);

        buttonCopy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Copy-icon.png"))); // NOI18N
        buttonCopy.setHideActionText(true);
        buttonCopy.setPreferredSize(new java.awt.Dimension(50, 40));
        buttonCopy.setRequestFocusEnabled(false);
        panelClipBord2.add(buttonCopy);

        buttonCut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Cut-icon.png"))); // NOI18N
        buttonCut.setHideActionText(true);
        buttonCut.setPreferredSize(new java.awt.Dimension(50, 40));
        buttonCut.setRequestFocusEnabled(false);
        panelClipBord2.add(buttonCut);

        buttonPaste.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Paste-icon.png"))); // NOI18N
        buttonPaste.setHideActionText(true);
        buttonPaste.setPreferredSize(new java.awt.Dimension(50, 40));
        buttonPaste.setRequestFocusEnabled(false);
        panelClipBord2.add(buttonPaste);

        panelClipBord1.add(panelClipBord2, java.awt.BorderLayout.PAGE_START);
        panelClipBord2.setPreferredSize(panelClipBord2.getPreferredSize());

        buttonsPanel.add(panelClipBord1);
        panelClipBord1.setPreferredSize(new Dimension(panelClipBord2.getComponent(0).getPreferredSize().width+20+panelClipBord2.getComponent(1).getPreferredSize().width+20+panelClipBord2.getComponent(2).getPreferredSize().width+20,60));

        panelSettingsFont1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200,200,200)));
        panelSettingsFont1.setAlignmentX(0.0F);
        panelSettingsFont1.setAlignmentY(0.0F);
        panelSettingsFont1.setFocusable(false);
        panelSettingsFont1.setMinimumSize(new java.awt.Dimension(20, 54));
        panelSettingsFont1.setOpaque(false);
        panelSettingsFont1.setPreferredSize(new java.awt.Dimension(320, 60));
        panelSettingsFont1.setLayout(new java.awt.BorderLayout());

        labelNastaveniPisma.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelNastaveniPisma.setText("Nastavení písma");
        labelNastaveniPisma.setFocusable(false);
        labelNastaveniPisma.setName(""); // NOI18N
        labelNastaveniPisma.setPreferredSize(new java.awt.Dimension(78, 13));
        panelSettingsFont1.add(labelNastaveniPisma, java.awt.BorderLayout.CENTER);

        panelSettingsFont2.setFocusable(false);
        panelSettingsFont2.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelSettingsFont2.setMinimumSize(new java.awt.Dimension(21, 40));
        panelSettingsFont2.setOpaque(false);
        panelSettingsFont2.setPreferredSize(new java.awt.Dimension(30, 40));
        java.awt.FlowLayout flowLayout1 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0);
        flowLayout1.setAlignOnBaseline(true);
        panelSettingsFont2.setLayout(flowLayout1);

        comboBoxFont.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Times New Roman", "Arial", "Monotype Corsiva" }));
        comboBoxFont.setFocusable(false);
        comboBoxFont.setPreferredSize(new java.awt.Dimension(150, 30));
        panelSettingsFont2.add(comboBoxFont);
        comboBoxFont.setRenderer(new ComboRenderer());

        comboBoxFontSize.setMaximumRowCount(16);
        comboBoxFontSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72" }));
        comboBoxFontSize.setSelectedIndex(4);
        comboBoxFontSize.setFocusable(false);
        comboBoxFontSize.setPreferredSize(new java.awt.Dimension(60, 30));
        comboBoxFontSize.setRequestFocusEnabled(false);
        comboBoxFontSize.setVerifyInputWhenFocusTarget(false);
        panelSettingsFont2.add(comboBoxFontSize);
        comboBoxFont.setRenderer(new ComboRenderer());

        toggleButtonBold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Bold-icon.png"))); // NOI18N
        toggleButtonBold.setFocusable(false);
        toggleButtonBold.setMargin(new java.awt.Insets(2, 8, 2, 8));
        toggleButtonBold.setPreferredSize(new java.awt.Dimension(50, 40));
        toggleButtonBold.setRequestFocusEnabled(false);
        panelSettingsFont2.add(toggleButtonBold);

        panelSettingsFont1.add(panelSettingsFont2, java.awt.BorderLayout.PAGE_START);
        panelSettingsFont2.setPreferredSize(panelSettingsFont2.getPreferredSize());

        buttonsPanel.add(panelSettingsFont1);
        panelSettingsFont1.setPreferredSize(new Dimension(panelSettingsFont2.getComponent(0).getPreferredSize().width+20+panelSettingsFont2.getComponent(1).getPreferredSize().width+20+panelSettingsFont2.getComponent(2).getPreferredSize().width+20,60));

        panelSettingsParagraph1.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(200,200,200)));
        panelSettingsParagraph1.setFocusable(false);
        panelSettingsParagraph1.setMinimumSize(new java.awt.Dimension(0, 55));
        panelSettingsParagraph1.setOpaque(false);
        panelSettingsParagraph1.setPreferredSize(new java.awt.Dimension(150, 60));
        panelSettingsParagraph1.setLayout(new java.awt.BorderLayout());

        panelSettingsParagraph2.setFocusable(false);
        panelSettingsParagraph2.setMinimumSize(new java.awt.Dimension(0, 41));
        panelSettingsParagraph2.setOpaque(false);
        panelSettingsParagraph2.setPreferredSize(new java.awt.Dimension(50, 40));
        java.awt.FlowLayout flowLayout2 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0);
        flowLayout2.setAlignOnBaseline(true);
        panelSettingsParagraph2.setLayout(flowLayout2);

        toggleButtonAlignLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Align-Left-icon.png"))); // NOI18N
        toggleButtonAlignLeft.setFocusable(false);
        toggleButtonAlignLeft.setMargin(new java.awt.Insets(2, 8, 2, 8));
        toggleButtonAlignLeft.setPreferredSize(new java.awt.Dimension(50, 40));
        toggleButtonAlignLeft.setRequestFocusEnabled(false);
        panelSettingsParagraph2.add(toggleButtonAlignLeft);

        toggleButtonAlignCenter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Align-Center-icon.png"))); // NOI18N
        toggleButtonAlignCenter.setFocusable(false);
        toggleButtonAlignCenter.setMargin(new java.awt.Insets(2, 8, 2, 8));
        toggleButtonAlignCenter.setPreferredSize(new java.awt.Dimension(50, 40));
        toggleButtonAlignCenter.setRequestFocusEnabled(false);
        panelSettingsParagraph2.add(toggleButtonAlignCenter);

        panelSettingsParagraph1.add(panelSettingsParagraph2, java.awt.BorderLayout.PAGE_START);

        labelNastaveniOdstavce.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelNastaveniOdstavce.setText("Nastavení odstavce");
        labelNastaveniOdstavce.setFocusable(false);
        panelSettingsParagraph1.add(labelNastaveniOdstavce, java.awt.BorderLayout.CENTER);

        buttonsPanel.add(panelSettingsParagraph1);

        panelForButtonInsertPicture1.setFocusable(false);
        panelForButtonInsertPicture1.setMinimumSize(new java.awt.Dimension(20, 54));
        panelForButtonInsertPicture1.setOpaque(false);
        panelForButtonInsertPicture1.setPreferredSize(new java.awt.Dimension(70, 60));
        panelForButtonInsertPicture1.setLayout(new java.awt.BorderLayout());

        panelForButtonInsertPicture2.setFocusable(false);
        panelForButtonInsertPicture2.setMaximumSize(new java.awt.Dimension(32767, 40));
        panelForButtonInsertPicture2.setMinimumSize(new java.awt.Dimension(214, 40));
        panelForButtonInsertPicture2.setOpaque(false);
        panelForButtonInsertPicture2.setPreferredSize(new java.awt.Dimension(300, 40));
        java.awt.FlowLayout flowLayout4 = new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0);
        flowLayout4.setAlignOnBaseline(true);
        panelForButtonInsertPicture2.setLayout(flowLayout4);

        buttonInsertPicture.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/insert-image-icon.png"))); // NOI18N
        buttonInsertPicture.setFocusable(false);
        buttonInsertPicture.setPreferredSize(new java.awt.Dimension(50, 40));
        buttonInsertPicture.setRequestFocusEnabled(false);
        panelForButtonInsertPicture2.add(buttonInsertPicture);

        panelForButtonInsertPicture1.add(panelForButtonInsertPicture2, java.awt.BorderLayout.PAGE_START);

        labelNastaveniOdstavce1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelNastaveniOdstavce1.setText("Obrázek");
        labelNastaveniOdstavce1.setFocusable(false);
        panelForButtonInsertPicture1.add(labelNastaveniOdstavce1, java.awt.BorderLayout.CENTER);

        buttonsPanel.add(panelForButtonInsertPicture1);

        getContentPane().add(buttonsPanel, java.awt.BorderLayout.PAGE_START);

        scrollPaneEditor.setBackground(new java.awt.Color(214, 217, 223));
        scrollPaneEditor.setBorder(null);
        scrollPaneEditor.setOpaque(false);
        scrollPaneEditor.getVerticalScrollBar().setUnitIncrement(16);

        panelUnderEditorPane.setBackground(new java.awt.Color(214, 217, 223));
        panelUnderEditorPane.setMinimumSize(new java.awt.Dimension(935, 1200));
        panelUnderEditorPane.setOpaque(false);
        panelUnderEditorPane.setLayout(new java.awt.GridBagLayout());
        scrollPaneEditor.setViewportView(panelUnderEditorPane);

        getContentPane().add(scrollPaneEditor, java.awt.BorderLayout.CENTER);

        bottomCoverPanel.setFocusable(false);
        bottomCoverPanel.setMinimumSize(new java.awt.Dimension(0, 10));
        bottomCoverPanel.setPreferredSize(new java.awt.Dimension(4, 20));

        javax.swing.GroupLayout bottomCoverPanelLayout = new javax.swing.GroupLayout(bottomCoverPanel);
        bottomCoverPanel.setLayout(bottomCoverPanelLayout);
        bottomCoverPanelLayout.setHorizontalGroup(
            bottomCoverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 935, Short.MAX_VALUE)
        );
        bottomCoverPanelLayout.setVerticalGroup(
            bottomCoverPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        getContentPane().add(bottomCoverPanel, java.awt.BorderLayout.PAGE_END);

        horniMenu.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        horniMenu.setFocusable(false);

        menuSoubor.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        menuSoubor.setText("Soubor");
        menuSoubor.setFocusPainted(true);
        menuSoubor.setFont(new java.awt.Font("Segoe UI", 0, 16)); // NOI18N

        menuNovy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/new-file-icon.png"))); // NOI18N
        menuNovy.setText("Nový");
        menuSoubor.add(menuNovy);
        menuSoubor.add(separatorMenuSoubor1);

        menuOtevrit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/open-file-icon.png"))); // NOI18N
        menuOtevrit.setText("Otevřít");
        menuSoubor.add(menuOtevrit);
        menuSoubor.add(separatorMenuSoubor2);

        menuUlozit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Save-icon.png"))); // NOI18N
        menuUlozit.setText("Uložit");
        menuSoubor.add(menuUlozit);
        menuSoubor.add(jSeparator1);

        menuUlozitJako.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Graphics/Save-as-icon.png"))); // NOI18N
        menuUlozitJako.setText("Uložit jako");
        menuSoubor.add(menuUlozitJako);

        horniMenu.add(menuSoubor);

        setJMenuBar(horniMenu);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomCoverPanel;
    private javax.swing.JButton buttonCopy;
    private javax.swing.JButton buttonCut;
    private javax.swing.JButton buttonInsertPicture;
    private javax.swing.JButton buttonPaste;
    private javax.swing.JButton buttonUndo;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JComboBox comboBoxFont;
    private javax.swing.JComboBox comboBoxFontSize;
    private javax.swing.JMenuBar horniMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JLabel labelNastaveniOdstavce;
    private javax.swing.JLabel labelNastaveniOdstavce1;
    private javax.swing.JLabel labelNastaveniPisma;
    private javax.swing.JLabel labelSchranka;
    private javax.swing.JMenuItem menuNovy;
    private javax.swing.JMenuItem menuOtevrit;
    private javax.swing.JMenu menuSoubor;
    private javax.swing.JMenuItem menuUlozit;
    private javax.swing.JMenuItem menuUlozitJako;
    private javax.swing.JPanel panelClipBord1;
    private javax.swing.JPanel panelClipBord2;
    private javax.swing.JPanel panelForButtonInsertPicture1;
    private javax.swing.JPanel panelForButtonInsertPicture2;
    private javax.swing.JPanel panelForButtonUndo1;
    private javax.swing.JPanel panelForButtonUndo2;
    private javax.swing.JPanel panelSettingsFont1;
    private javax.swing.JPanel panelSettingsFont2;
    private javax.swing.JPanel panelSettingsParagraph1;
    private javax.swing.JPanel panelSettingsParagraph2;
    private javax.swing.JPanel panelUnderEditorPane;
    private javax.swing.JScrollPane scrollPaneEditor;
    private javax.swing.JPopupMenu.Separator separatorMenuSoubor1;
    private javax.swing.JPopupMenu.Separator separatorMenuSoubor2;
    private javax.swing.JToggleButton toggleButtonAlignCenter;
    private javax.swing.JToggleButton toggleButtonAlignLeft;
    private javax.swing.JToggleButton toggleButtonBold;
    // End of variables declaration//GEN-END:variables

    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Třída představující panel s šedým gradientem.
     */
    private static class GradientPanel extends JPanel {
        
        public GradientPanel() {
            super();
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = getBackground();
            Color color2 = Color.WHITE;
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }
    
    /**
     * Třída představující panel s šedým gradientem.
     */
    private static class GradientPanel2 extends JPanel {
        
        public GradientPanel2() {
            super();
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            Color color1 = getBackground();
            Color color2 = color1.darker();//new Color(190, 191, 194);
            int w = getWidth();
            int h = getHeight();
            GradientPaint gp = new GradientPaint(0, 0, color2, 0, h, color1);
            g2d.setPaint(gp);
            g2d.fillRect(0, 0, w, h);
        }
    }
    
    /**
     * Třída představuje dvoustavové tlačítko pro funkce na tlačítkové liště.
     * Oproti normálnímu tlačítku je zde vykreslen 
     * Má také přemístěný Tool tip text.
     * oranžový obrys při sepnuté poloze tlačítka.
     */
    private static class MyToggleButton extends JToggleButton{

        public MyToggleButton() {
            super();
        }
        
        @Override
        public Point getToolTipLocation(MouseEvent e) {
            JPanel buttonPanel = (JPanel) getParent().getParent();
            return new Point(0, buttonPanel.getLocation().y+buttonPanel.getHeight()-getLocation().y);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            if (getModel().isSelected()){
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(getBackground());
            }
            g.fillRoundRect(-1, -1, getWidth()+1, getHeight()+1, 10, 10);
            super.paintComponent(g);
        }
    }
    
    /**
     * Třída představuje standartní tlačítko akorát s přemístěným tool tip textem.
     * @author Jan Brzobohatý
     */
    private class MyButton extends JButton{
        @Override
        public Point getToolTipLocation(MouseEvent e) {
            JPanel buttonPanel = (JPanel) getParent().getParent();
            return new Point(0, buttonPanel.getLocation().y+buttonPanel.getHeight()-getLocation().y);
        }
    }
    
    /**
     * Třída představuje standartní vysouvací seznam akorát s přemístěným tool tip textem.
     * @author Jan Brzobohatý
     */
    private class MyComboBox extends JComboBox{
        @Override
        public Point getToolTipLocation(MouseEvent e) {
            JPanel buttonPanel = (JPanel) getParent().getParent();
            return new Point(0, buttonPanel.getLocation().y+buttonPanel.getHeight()-getLocation().y);
        }
    }
    
    /**
     * Třída představuje standartní položku menu akorát s přemístěným tool tip textem.
     * @author Jan Brzobohatý
     */
    private class MyMenuItem extends JMenuItem{
        @Override
        public Point getToolTipLocation(MouseEvent e) {
            return new Point(getWidth()+2, 0);
        }
    }
    
    /**
     * Třída představuje vykreslovač pro Combo Box.
     * Konkrétně tento vykreslovač vykreslí všechny položky Combo 
     * boxu s fontem, který odpovídá jejich hodnotě.
     */
    private static class ComboRenderer extends BasicComboBoxRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            setText((String)value);
            final String fontFamilyName = (String) value;
            setFont(new Font(fontFamilyName, Font.PLAIN, 16));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            
            if(isSelected){
                setBackground(new Color(57,105,138));
                setForeground(Color.WHITE);
            }
            
            return this;
        }
    }
}
