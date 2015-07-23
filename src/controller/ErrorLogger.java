//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * Třída představuje globální logger chyb pro celou palikaci.
 * Zajišťuje logování do dvou soborů.
 * Do HTML souboru stručně pouze zprávy čitelné pro uživatele (administrátora).
 * Do txt souboru pro autora aplikace, kde je vypsaný kompletní trace route.
 * @author Jan Brzobohatý
 */
public class ErrorLogger {
    /**
     * handler pro txt soubor
     */
    static private FileHandler txtHandler;
    
    /**
     * formátor pro txt soubor
     */
    static private SimpleFormatter formatterTxt;

    /**
     * handler pro práci s html souborem
     */
    static private FileHandler htmlHandler;
    
    /**
     * formátor pro HTML soubor
     */
    static private Formatter formatterHTML;

    /**
     * Nastavení globálního loggeru.
     * @throws IOException pokud nemůže vytvořit logovací soubor
     */
    static public void setup() throws IOException{
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

        logger.setLevel(Level.INFO);
        txtHandler = new FileHandler("ErrorLog.txt", true);
        htmlHandler = new FileHandler("ErrorLog.html", true);

        //vytvoření textového formátoru
        formatterTxt = new SimpleFormatter();
        txtHandler.setFormatter(formatterTxt);
        logger.addHandler(txtHandler);

        //vytvoření HTML formátoru
        formatterHTML = new MyHtmlFormatter();
        htmlHandler.setFormatter(formatterHTML);
        logger.addHandler(htmlHandler);
    }
    
    /**
     * Uzavření souborů pro logování.
     */
    public static void close(){
        htmlHandler.close();
        txtHandler.close();
    }
  
    // this custom formatter formats parts of a log record to a single line
    static class MyHtmlFormatter extends Formatter {
        /**
         * Formát vypisovaného data
         */
        private final SimpleDateFormat date_format = new SimpleDateFormat("yyyy MMM dd. HH:mm");
        
        
        // this method is called for every log records
        @Override
        public String format(LogRecord rec) {
            StringBuilder buf = new StringBuilder(1000);
            buf.append("<!DOCTYPE html>");
            buf.append("<body>");
            buf.append("<h3>");
            buf.append(date_format.format(new Date()));
            buf.append("</h3>");
            buf.append("<div style=\"color:red\">");
            buf.append(formatMessage(rec));
            buf.append("</div>");
            buf.append("</body></html>");
            return buf.toString();
        }
    }
}


