//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package model;

import java.awt.event.ActionEvent;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AbstractDocument.DefaultDocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;

/**
 * Třída představuje manager pro funkci Undo (Zpět). 
 * @author Jan Brzobohatý
 */
public class MyUndoManager extends AbstractUndoableEdit implements UndoableEditListener {
    /**
     * Instance tohoto UndoMangeru
     */
    private static MyUndoManager undoManager;
    
    /**
     * Akce Undo pro tlačítko Undo.
     */
    private final UndoAction undoAction;
    
    /**
     * Zásobník s vratitelnými editacemi.
     */
    private final Stack<MyCompoundEdit> edits = new Stack<>();

    /**
     * Současná skupina editací.
     */
    private MyCompoundEdit current;

    /**
     * Indikátor, zda se jedná o skupinu.
     */
    private boolean group = false;
    
    /**
     * Pozice naposledy vkládáného znaku.
     */
    private int lastInsertOffset = -2;
    
    /**
     * Pozice naposledy mazaného znaku.
     */
    private int lastRemoveOffset = -2;
    
    /**
     * Hodnota právě smazaného znaku.
     */
    private String currentRemovedValue;
    
    /**
     * Zapamatování si poslední skupiny. (Pro speciální případ mazání znaku na konci odstavce.)
     */
    private MyCompoundEdit rememberedCompound;
    
    /**
     * Počet vnitřních skupin současné skupiny.
     */
    private int innerGroups = 0;
    
    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    private MyUndoManager(){
        undoAction = new UndoAction();
    }
    
    public static MyUndoManager getUndoManager(){
        if(undoManager==null){
            undoManager = new MyUndoManager();
        }
        return undoManager;
    }
    
    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    /**
     * @return Akce Undo pro tlačítko Undo. 
     */
    public AbstractAction getUndoAction(){
        return undoAction;
    }
    
    /**
     * Pomocí této metody se definuje začátek a konec skupiny operací, které se mají v případě undo vrátit všechny najednou.
     * @param group true před skupinou editací a false na po skončení editací
     */
    public void isGroup(boolean group){
        //pokud je zrovna nastavena nějaká skupina
        if(this.group){
            //a teď má začít další skupina unvintř ní
            if(group){
                innerGroups++;
                return;
            }else if(innerGroups>0){
                innerGroups--;
                return;
            }
        }
        this.group = group;
        this.current = null;
    }

    /**
     * Vymaže veškeré zapamatované editace.
     */
    public void clearManager(){
        edits.clear();
        undoAction.updateUndoState();
        current=null;
    }
    
    /**
     * @param value hodnota právě mazaného znaku
     */
    public void setCurrentRemoveValue(String value){
        this.currentRemovedValue = value;
    }

    /**
     * Metoda se zavolá v případě, že byla vykonána editace, která je vratitelná.
     * @param e
     */
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (e.getEdit() instanceof AbstractDocument.DefaultDocumentEvent) {            
            DefaultDocumentEvent edit = (DefaultDocumentEvent) e.getEdit();
            int currentOffset = edit.getOffset();
            try {   
                //pokud se jedná o skupinu editací tak nastavit, aby se tvářili jako jedna
                if(group){
                    handleGroupCompound(currentOffset, edit);
                //pokud se jedná o vložení jednoho nebílého znaku, tak seskupit s předchozím vložením jednoho nebílého znaku (undování celých slov)
                }else if(edit.getLength() == 1 && edit.getType() == EventType.INSERT && !edit.getDocument().getText(edit.getOffset(), 1).trim().isEmpty()){
                    handleInsertCharCompound(currentOffset, edit);
                //pokud se jedná o vymazání nebílého znaku, tak seskupit s předchozím vymazáním nebílého znaku (undování mazání celých slov)
                }else if(edit.getLength() == 1 && edit.getType() == EventType.REMOVE && !currentRemovedValue.trim().isEmpty()){
                    handleRemoveCharCompound(currentOffset, edit);
                //neseskupovat
                }else{
                    handleSingleEdit(edit);
                }
                
                undoAction.updateUndoState();
            } catch (BadLocationException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }

    /**
     * Vrátí poslední změnu.
     * @throws CannotUndoException Pokud poslední změna nejde vrátit.
     */
    @Override
    public void undo() throws CannotUndoException {
        if (!canUndo()) {
            throw new CannotUndoException();
        }

        edits.pop().undo();
        undoAction.updateUndoState();
    }

    /**
     * @return true, pokud je co vracet ve funkci Undo 
     */
    @Override
    public boolean canUndo() {
        return !edits.empty();
    }
    
    /************************************************************************************************
    Deklarace soukromých metod.
    ************************************************************************************************/

    /**
     * Vytvoří novou skupinu změn.
     */
    private void createCompoundEdit() {
        current= new MyCompoundEdit();
        edits.push(current);
    }   
    
    /**
     * Vytvoří novou skupinu změn v případě potřeby a rovnou přidá editaci do skupiny.
     * @param edit aktuální editace do skupiny
     */
    private void createCompoundIfNeededAndAddEdit(DefaultDocumentEvent edit){
        if (current==null){
            createCompoundEdit();
        }
        current.addEdit(edit);
    }
    
    /**
     * Ošetří případ, kdy je vkládán jeden znak a zaručí, že se budou seskupovat znaky po sobě vkládané.
     * @param currentOffset pozice aktuální editace
     * @param edit aktuální editace
     */
    private void handleInsertCharCompound(int currentOffset, DefaultDocumentEvent edit){
        if(lastInsertOffset+1==currentOffset){
            createCompoundIfNeededAndAddEdit(edit);
        }else{
            createCompoundEdit();
            current.addEdit(edit);
        }
        lastInsertOffset = currentOffset;
    }
    
    /**
     * Ošetří případ, kdy je mazán jeden znak a zaručí, že se budou seskupovat znaky po sobě mazané.
     * @param currentOffset pozice aktuální editace
     * @param edit aktuální editace
     */
    private void handleRemoveCharCompound(int currentOffset, DefaultDocumentEvent edit){
        if(lastRemoveOffset-1==currentOffset || lastRemoveOffset==currentOffset){
            createCompoundIfNeededAndAddEdit(edit);
        }else{
            createCompoundEdit();
            current.addEdit(edit);
        }
        lastRemoveOffset = currentOffset;
    }
    
    /**
     * Ošetří případ, kdy je více editací seskupováno cíleně do skupiny.
     * @param currentOffset pozice aktuální editace
     * @param edit aktuální editace
     */
    private void handleGroupCompound(int currentOffset, DefaultDocumentEvent edit){
        
        try {
            //ošetření speciálního případu, kdy mažeme znak před enterem a je zároveň měněn styl
            if(edit.getLength() == 1 && edit.getType() == EventType.REMOVE && !currentRemovedValue.trim().isEmpty() && edit.getDocument().getText(edit.getOffset(), 1).equals("\n")){
                if(lastRemoveOffset-1==currentOffset || lastRemoveOffset==currentOffset){
                    //použít předchozí skupinu
                    current = rememberedCompound;
                }else{
                    if (current==null){
                        createCompoundEdit();
                    }
                    //zapamatovat si skupinu
                    rememberedCompound = current;
                }
                lastRemoveOffset = currentOffset;
            }else if(current!=rememberedCompound){
                lastRemoveOffset = -2;
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(MyUndoManager.class.getName()).log(Level.SEVERE, null, ex);
        }
            
        createCompoundIfNeededAndAddEdit(edit);
        lastInsertOffset = -2;
    }
    
    /**
     * Ošetří případ, kdy není potřeba žádné editace a jedná se o samostatnou editaci.
     * @param edit aktuální editace
     */
    private void handleSingleEdit(DefaultDocumentEvent edit){
        createCompoundEdit();
        current.addEdit(edit);
        current = null;
        lastInsertOffset = -2;
        lastRemoveOffset = -2;
    }
    
    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    /**
     * Třída předtavuje skupinu editací.
     */
    private static class MyCompoundEdit extends CompoundEdit {
        boolean isUnDone=false;
 
        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            isUnDone=true;
        }
        
        @Override
        public boolean canUndo() {
            return !edits.isEmpty() && !isUnDone;
        }
    }
    
    /**
     * Třída představuje akci Undo pro tlačítko Undo.
     */
    private static class UndoAction extends AbstractAction {
        public UndoAction() {
            super("Undo");
            setEnabled(false);
        }
 
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            } catch (CannotUndoException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            }
            updateUndoState();
        }

        protected void updateUndoState() {
            if (undoManager.canUndo()) {
                setEnabled(true);
            } else {
                setEnabled(false);
            }
        }
    }
}
