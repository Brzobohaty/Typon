//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.text.BadLocationException;


/**
 * Třída se stará o veškerou práci se soubory.
 */
class FileHandler{
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Objekt představuje formát datumu "yyyy_MM_dd_HH_mm_ss".
     */
    private final DateFormat dateFormat;
    
    /**
     * Soubor v kterém jsou uložena data na lokálním souborovém systému.
     */
    private File localFile;
    
    /**
     * Model editovacího okna.
     */
    private MyEditorKit editorKit;
    
    /**
     * Název konfiguračního souboru, který obsahuje cestu ke vzdálenému uložišti.
     */
    private final String fileRemoteSaveConfigurationName = "konfigurace_sdileneho_uloziste.txt";
    
    /**
     * Název konfiguračního souboru, který obsahuje cestu ke globálnímu nastavení funkcí editoru.
     */
    private final String fileRemoteFunctionSetupConfigurationName = "konfigurace_sdileneho_nastaveni.txt";
    
    /**
     * Název souboru s globálním nastavením funkcí.
     */
    private final String fileFunctionConfigurationName = "GlobalniNastaveniFunkciTypone";
    
    /**
     * Složka vzdáleného uložiště.
     */
    private File remoteSavingDirectory = null;
    
    /**
     * Soubor vzdáleného nastavení funkcí.
     */
    private File remoteFunctionsSetup = null;

    /**
     * Poslední modofikace souboru s globálním nastavením funkcí.
     */
    private Long lastModificationFunctionSetup = 0L;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    FileHandler(){
        dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        readLocalConfigurationFiles();
    }

    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * Uloží obsah dokumentu do zvoleného souboru.
     * @param file soubor, do kterého se má uložit obsah dokumentu (může být null)
     * @param document dokument s obsahem
     * @throws java.io.IOException Pokud nastala chyba při zapisování.
     * @throws java.io.FileNotFoundException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws javax.swing.text.BadLocationException Pokud byla nastavena pozice, která v dokumentu neexistuje.
     */
    public void saveFile(File file, MyDocument document) throws FileNotFoundException, IOException, BadLocationException{
        if(file==null){
            file = localFile;
        }
        if(!file.toString().endsWith(".typon")){
            file = new File(file.getPath()+".typon");
        }
        
        //try (FileOutputStream fos = new FileOutputStream(file); OutputStreamWriter writer = new OutputStreamWriter(fos)) {
        try(FileWriter writer = new FileWriter(file)){
            editorKit.write(writer, document, 0, document.getLength());
        }
        localFile = file;
    }

    /**
     * Uloží soubor do nastaveného sdíleného úložiště s názvem „RRRR_MM_DD_HH_MM_SS_přijmení_jméno“.
     * @throws java.io.IOException Pokud nastala chyba při zapisování.
     * @throws java.io.FileNotFoundException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws javax.swing.text.BadLocationException Pokud byla nastavena pozice, která v dokumentu neexistuje.
     */
    public void saveRemoteFile(MyDocument document, String userFirstName, String userSurename) throws FileNotFoundException, IOException, BadLocationException{
        if(remoteSavingDirectory!=null){
            File file = new File(remoteSavingDirectory,dateFormat.format(new Date())+"_"+userSurename+"_"+userFirstName+".typon");
            saveFile(file, document);
        }
    }
    
    /**
     * @return soubor pro lokální uložení
     */
    public File getLocalFile(){
        return localFile;
    }
    
    /**
     * Vymaže referenci na uložený soubor.
     */
    public void clearLocalFile(){
        localFile=null;
    }
    
    /**
     * Otevře obrázek.
     * @param file soubor, ze kterého se má obrázek načíst
     * @throws IOException Pokud nastala chyba při čtení.
     * @return obrázek
     */
    public BufferedImage openImage(File file) throws IOException{
        BufferedImage img = ImageIO.read(file);
        return img;
    }
    
    /**
     * Předá EditorKit editovacího okna.
     * @param editorKit 
     */
    public void setEditorKit(MyEditorKit editorKit){
        this.editorKit = editorKit;
    }
    
    /**
     * Otevře v editoru obsah RTF souboru.
     * @param file soubor s RTF obsahem
     * @param document dokument editovacího okna
     * @throws IOException pokud nastane chyba při čtení ze souboru
     * @throws FileNotFoundException pokud daný soubor nebyl nalezen
     */
    public void readFile(File file, MyDocument document) throws FileNotFoundException, IOException{
        //try(FileInputStream fis = new FileInputStream(file);InputStreamReader reader = new InputStreamReader(fis)){
        try(FileReader reader = new FileReader(file)){
            editorKit.read(reader, document, 0);
        }catch(BadLocationException ex){
            //nemůže nastat, protože jsme zadali pozici v dokumentu natvrdo na 0
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * @return zda existuje soubor s nastavením vzdáleného uložiště 
     */
    public boolean isRemoteFileSetup(){
        return new File(fileRemoteSaveConfigurationName).exists();
    }
    
    /**
     * @return zda existuje soubor s cestou ke globálnímu nastavením funkcí 
     */
    public boolean isRemoteFunctionSettingsSetup(){
        return new File(fileRemoteFunctionSetupConfigurationName).exists();
    }
    
    /**
     * Přečte soubor s globálním nastavením funkcí a vrátí jeho obsah v podobě nastavení funkcí.
     * @return nastavení funkcí (může být null)
     */
    public FunctionSetup readGlobalFunctionsSetup(){
        lastModificationFunctionSetup = remoteFunctionsSetup.lastModified();
        if(remoteFunctionsSetup!=null){
            try (FileReader reader = new FileReader(remoteFunctionsSetup); BufferedReader br = new BufferedReader(reader)) {
                return new FunctionSetup(readOneFunction(br), readOneFunction(br), readOneFunction(br), readOneFunction(br), readOneFunction(br), readOneFunction(br), readOneFunction(br));
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return null;
    }
    
    /**
     * @return zda byl modifikován soubor s globálním nastavením funkcí od poslední aktualizace
     * Pokud nebyl nastaven takový soubor, tak vrací false.
     */
    public boolean wasRemoteFunctionSetupModified(){
        return remoteFunctionsSetup!=null && lastModificationFunctionSetup != remoteFunctionsSetup.lastModified();
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Přečtení prvního řádku (cestu v souborovém systému) z daného lokálního konfiguračního souboru a vratí příslušnou cestu ke vzdálené složce.
     * @param nameFileConfig název lokálního konfiguračního souboru
     * @throws java.io.IOException Pokud nastala chyba při čtení. Nebo když je konfigurační soubor prázdný.
     * @throws java.io.FileNotFoundException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     *                   Nebo v případě, že cesta v konfiguračním souboru je neplatná.
     * @return cestu ke vzdálené složce
     */
    private File readLocalConfigurationFile(String nameFileConfig) throws FileNotFoundException, IOException{
        //Složka ve sdíleném uložišti.
        File remoteDirectory;
        
        File file = new File(nameFileConfig);
        try (FileReader reader = new FileReader(file); BufferedReader br = new BufferedReader(reader)) {
            String path2 = br.readLine();
            if(path2 == null){
                throw new IOException("Konfigurační soubor "+nameFileConfig+" je prázdný. Je potřeba spustit administrační nástroj a nastavit správně obsah konfiguračního souboru.");
            }
            remoteDirectory = new File(path2);
        }
        
        if(!remoteDirectory.exists()){
            throw new FileNotFoundException("Cesta v souboru "+nameFileConfig+" není platná. Je potřeba spustit administrační nástroj a nastavit správně obsah konfiguračního souboru.");
        }
        
        return remoteDirectory;
    }
    
    /**
     * Přečte konfigurační soubory.
     */
    private void readLocalConfigurationFiles(){
        try {
            remoteSavingDirectory = readLocalConfigurationFile(fileRemoteSaveConfigurationName);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        try {
            remoteFunctionsSetup = readLocalConfigurationFile(fileRemoteFunctionSetupConfigurationName);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
        
        remoteFunctionsSetup = new File(remoteFunctionsSetup, fileFunctionConfigurationName);
        
        if(!remoteFunctionsSetup.exists()){
            remoteFunctionsSetup = null;
            LOGGER.log(Level.SEVERE, "Na vzdáleném uložišti nebyl nalezen soubor "+fileFunctionConfigurationName+", který by měl obsahovat nastavení funkcí.", this);
        }
    }
    
    /**
     * Přečte ze souboru jeden znak (1 nebo 0).
     * @param br
     * @return true nebo false
     * @throws IOException Pokud nastala chyba při čtení ze souboru nebo pokud soubor obsahuje nevalidní data.
     */
    private boolean readOneFunction(BufferedReader br) throws IOException{
        char character = (char) br.read();
        if(character>-1){
            if(character=='0'){
                return false;
            }else if(character=='1'){
                return true;
            }
        }
        throw new IOException("Soubor "+remoteFunctionsSetup+" není validní.");
    }
    
    
}

