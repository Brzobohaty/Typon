//=======================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//=======================================================================

import controller.Controller;
import controller.ErrorLogger;

public class Typon{
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
       
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try{ 
                    Controller controller = Controller.getController();
                    if(args.length>0){
                        //pokud je v cestě k souboru mezera, tak je cesta rozdělena do více argumentů
                        String path = "";
                        for (String arg : args) {
                            path += arg;
                            if (arg.endsWith(".typon")) {
                                break;
                            }
                            path+=" ";
                        }
                        controller.setVisible(path);
                    }else{
                        controller.setVisible(null);
                    }
                }finally{
                    ErrorLogger.close();
                }
            }
        });
   }
}

