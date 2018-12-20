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

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.syntax.regex.DMatch;

public interface Tokenizer<S extends Grammar> extends DStruct1<Text<S, ?>>, DObject {

    @Property(key = 0)
    Text<S, ?> text();

    @Property
    default List<DMatch> matches() {
        return text().grammar().newLinePattern().matcher(text().string()).matches(true).toList();
    }

    @Property(containment)
    default List<Line> lines() {
        return matches().reuse(lines(), (l, m) -> m.value().equals(l.string()), (l, m) -> {
            set(l, Line::startInText, m.start());
            set(l, Line::string, m.value());
        }, Line::id, (l, m) -> true, (i, m) -> dclare(Line.class, text(), i)).linked((p, l, n) -> {
            set(l, Line::next, n);
            return l;
        }).toList();
    }

    @Rule
    default void connectLineEnTokens() {
        lines().linked((p, l, n) -> {
            List<Token> tokens = l.tokens();
            Token first = tokens.first();
            Token last = tokens.last();
            if (p == null && first != null) {
                set(first, Token::previousToken, null);
            }
            if (last != null) {
                set(last, Token::nextToken, n != null ? n.tokens().first() : null);
            }
        });
    }

}
