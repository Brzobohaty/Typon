//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view.EditorViewes;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;
import static view.EditorViewes.PageableViewFactory.PAGE_HEIGHT;
import static view.EditorViewes.PageableViewFactory.PAGE_INSET;
import static view.EditorViewes.PageableViewFactory.PAGE_MARGINS;
import static view.EditorViewes.PageableViewFactory.PAGE_WIDTH;

/**
 * Root view which perform pagination and paints frame around pages.
 * @author Stanislav Lapitsky (http://java-sl.com/Pagination_In_JEditorPane.html)
 * @modifier Jan Brzobohatý
 */
class SectionView extends BoxView {
    private int pageNumber = 0;

    /**
     * Creates view instace
     * @param elem Element
     * @param axis int
     */
    SectionView(Element elem, int axis) {
        super(elem, axis);
    }

    /**
     * Gets amount of pages
     * @return int
     */
    private int getPageCount() {
        return pageNumber;
    }

    /**
     * Perform layout on the box
     *
     * @param width the width (inside of the insets) >= 0
     * @param height the height (inside of the insets) >= 0
     */
    @Override
    protected void layout(int width, int height) {
        width = PAGE_WIDTH - 2 * PAGE_INSET - PAGE_MARGINS.left - PAGE_MARGINS.right;
        this.setInsets( (short) (PAGE_INSET + PAGE_MARGINS.top), (short) (PAGE_INSET + PAGE_MARGINS.left), (short) (PAGE_INSET + PAGE_MARGINS.bottom),
                       (short) (PAGE_INSET + PAGE_MARGINS.right));
        super.layout(width, height);
    }

    /**
     * Determines the maximum span for this view along an
     * axis.
     *
     * overriddedn
     */
    @Override
    public float getMaximumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * Determines the minimum span for this view along an
     * axis.
     *
     * overriddedn
     */
    @Override
    public float getMinimumSpan(int axis) {
        return getPreferredSpan(axis);
    }

    /**
     * Determines the preferred span for this view along an
     * axis.
     * overriddedn
     */
    @Override
    public float getPreferredSpan(int axis) {
        float span;
        if (axis == View.X_AXIS) {
            span = PAGE_WIDTH;
        }
        else {
            span = PAGE_HEIGHT * getPageCount();
        }
        return span;
    }

    /**
     * Performs layout along Y_AXIS with shifts for pages.
     *
     * @param targetSpan int
     * @param axis int
     * @param offsets int[]
     * @param spans int[]
     */
    @Override
    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        super.layoutMajorAxis(targetSpan, axis, offsets, spans);
        int totalOffset = 0;
        int n = offsets.length;
        pageNumber = 0;
        for (int i = 0; i < n; i++) {
            offsets[i] = totalOffset;
            View v = getView(i);
            if (v instanceof MultiPageView) {
                ( (MultiPageView) v).setBreakSpan(0);
                ( (MultiPageView) v).setAdditionalSpace(0);
            }

            if ( (offsets[i] + spans[i]) > (pageNumber * PAGE_HEIGHT - PAGE_INSET * 2 - PAGE_MARGINS.top - PAGE_MARGINS.bottom)) {
                if ( (v instanceof MultiPageView) && (v.getViewCount() > 1)) {
                    MultiPageView multipageView = (MultiPageView) v;
                    int space = offsets[i] - (pageNumber - 1) * PAGE_HEIGHT;
                    int breakSpan = (pageNumber * PAGE_HEIGHT - PAGE_INSET * 2 - PAGE_MARGINS.top - PAGE_MARGINS.bottom) - offsets[i];
                    multipageView.setBreakSpan(breakSpan);
                    multipageView.setPageOffset(space);
                    multipageView.setStartPageNumber(pageNumber);
                    multipageView.setEndPageNumber(pageNumber);
                    int height = (int) getHeight();

                    int width = ( (BoxView) v).getWidth();
                    if (v instanceof PageableParagraphView) {
                        PageableParagraphView parView = (PageableParagraphView) v;
                        parView.layout(width, height);
                    }

                    pageNumber = multipageView.getEndPageNumber();
                    spans[i] += multipageView.getAdditionalSpace();
                }
                else {
                    offsets[i] = pageNumber * PAGE_HEIGHT;
                    pageNumber++;
                }
            }
            totalOffset = (int) Math.min( (long) offsets[i] + (long) spans[i], Integer.MAX_VALUE);
        }
    }

    /**
     * Paints view content and page frames.
     * @param g Graphics
     * @param a Shape
     */
    @Override
    public void paint(Graphics g, Shape a) {
        Rectangle alloc = (a instanceof Rectangle) ? (Rectangle) a : a.getBounds();
        Shape baseClip = g.getClip().getBounds();
        int pageCount = getPageCount();
        Rectangle page = new Rectangle();
        page.x = alloc.x;
        page.y = alloc.y;
        page.height = PAGE_HEIGHT;
        page.width = PAGE_WIDTH;
        for (int i = 0; i < pageCount; i++) {
            page.y = alloc.y + PAGE_HEIGHT * i;
            paintPageFrame(g, page, (Rectangle) baseClip);
        }
        super.paint(g, a);
        g.setColor(Color.gray);
        // Fills background of pages
        int currentWidth = (int) alloc.getWidth();
        int currentHeight = (int) alloc.getHeight();
        int x = page.x + PAGE_INSET;
        int y = 0;
        int w;
        int h;
        if (PAGE_WIDTH < currentWidth) {
            w = currentWidth;
            h = currentHeight;
            g.fillRect(page.x + page.width, alloc.y, w, h);
        }
        if (PAGE_HEIGHT * pageCount < currentHeight) {
            w = currentWidth;
            h = currentHeight;
            g.fillRect(page.x, alloc.y + page.height * pageCount, w, h);
        }
    }

    /**
     * Paints frame for specified page
     * @param g Graphics
     * @param page Shape page rectangle
     * @param container Rectangle
     */
    private void paintPageFrame(Graphics g, Shape page, Rectangle container) {
        int x,y,w,h,plus;
        Rectangle alloc = (page instanceof Rectangle) ? (Rectangle) page : page.getBounds();

        if (container.intersection(alloc).height <= 0)
            return;
        Color oldColor = g.getColor();

        //pozadí
        g.setColor(new Color(214,217,223));
        g.fillRect(alloc.x-7,alloc.y-7,PAGE_INSET,alloc.height+PAGE_INSET);
        g.fillRect(alloc.x-7,alloc.y-7,alloc.width+PAGE_INSET,PAGE_INSET);
        g.fillRect(alloc.width,alloc.y-7,PAGE_INSET,alloc.height+PAGE_INSET);
        g.fillRect(alloc.x-7,alloc.y+alloc.height-7,alloc.width+PAGE_INSET,PAGE_INSET);

        //frame
        g.setColor(Color.black);
        g.drawRect(alloc.x + PAGE_INSET, alloc.y + PAGE_INSET, alloc.width - (2*PAGE_INSET), alloc.height - (2*PAGE_INSET));

        //shadow
        g.setColor(Color.BLACK);
        x=alloc.x + PAGE_INSET;
        y=alloc.y + PAGE_INSET;
        w=alloc.width - (2*(PAGE_INSET));
        h=alloc.height - (2*(PAGE_INSET));
        plus=0;
        while(plus<31){
            x-=1;
            y-=1;
            w+=2;
            h+=2;
            plus += 2;
            g.setColor(new Color(183+plus,186+plus,190+plus));
            g.drawRect(x,y,w,h);
        }

        g.setColor(oldColor);
    }
}
