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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.StyledDocument;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Deferred;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DTextPane.TextPaneNative;
import org.modelingvalue.jdclare.swing.PopupMenu.PopupMenuNative;
import org.modelingvalue.transactions.Compound;

@Native(TextPaneNative.class)
public interface DTextPane<E extends TextElement> extends DTextComponent {

    @Property(containment)
    List<E> elements();

    default E element(int position) {
        return elements().filter(e -> e.offset() <= position && position - e.offset() <= e.len()).findFirst().orElse(null);
    }

    class TextPaneNative<E extends TextElement> extends TextComponentNative<DTextPane<E>, JTextPane> {

        private final KeyAdapter keyAdapter;

        public TextPaneNative(DTextPane<E> visible) {
            super(visible);
            keyAdapter = new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_SPACE) {
                        Caret caret = swing.getCaret();
                        Point point = caret.getMagicCaretPosition();
                        popup(point.x, point.y, caret.getDot(), false);
                        e.consume();
                    }
                }
            };
        }

        @Override
        public void init(DObject parent, Compound tx) {
            swing = new JTextPane();
            swing.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            swing.addKeyListener(keyAdapter);
            super.init(parent, tx);
        }

        @Override
        public void exit(DObject parent, Compound tx) {
            super.exit(parent, tx);
            swing.removeKeyListener(keyAdapter);
        }

        @Override
        protected void popup(int x, int y) {
            int position = swing.viewToModel2D(new Point(x, y));
            if (position >= 0) {
                popup(x, y, position, true);
            }
        }

        private void popup(int x, int y, int position, boolean select) {
            E el = visible.element(position);
            if (el != null) {
                PopupMenu popupMenu = el.popupMenu();
                if (popupMenu != null) {
                    if (select) {
                        swing.select(el.offset(), el.offset() + el.len());
                        swing.requestFocus();
                    }
                    ((PopupMenuNative) dNative(popupMenu)).swing.show(swing, x, y);
                }
            }
        }

        @Override
        @Deferred
        public void string(String pre, String post) {
            super.string(pre, post);
            elements(null, visible.elements());
        }

        @Deferred
        public void elements(List<E> pre, List<E> post) {
            Highlighter highlighter = swing.getHighlighter();
            highlighter.removeAllHighlights();
            StyledDocument styledDocument = swing.getStyledDocument();
            post.forEach(el -> {
                TextStyle style = el.style();
                styledDocument.setCharacterAttributes(el.offset(), el.len(), style.attributeSet(), true);
                HighlightPainter hl = style.highligth();
                if (hl != null) {
                    try {
                        highlighter.addHighlight(el.offset(), el.offset() + el.len(), hl);
                    } catch (BadLocationException e) {
                        throw new Error(e);
                    }
                }
            });
        }

    }

}
