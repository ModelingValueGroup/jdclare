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

package org.modelingvalue.jdclare.syntax;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.Objects;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.meta.TokenType;
import org.modelingvalue.jdclare.syntax.parser.TerminalParser;
import org.modelingvalue.jdclare.syntax.regex.DMatch;
import org.modelingvalue.jdclare.syntax.regex.DMultiMatcher;

public interface Line extends DObject, DStruct2<Text<?, ?>, Long> {

    @Constraints
    private void constraints() {
        OPPOSITE(Line::previous, Line::next);
    }

    @Property(key = 0)
    Text<?, ?> text();

    @Property(key = 1)
    long id();

    @Property
    int startInText();

    @Property
    String string();

    @Property(optional)
    Line next();

    @Property(optional)
    Line previous();

    @Property
    default Set<TerminalParser> terminals() {
        return tokens().flatMap(Token::terminals).toSet();
    }

    @Property
    default List<DMatch> matches() {
        return dclare(DMultiMatcher.class, text().grammar().tokenPatterns(), string()).matches().toList();
    }

    @Property(containment)
    default List<Token> tokens() {
        List<TokenType> tokens = text().grammar().tokens();
        return matches().reuse(tokens(), (t, m) -> m.value().equals(t.value()), (t, m) -> {
            set(t, Token::startInLine, m.start());
            set(t, Token::value, m.value());
            set(t, Token::type, type(tokens, m));
        }, Token::id, (t, m) -> Objects.equals(t.type(), type(tokens, m)), (i, m) -> dclare(Token.class, Line.this, i)).linked((p, t, n) -> {
            if (n != null) {
                set(t, Token::nextToken, n);
            }
            return t;
        }).toList();
    }

    private TokenType type(List<TokenType> tokens, DMatch m) {
        return m.multiNr() < 0 ? null : tokens.get(m.multiNr());
    }

    default int nr() {
        Line prev = previous();
        return prev != null ? prev.nr() + 1 : 0;
    }

    @Override
    default String asString() {
        return "L" + nr();
    }

}
