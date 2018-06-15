//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
// (C) Copyright 2018 Modeling Value Group B.V. (http://modelingvalue.org)                                             ~
//                                                                                                                     ~
// Licensed under the GNU Lesser General Public License v3.0 (the "License"). You may not use this file except in      ~
// compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0  ~
// Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on ~
// an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the  ~
// specific language governing permissions and limitations under the License.                                          ~
//                                                                                                                     ~
// Contributors:                                                                                                       ~
//     Wim Bast, Carel Bast, Tom Brus, Arjan Kok, Ronald Krijgsheld                                                    ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.jdclare.swing;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import org.modelingvalue.jdclare.DStruct6;
import org.modelingvalue.jdclare.Property;

public interface TextStyle extends DStruct6<Color, Color, Boolean, Boolean, Boolean, HighlightPainter> {

    StyleContext STYLE_CONTEXT = StyleContext.getDefaultStyleContext();

    @Property(key = 0)
    Color foreground();

    @Property(key = 1)
    Color background();

    @Property(key = 2)
    boolean bold();

    @Property(key = 3)
    boolean underline();

    @Property(key = 4)
    boolean italic();

    @Property(key = 5)
    HighlightPainter highligth();

    @Property(constant)
    default AttributeSet attributeSet() {
        AttributeSet as = STYLE_CONTEXT.getEmptySet();
        Color fg = foreground();
        if (fg != null) {
            as = STYLE_CONTEXT.addAttribute(as, StyleConstants.Foreground, fg);
        }
        Color bg = background();
        if (bg != null) {
            as = STYLE_CONTEXT.addAttribute(as, StyleConstants.Background, bg);
        }
        if (bold()) {
            as = STYLE_CONTEXT.addAttribute(as, StyleConstants.Bold, true);
        }
        if (underline()) {
            as = STYLE_CONTEXT.addAttribute(as, StyleConstants.Underline, true);
        }
        if (italic()) {
            as = STYLE_CONTEXT.addAttribute(as, StyleConstants.Italic, true);
        }
        return as;
    }

}
