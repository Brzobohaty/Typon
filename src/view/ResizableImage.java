//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.Border;


public class ResizableImage extends JComponent {
    /************************************************************************************************
    Deklarace proměnných a konstant.
    ************************************************************************************************/
    
    /**
     * Vykreslovaný obrázek.
     */
    private final BufferedImage image;
    
    /**
     * Šířka jednoho uchopovacího čtverečku na zvetšovacím okraji obrázku. 
     */
    private final int BORDER_WIDTH = 15;
    
    /************************************************************************************************
    Deklarace konstruktorů a továrních metod.
    ************************************************************************************************/
    
    /**
     * Vytvoří zvětšovatelný obrázek na dané pozici (náhled).
     * @param buffered obrázek pro vykreslení
     * @param bounds rozměry a umístění obrázku
     * @param cursor kurzor, který se má po dobu zobrazení náhledu zobrazovat
     */
    ResizableImage(BufferedImage buffered, Rectangle bounds, int cursor) {
        this.image = buffered;
        setBounds(bounds);
        setBorder(new ResizableBorder(BORDER_WIDTH));
        setCursor(Cursor.getPredefinedCursor(cursor));
    }
    
    /************************************************************************************************
    Deklarace veřejných metod.
    ************************************************************************************************/
    
    @Override
    public void paint(Graphics g) {
        Composite oldComposite = ((Graphics2D)g).getComposite();
        
        //nastavení průhlednosti
        float opacity = 0.5f;
        ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        
        g.drawImage(image, BORDER_WIDTH/2, BORDER_WIDTH/2, getWidth()-BORDER_WIDTH, getHeight()-BORDER_WIDTH, null);
        ((Graphics2D)g).setComposite(oldComposite);
        
        super.paint(g);
    }
    
    /************************************************************************************************
    Deklarace soukromých tříd.
    ************************************************************************************************/
    
    private static class ResizableBorder implements Border {

        /**
         * Šířka čtverečků pro uchopení.
         */
        private final int dist;

        /**
         * Varianty umístění čtverečku pro uchopení na okraji obrázku.
         */
        private final int locations[] = {
            SwingConstants.NORTH, SwingConstants.SOUTH, SwingConstants.WEST,
            SwingConstants.EAST, SwingConstants.NORTH_WEST,
            SwingConstants.NORTH_EAST, SwingConstants.SOUTH_WEST,
            SwingConstants.SOUTH_EAST
        };

        /**
         * @param dist Šířka čtverečků pro uchopení
         */
        public ResizableBorder(int dist) {
            this.dist = dist;
        }
        
        @Override
        public Insets getBorderInsets(Component component) {
            return new Insets(dist, dist, dist, dist);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component component, Graphics g, int x, int y, int w, int h) {

            g.setColor(Color.black);
            g.drawRect(x + dist / 2, y + dist / 2, w - dist, h - dist);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Retains the previous state
            Paint oldPaint = g2.getPaint();
            
            //Vykreslit čtverečky pro uchopení.
            for (int i = 0; i < locations.length; i++) {
                Rectangle rect = getRectangle(x, y, w, h, locations[i]);
            
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

        /**
         * Vrátí jeden čtvereček představujíví uchopení obrázku.
         * @param x vertikální souřadnice
         * @param y horizintální souřadnice
         * @param w šířka obrázku
         * @param h výška obrázku
         * @param location pozice na daném obrázku podle světových stran z SwingConstants
         * @return čtvereček pro vykreslení
         */
        private Rectangle getRectangle(int x, int y, int w, int h, int location) {

            switch (location) {
                case SwingConstants.NORTH:
                    return new Rectangle(x + w / 2 - dist / 2, y, dist, dist);
                case SwingConstants.SOUTH:
                    return new Rectangle(x + w / 2 - dist / 2, y + h - dist, dist,
                            dist);
                case SwingConstants.WEST:
                    return new Rectangle(x, y + h / 2 - dist / 2, dist, dist);
                case SwingConstants.EAST:
                    return new Rectangle(x + w - dist, y + h / 2 - dist / 2, dist,
                            dist);
                case SwingConstants.NORTH_WEST:
                    return new Rectangle(x, y, dist, dist);
                case SwingConstants.NORTH_EAST:
                    return new Rectangle(x + w - dist, y, dist, dist);
                case SwingConstants.SOUTH_WEST:
                    return new Rectangle(x, y + h - dist, dist, dist);
                case SwingConstants.SOUTH_EAST:
                    return new Rectangle(x + w - dist, y + h - dist, dist, dist);
            }
            return null;
        }
    }
}
