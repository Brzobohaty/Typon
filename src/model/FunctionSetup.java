//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

/**
 * Třída představuje přepravku pro nastavení jednotlivých funkcí v editoru.
 * @author Jan Brzobohatý
 */
public class FunctionSetup {
    public final boolean undo;
    public final boolean clipboard;
    public final boolean font;
    public final boolean fontSize;
    public final boolean bold;
    public final boolean align;
    public final boolean picture;
    
    FunctionSetup(boolean undo, boolean clipboard, boolean font, boolean fontSize, boolean bold, boolean align, boolean picture) {
        this.undo = undo;
        this.clipboard = clipboard;
        this.font = font;
        this.fontSize = fontSize;
        this.bold = bold;
        this.align = align;
        this.picture = picture;
    }
}
