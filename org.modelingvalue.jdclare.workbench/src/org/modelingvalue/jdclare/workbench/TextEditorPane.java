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

package org.modelingvalue.jdclare.workbench;

import static org.modelingvalue.jdclare.DClare.*;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DStruct4;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.DTextComponent;
import org.modelingvalue.jdclare.swing.DTextPane;
import org.modelingvalue.jdclare.swing.MenuItem;
import org.modelingvalue.jdclare.swing.PopupMenu;
import org.modelingvalue.jdclare.swing.TextElement;
import org.modelingvalue.jdclare.swing.TextStyle;
import org.modelingvalue.jdclare.syntax.Text;
import org.modelingvalue.jdclare.syntax.Token;
import org.modelingvalue.jdclare.syntax.meta.TokenType;
import org.modelingvalue.jdclare.syntax.test.MySyntax.IDENTIFIER;
import org.modelingvalue.jdclare.syntax.test.MySyntax.NUMBER;
import org.modelingvalue.jdclare.syntax.test.MySyntax.STRING;
import org.modelingvalue.jdclare.syntax.test.TextUniverse.MyText;
import org.modelingvalue.jdclare.workbench.TextEditorPane.TokenTextElement;

public interface TextEditorPane extends DTextPane<TokenTextElement>, DStruct1<WBUniverse> {

    HighlightPainter ERROR_HIGHLIGTH  = new ErrorHighlightPainter();

    TextStyle        IDENTIFIER_STYLE = dclare(TextStyle.class, Color.GREEN.darker().darker(), null, false, false, false, null);
    TextStyle        STRING_STYLE     = dclare(TextStyle.class, Color.BLUE, null, false, false, false, null);
    TextStyle        NUMBER_STYLE     = dclare(TextStyle.class, Color.BLUE.darker().darker(), null, true, false, false, null);
    TextStyle        ERROR_STYLE      = dclare(TextStyle.class, Color.BLACK, null, false, false, false, ERROR_HIGHLIGTH);
    TextStyle        LITERAL_STYLE    = dclare(TextStyle.class, Color.DARK_GRAY, null, true, false, false, null);

    @Property(key = 0)
    WBUniverse wb();

    @Override
    default String string() {
        return wb().text().string().toString();
    }

    @Rule
    default void toText() {
        set(wb().text(), Text::string, string());
    }

    @Override
    default List<TokenTextElement> elements() {
        WBUniverse wb = wb();
        MyText text = wb.text();
        return text.tokenizer().lines().flatMap(line -> line.tokens().map(token -> {
            int start = token.line().startInText() + token.startInLine();
            int len = token.value().length();
            TextStyle style = style(wb.problemTokens().contains(token) ? null : token.type());
            return dclare(TokenTextElement.class, start, len, style, token);
        })).toList();
    }

    default TextStyle style(TokenType type) {
        if (type == null) {
            return ERROR_STYLE;
        } else if (type.literal() != null) {
            return LITERAL_STYLE;
        } else if (type.terminals().anyMatch(t -> IDENTIFIER.class.isAssignableFrom(t.jClass()))) {
            return IDENTIFIER_STYLE;
        } else if (type.terminals().anyMatch(t -> STRING.class.isAssignableFrom(t.jClass()))) {
            return STRING_STYLE;
        } else if (type.terminals().anyMatch(t -> NUMBER.class.isAssignableFrom(t.jClass()))) {
            return NUMBER_STYLE;
        } else {
            return STRING_STYLE;
        }
    }

    @Override
    default Color background() {
        return new Color(255, 255, 200);
    }

    @Rule
    default void setProblemSelection() {
        DProblem pre = pre(wb(), WBUniverse::selectedProblem);
        DProblem post = wb().selectedProblem();
        if (post != null && !post.equals(pre) && post.context() instanceof Token) {
            Token token = (Token) post.context();
            int start = token.line().startInText() + token.startInLine();
            int end = start + token.value().length();
            set(this, DTextComponent::selection, Pair.of(start, end));
        }
    }

    interface TokenTextElement extends TextElement, DStruct4<Integer, Integer, TextStyle, Token> {

        @Property(key = 3)
        Token token();

        @Override
        default PopupMenu popupMenu() {
            return dclare(Menu.class, this);
        }

    }

    interface Menu extends PopupMenu, DStruct1<TokenTextElement> {

        @Property(key = 0)
        TokenTextElement element();

        @Override
        default List<MenuItem> items() {
            return List.of(dclare(MenuItem.class, this, "hallo", DClare.id(e -> {
                System.err.println("POPUP MENU CALLED FOR: " + element().token());
            }, this)));

        }

    }

    class ErrorHighlightPainter implements HighlightPainter {

        private static final int         AMT    = 2;
        private static final BasicStroke STROKE = new BasicStroke(1.2f);

        @Override
        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c) {
            try {
                Rectangle2D r = c.modelToView2D(p0);
                r.add(c.modelToView2D(p1));
                g.setColor(Color.RED);
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(STROKE);
                paintLine(g, r);
            } catch (BadLocationException e) {
                throw new Error(e);
            }
        }

        private void paintLine(Graphics g, Rectangle2D r) {
            int x = (int) r.getX();
            int y = (int) (r.getY() + r.getHeight() - AMT);
            int delta = -AMT;
            while (x < r.getX() + r.getWidth()) {
                g.drawLine(x, y, x + AMT, y + delta);
                y += delta;
                delta = -delta;
                x += AMT;
            }
        }

    }
}
