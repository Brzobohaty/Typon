//==============================================================================
// Copyright Jan Brzobohatý 2015.
// Distributed under the MIT License.
// (See accompanying file LICENSE or copy at http://opensource.org/licenses/MIT)
//==============================================================================

package view.EditorViewes;

import javax.swing.text.Element;
import javax.swing.text.LabelView;

/**
 * Label se zalamováním textu.
 * @author Stanislav Lapitsky (http://java-sl.com/Pagination_In_JEditorPane.html)
 */
class WrapLabelView extends LabelView {

    WrapLabelView(Element elem) {
        super(elem);
    }

    @Override
    public float getMinimumSpan(int axis) {
        switch (axis) {
            case javax.swing.text.View.X_AXIS:
                return 0;
            case javax.swing.text.View.Y_AXIS:
                return super.getMinimumSpan(axis);
            default:
                throw new IllegalArgumentException("Invalid axis: " + axis);
        }
    }
}
