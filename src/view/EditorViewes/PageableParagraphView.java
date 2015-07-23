//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view.EditorViewes;

import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import static view.EditorViewes.PageableViewFactory.PAGE_HEIGHT;
import static view.EditorViewes.PageableViewFactory.PAGE_INSET;
import static view.EditorViewes.PageableViewFactory.PAGE_MARGINS;

/**
 * Represents multipage paragraph.
 * @author Stanislav Lapitsky (http://java-sl.com/Pagination_In_JEditorPane.html)
 * @version 1.0
 */
class PageableParagraphView extends ParagraphView implements MultiPageView {
    private int additionalSpace = 0;
    private int breakSpan = 0;
    private int pageOffset = 0;
    private int startPageNumber = 0;
    private int endPageNumber = 0;

    PageableParagraphView(Element elem) {
        super(elem);
    }

    @Override
    public void layout(int width, int height) {
        super.layout(width, height);
    }

    @Override
    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        super.layoutMajorAxis(targetSpan, axis, offsets, spans);
        performMultiPageLayout(targetSpan, axis, offsets, spans);
    }

    /**
     * Layout paragraph's content splitting between pages if needed.
     * Calculates shifts and breaks for parent view (SectionView)
     * @param targetSpan int
     * @param axis int
     * @param offsets int[]
     * @param spans int[]
     */
    @Override
    public void performMultiPageLayout(int targetSpan, int axis, int[] offsets, int[] spans) {
        if (breakSpan == 0)
            return;
        int space = breakSpan;

        additionalSpace = 0;
        endPageNumber = startPageNumber;
        int topInset = this.getTopInset();
        int offs = 0;
        for (int i = 0; i < offsets.length; i++) {
            if (offs + spans[i] + topInset > space) {
                int newOffset = endPageNumber * PAGE_HEIGHT;
                int addSpace = newOffset - (startPageNumber - 1) * PAGE_HEIGHT - pageOffset - offs - topInset;
                additionalSpace += addSpace;
                offs += addSpace;
                for (int j = i; j < offsets.length; j++) {
                    offsets[j] += addSpace;
                }
                endPageNumber++;
                space = (endPageNumber * PAGE_HEIGHT - 2 * PAGE_INSET - PAGE_MARGINS.top - PAGE_MARGINS.bottom) - (startPageNumber - 1) * PAGE_HEIGHT - pageOffset;
            }
            offs += spans[i];
        }
    }

    /**
     * Gets view's start page number
     * @return page number
     */
    @Override
    public int getStartPageNumber() {
        return startPageNumber;
    }

    /**
     * Gets view's end page number
     * @return page number
     */
    @Override
    public int getEndPageNumber() {
        return endPageNumber;
    }

    /**
     * Gets view's extra space (space between pages)
     * @return extra space
     */
    @Override
    public int getAdditionalSpace() {
        return additionalSpace;
    }

    /**
     * Gets view's break span
     * @return break span
     */
    @Override
    public int getBreakSpan() {
        return breakSpan;
    }

    /**
     * Gets view's offsets on the page
     * @return offset
     */
    @Override
    public int getPageOffset() {
        return pageOffset;
    }

    /**
     * Sets view's start page number
     *
     * @param startPageNumber page number
     */
    @Override
    public void setStartPageNumber(int startPageNumber) {
        this.startPageNumber = startPageNumber;
    }

    /**
     * Sets view's end page number
     *
     * @param endPageNumber page number
     */
    @Override
    public void setEndPageNumber(int endPageNumber) {
        this.endPageNumber = endPageNumber;
    }

    /**
     * Sets extra space (space between pages)
     *
     * @param additionalSpace additional space
     */
    @Override
    public void setAdditionalSpace(int additionalSpace) {
        this.additionalSpace = additionalSpace;
    }

    /**
     * Sets view's break span.
     *
     * @param breakSpan break span
     */
    @Override
    public void setBreakSpan(int breakSpan) {
        this.breakSpan = breakSpan;
    }

    /**
     * Sets view's offset on the page
     *
     * @param pageOffset offset
     */
    @Override
    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }
}
