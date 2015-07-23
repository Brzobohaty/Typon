//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class MyFileChooser extends JFileChooser{
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    // Invalid names on Windows
    private final String[] INVALID_NAMES = {
            "com1",
            "com2",
            "com3",
            "com4",
            "com5",
            "com6",
            "com7",
            "com8",
            "com9",
            "lpt1",
            "lpt2",
            "lpt3",
            "lpt4",
            "lpt5",
            "lpt6",
            "lpt7",
            "lpt8",
            "lpt9",
            "con",
            "nul",
            "prn",
            ".",
            ".."
    };
    
    /**
     * koncovka souboru
     */
    private String extension;
 
    /**
     * seznam povolených koncovek
     */
    private final String[] allowed_extensions;
    
    /**
     * Text, který se uživateli bude říkat, jaké koncovky smí vybírat. 
     */
    private String popisFiltru;
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    /**
     * @param allowed_extensions seznam povolených koncovek 
     */
    MyFileChooser(String[] allowed_extensions){
        this.allowed_extensions = allowed_extensions;
        setPopisFiltru();
        
        setFileFilter(new MyFileFilter());
        setAcceptAllFileFilterUsed(false);
        
    }
    
    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * We do a series of checks to make sure we have a valid filename:
     * - The parent directory exists (it might not if a user includes a backslash in the name)
     * - The name can only use valid characters
     * - The name can't be too long or short
     * - The name can't end with a space or period (a Windows restriction)
     * - The name can't start with a period (this would make it hidden)
     * - The name can't be a system reserved name
     * 
     * Then, if specified, we add the required extension to the name if it
     * is not already part of the filename. If a required extension was not
     * specified, we check the extension against a list of valid
     * extensions and complain if the filename uses an invalid extension or
     * no extension at all.
     * 
     * Finally, we check whether a file with that name already exists, and
     * ask for confirmation to overwrite it if it does.
     */
    @Override
    public void approveSelection() {
            File file = getSelectedFile();
            String filename = file.getName();

            if (!(new File(file.getParent()).isDirectory()) && getDialogType() == SAVE_DIALOG) {
                    complain("Zpětná lomítka nejsou povolena v názvu souboru.");
                    return;
            }
            if (!filename.matches("[\\wěščřžýáíéóúůťď \\-_\\.]+") && getDialogType() == SAVE_DIALOG) {
                    complain("Pro název souboru můžete používat pouze písmena, čísla, mezery, pomlčky, podtržítka a tečky.");
                    return;
            }
            if (filename.length() > 255 && getDialogType() == SAVE_DIALOG) {
                    complain("Moc dlouhý název souboru.");
                    return;
            }
            if (filename.length() < 1 && getDialogType() == SAVE_DIALOG) {
                    complain("Nebyl zadán název souboru.");
                    return;
            }
            if (filename.matches(".*(\\.|\\s)+") && getDialogType() == SAVE_DIALOG) {
                    complain("Název souboru nesmí končit tečkou nebo mezerou.");
                    return;
            }
            if (filename.matches("\\.+.*+") && getDialogType() == SAVE_DIALOG) {
                    complain("Název souboru nesmí začínat tečkou.");
                    return;
            }
            if (inArray(filename, INVALID_NAMES) && getDialogType() == SAVE_DIALOG) {
                    complain("Tento název souboru je rezervován operačním systémem.");
                    return;
            }
            if ((file.exists() || new File(file.getPath()+".typon").exists())&& getDialogType() == SAVE_DIALOG) {
                if(!MyView.getView().raiseOptionDialog("Soubor "+file.getName() + " již existuje! Chcete ho přepsat?", "Soubor již existuje.")){
                    return;
                }
            }
            
            if(getDialogType() == OPEN_DIALOG){
                if (extension != null) {
                    String ext = getExtension(file);
                    if (ext == null || !ext.equals(extension)) {
                        file = new File(file.getPath() +"."+ extension);
                        setSelectedFile(file);
                    }
                }else {
                    String ext = getExtension(file);
                    if (ext == null) {
                        complain("Souboru chybí koncovka.");
                        return;
                    }
                    if (!inArray(ext, allowed_extensions)) {
                        complain("Typ souboru "+ ext +" není povolen.");
                        return;
                    }
                }
            }

        super.approveSelection();
    }
    
    @Override
    public void setDialogType(int dialogType) {
        super.setDialogType(dialogType);
        if(dialogType == OPEN_DIALOG){
            setApproveButtonText("Otevřít");
        }else{
            setApproveButtonText("Uložit");
        }
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/
    
    /**
     * Otevře error dialog s daným textem.
     * @param complaint text v dialogu
     */
    private void complain(String complaint) {
        MyView.getView().raiseErrorDialog(complaint);
    }

    /**
     * Zjistí, zda se daná hodnota nachází v poli.
     * @param val hodnota, kterou hledáme
     * @param array pole v kterém hledáme
     * @return true, pokud se v poli nachází
     */
    private static boolean inArray(String val, String[] array) {
        for (String v : array) {
                if (val.equalsIgnoreCase(v))
                        return true;
        }
        return false;
    }

    /**
     * Složí popis filtru, který se bude zobrazovat uživateli.
     */
    private void setPopisFiltru() {
        popisFiltru = "";
        for(String ext : allowed_extensions){
            popisFiltru = popisFiltru+"."+ext+" ";
        }
    }
    
    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Třída představuje filter, který zajistí, že budou zobrazovány pouze složky a soubory s koncovkou .typon
     */
    private class MyFileFilter extends FileFilter{

        @Override
        public boolean accept(File file){
            if (file.isDirectory()){
                  return true;
            }
            for (String extension : allowed_extensions){
              if (file.getName().toLowerCase().endsWith(extension)){
                return true;
              }
            }
            return false;
        }
        
        @Override
        public String getDescription() {
            return popisFiltru;
        }
    }
    
    /**
     * @param f soubor
     * @return koncovku souboru 
     */
    private String getExtension(File f) {
        String ext = null, s = f.getName();
        int i = s.lastIndexOf('.');
        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
