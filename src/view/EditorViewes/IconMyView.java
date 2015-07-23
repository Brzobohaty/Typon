//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view.EditorViewes;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.Position;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import net.coobird.thumbnailator.Thumbnails;

/**
 * Třída představuje view pro obrázek.
 */
public class IconMyView extends ParagraphView{
    /**
     * Maximální šířka a výška obrázku.
     */
    public static final int MAX_WIDTH = 660;
    public static final int MAX_HIGHT = 900;

    /**
     * Globální logger chyb
     */
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    
    /**
     * Hranice obrázku i s rámečkem.
     */
    private Rectangle bounds = new Rectangle();

    /**
     * Velikost čtverečků pro uchycení obrázku na okraji.
     */
    private final int dist = 15;

    /**
     * Indikátor, zda má tento obrázek focus.
     */
    private boolean focus = false;

    /**
     * List všech view obrázků v editoru.
     */
    private static final ArrayList<IconMyView> listOfInstants = new ArrayList();

    /**
     * Originální obrázek v původních rozměrech.
     */
    private BufferedImage original;

    /**
     * Indikátor, zda je obrázek vybrán.
     */
    private boolean selected;

    /**
     * Varianty umístění čtverečku pro uchopení na okraji obrázku.
     */
    public final int locations[] = {
        SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST,
        SwingConstants.EAST, SwingConstants.NORTH_WEST,
        SwingConstants.NORTH_EAST, SwingConstants.SOUTH_WEST,
        SwingConstants.SOUTH_EAST
    };

    /**
     * Zarovnání obrázku (odstavce).
     */
    private int justification;

    /**
     * aktuální ImageView
     */
    private static IconMyView currentImage = null;

    /**
     * Obrázek v editoru
     */
    private BufferedImage image;

    /**
     * Šířka obrázku.
     */
    private final int width;

    /**
     * Výška obrázku.
     */
    private final int height;

    /**
     * @return Šířku obrázku
     */
    public int getRatioWidth() {
        return width;
    }

    /**
     * @return Výšku obrázku
     */
    public int getRatioHeight() {
        return height;
    }

    /**
     * @return hranice obrázku i s ohraničením 
     */
    public Rectangle getBounds() {
        return bounds;
    }

    /**
     * @return aktuální ImageView 
     */
    public static IconMyView getCurrentImageView(){
        return currentImage;
    }

    /**
     * @return zda má obrázek focus 
     */
    public boolean hasFocus() {
        return focus;
    }

    /**
     * Přidělení nebo odebrání focusu.
     * @param focus 
     */
    public void setFocus(boolean focus) {
        this.focus = focus;
    }

    /**
     * @return originální obrázek 
     */
    public BufferedImage getOriginal(){
        return original;
    }

    /**
     * @param original originální obrázek
     */
    public void setOriginal(BufferedImage original){
        this.original = original;
    }

    /**
     * @return obrázek
     */
    public Image getImage(){
        return image;
    }

    /**
     * @param selected true, pokud je obrázek vybrán
     */
    public void setSelected(boolean selected){
        this.selected = selected;
    }

    /**
     * Nastaví novou velikost obrázku.
     * @param w nová šířka
     * @param h nová výška
     * @param keepAspectRatio zachovat poměr stran 
     */
    public void setSize(int w, int h, boolean keepAspectRatio){
        try {
            image = Thumbnails.of(original).size(w,h).keepAspectRatio(keepAspectRatio).asBufferedImage();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    /**
     * Odstraní focus ze všech obrázků v dokumentu.
     */
    public static void clearAllFocus(){
        Iterator iterator = listOfInstants.iterator();
        IconMyView view;
        while(iterator.hasNext()){
            view = (IconMyView)iterator.next();
            if(view!=null){
                view.setFocus(false);
            }
        }
    }

    /**
     * Odstraní zobrazení selekce ze všech obrázků v dokumentu.
     */
    public static void clearAllSelection(){
        Iterator iterator = listOfInstants.iterator();
        IconMyView view;
        while(iterator.hasNext()){
            view = (IconMyView)iterator.next();
            if(view!=null){
                view.setSelected(false);
            }
        }
    }

    /**
    * Creates a new icon view that represents an element.
    *
    * @param elem the element to create a view for
    */
   public IconMyView(Element elem) {
       super(elem);
       AttributeSet attr = elem.getAttributes();
       ImageIcon imager = (ImageIcon) StyleConstants.getIcon(attr);
       image = toBufferedImage(imager.getImage());
       original = image;
       height = image.getHeight();
       width = image.getWidth();

       //V případě, že má obrázek větší rozměry než stránka, tak zmenšit.
       if(width>MAX_WIDTH){
           setSize(MAX_WIDTH, height, true);
       }
       if(height>MAX_HIGHT){
           setSize(width, MAX_HIGHT, true);
       }
       listOfInstants.add(this);
   }

   // --- View methods ---------------------------------------------

   /**
    * Paints the icon.
    * The real paint behavior occurs naturally from the association
    * that the icon has with its parent container (the same
    * container hosting this view), so this simply allows us to
    * position the icon properly relative to the view.  Since
    * the coordinate system for the view is simply the parent
    * containers, positioning the child icon is easy.
    *
    * @param g the rendering surface to use
    * @param a the allocated region to render into
    * @see View#paint
    */
    @Override
    public void paint(Graphics g, Shape a) {
        int x = a.getBounds().x;//+(dist/2);
        int y = a.getBounds().y+(dist/2);

        if(justification != StyleConstants.ALIGN_LEFT){
            x += (MAX_WIDTH/2)-(image.getWidth()/2);
        }

        bounds = new Rectangle(x-(dist/2),y-(dist/2),image.getWidth()+(dist),image.getHeight()+(dist));
        g.drawImage(image, x, y, image.getWidth(), image.getHeight(), null);
        
        if(focus){
            paintBorder(g);
        }else if(selected){
            paintSelection(g);
        }
    }

    /**
     * Zarovnání odstavce (obrázku)
     * @param j zarovnání StyleConstants
     */
    @Override
    public void setJustification(int j){
        super.setJustification(j);
        justification = j;
    }

   /**
    * Determines the preferred span for this view along an
    * axis.
    *
    * @param axis may be either View.X_AXIS or View.Y_AXIS
    * @return  the span the view would like to be rendered into
    *           Typically the view is told to render into the span
    *           that is returned, although there is no guarantee.
    *           The parent may choose to resize or break the view.
    * @exception IllegalArgumentException for an invalid axis
    */
    @Override
    public float getPreferredSpan(int axis) {
        switch (axis) {
        case View.X_AXIS:
            return MAX_WIDTH;
        case View.Y_AXIS:
            return image.getHeight()+dist;
        default:
            throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }

   /**
    * Determines the desired alignment for this view along an
    * axis.  This is implemented to give the alignment to the
    * bottom of the icon along the y axis, and the default
    * along the x axis.
    *
    * @param axis may be either View.X_AXIS or View.Y_AXIS
    * @return the desired alignment &gt;= 0.0f &amp;&amp; &lt;= 1.0f.  This should be
    *   a value between 0.0 and 1.0 where 0 indicates alignment at the
    *   origin and 1.0 indicates alignment to the full span
    *   away from the origin.  An alignment of 0.5 would be the
    *   center of the view.
    */
    @Override
    public float getAlignment(int axis) {
        switch (axis) {
        case View.Y_AXIS:
            return 1;
        default:
            return super.getAlignment(axis);
        }
    }

   /**
    * Provides a mapping from the document model coordinate space
    * to the coordinate space of the view mapped to it.
    *
    * @param pos the position to convert &gt;= 0
    * @param a the allocated region to render into
    * @return the bounding box of the given position
    * @exception BadLocationException  if the given position does not
    *   represent a valid location in the associated document
    * @see View#modelToView
    */
    @Override
    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        currentImage = this;
        int p0 = getStartOffset();
        int p1 = getEndOffset();
        if ((pos >= p0) && (pos <= p1)) {
            if(bounds==null){
                Rectangle r = a.getBounds();
                if (pos == p1) {
                    r.x += r.width;
                }
                r.width = image.getWidth();
                return r;
            }
            return bounds;
        }
        throw new BadLocationException(pos + " not in range " + p0 + "," + p1, pos);
    }


   /**
    * Provides a mapping from the view coordinate space to the logical
    * coordinate space of the model.
    *
    * @param x the X coordinate &gt;= 0
    * @param y the Y coordinate &gt;= 0
    * @param a the allocated region to render into
    * @return the location within the model that best represents the
    *  given point of view &gt;= 0
    * @see View#viewToModel
    */
    @Override
    public int viewToModel(float x, float y, Shape a, Position.Bias[] bias) {
       return viewToModel();
    }

    /**
     * Vrátí pozici tohoto view v dokumnetu.
     * @return pozice v dokumentu
     */
    public int viewToModel() {
       return getStartOffset();
    }
    
    /**
    * Vrátí jeden čtvereček představujíví uchopení obrázku.
    * @param location pozice na daném obrázku podle světových stran z SwingConstants
    * @return čtvereček pro vykreslení
    */
    public Rectangle getRectangle(int location) {
        switch (location) {
            case SwingConstants.NORTH:
                return new Rectangle(bounds.x + bounds.width / 2 - dist / 2, bounds.y, dist, dist);
            case SwingConstants.SOUTH:
                return new Rectangle(bounds.x + bounds.width / 2 - dist / 2, bounds.y + bounds.height - dist, dist,
                        dist);
            case SwingConstants.WEST:
                return new Rectangle(bounds.x, bounds.y + bounds.height / 2 - dist / 2, dist, dist);
            case SwingConstants.EAST:
                return new Rectangle(bounds.x + bounds.width - dist, bounds.y + bounds.height / 2 - dist / 2, dist,
                        dist);
            case SwingConstants.NORTH_WEST:
                return new Rectangle(bounds.x, bounds.y, dist, dist);
            case SwingConstants.NORTH_EAST:
                return new Rectangle(bounds.x + bounds.width - dist, bounds.y, dist, dist);
            case SwingConstants.SOUTH_WEST:
                return new Rectangle(bounds.x, bounds.y + bounds.height - dist, dist, dist);
            case SwingConstants.SOUTH_EAST:
                return new Rectangle(bounds.x + bounds.width - dist, bounds.y + bounds.height - dist, dist, dist);
        }
        return null;
    }
    
    /**
    * Converts a given Image into a BufferedImage
    *
    * @param img The Image to be converted
    * @return The converted BufferedImage
    */
   private static BufferedImage toBufferedImage(Image img)
   {
       if (img instanceof BufferedImage)
       {
           return (BufferedImage) img;
       }

       // Create a buffered image with transparency
       BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

       // Draw the image on to the buffered image
       Graphics2D bGr = bimage.createGraphics();
       bGr.drawImage(img, 0, 0, null);
       bGr.dispose();

       // Return the buffered image
       return bimage;
   }
   
   /**
     * Vykreslí nad obrázek průhledné modré pole, které znázorňuje, že je obrázek označen.
     * @param g grafika
     */
    private void paintSelection(Graphics g) {
        g.setColor(new Color(57f/255, 105f/255, 138f/255, 0.50f));
        g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
    }

    /**
     * Vykreslí čtverečky pro uchopení na okraji obrázku.
     * @param g grafika
     */
    private void paintBorder(Graphics g) {
        //obrys obrázku
        g.setColor(Color.black);
        g.drawRect(bounds.x + dist / 2, bounds.y + dist / 2, bounds.width - dist, bounds.height - dist);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Retains the previous state
        Paint oldPaint = g2.getPaint();
        
        //úchyty obrázku
        for (int i = 0; i < locations.length; i++) {
            Rectangle rect = getRectangle(locations[i]);
            
            // Fills the circle with solid blue color
            //g2.setColor(new Color(0x0153CC));
            g2.setColor(new Color(255,200,0));
            g2.fillOval(rect.x, rect.y, rect.width - 1, rect.height - 1);

            // Adds shadows at the top
            Paint p;
            p = new GradientPaint(rect.x, rect.y, new Color(0.0f, 0.0f, 0.0f, 0.4f), rect.x, rect.y+rect.height, new Color(0.0f, 0.0f, 0.0f, 0.0f));
            g2.setPaint(p);
            g2.fillOval(rect.x, rect.y, rect.width - 1, rect.height - 1);

            // Adds highlights at the bottom 
            p = new GradientPaint(rect.x, rect.y, new Color(1.0f, 1.0f, 1.0f, 0.0f), rect.x, rect.y+rect.height, new Color(1.0f, 1.0f, 1.0f, 0.4f));
            g2.setPaint(p);
            g2.fillOval(rect.x, rect.y, rect.width - 1, rect.height - 1);

            // Creates dark edges for 3D effect
            p = new RadialGradientPaint(new Point2D.Double(rect.x+rect.width / 2.0, rect.y+rect.height / 2.0), 
                    rect.width / 2.0f,
                    new float[] { 0.0f, 1.0f },
                    new Color[] { new Color(255, 128, 0, 127), new Color(0.0f, 0.0f, 0.0f, 0.8f) });
            g2.setPaint(p);
            g2.fillOval(rect.x, rect.y, rect.width - 1, rect.height - 1);

            // Adds oval specular highlight at the top left
            p = new RadialGradientPaint(new Point2D.Double(rect.x+rect.width / 2.0,
                    rect.y+rect.height / 2.0), rect.width / 1.4f,
                    new Point2D.Double(rect.x+45.0, rect.y+25.0),
                    new float[] { 0.0f, 0.5f },
                    new Color[] { new Color(1.0f, 1.0f, 1.0f, 0.4f),
                        new Color(1.0f, 1.0f, 1.0f, 0.0f) },
                    RadialGradientPaint.CycleMethod.NO_CYCLE);
            g2.setPaint(p);
            g2.fillOval(rect.x, rect.y, rect.width - 1, rect.height - 1);
        }
        
        // Restores the previous state
        g2.setPaint(oldPaint);
    }
}
