//=============================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//=============================================================
package model;

import java.awt.datatransfer.Clipboard;

/**
 * Třída představuje lokální schránku pouze pro tento editor.
 */
public class LocalClipboard extends Clipboard{
    private static Clipboard instance;
    
    private LocalClipboard(){
        super("MyClipboard");
    }
    
    public static Clipboard getClipboard(){
        if(instance==null){
            instance = new LocalClipboard();
        }
        return instance;
    }
}
