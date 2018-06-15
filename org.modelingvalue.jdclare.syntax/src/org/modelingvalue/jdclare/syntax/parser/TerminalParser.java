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

package org.modelingvalue.jdclare.syntax.parser;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.Text;
import org.modelingvalue.jdclare.syntax.Token;
import org.modelingvalue.jdclare.syntax.meta.NodeType;
import org.modelingvalue.jdclare.syntax.meta.SyntaxProperty;
import org.modelingvalue.jdclare.syntax.meta.TerminalClass;

public interface TerminalParser extends NodeParser, ElementParser, DStruct2<TerminalClass<?>, Token> {

    @Override
    @Property(key = 0)
    TerminalClass<?> type();

    @Property(key = 1)
    Token token();

    @Override
    @Property(constant)
    default Text<?, ?> text() {
        return token().text();
    }

    @Override
    default boolean root() {
        return false;
    }

    @Override
    default String value() {
        return token().value();
    }

    @Override
    @Property(constant)
    default boolean matched() {
        return true;
    }

    @Override
    @Property(constant)
    default SequenceElementParser pre() {
        return null;
    }

    @Override
    @Property(constant)
    default SequenceElementParser post() {
        return null;
    }

    @Override
    default List<NodeParser> nodes() {
        return List.of(this);
    }

    @SuppressWarnings("rawtypes")
    @Override
    default SyntaxProperty property() {
        return type().syntaxProperty();
    }

    @Override
    default NodeParser base() {
        return this;
    }

    @Override
    default NodeParser nextMatch(NodeType pattern, SequenceParser upper) {
        Token next = token().next();
        if (next != null) {
            for (TerminalParser term : next.terminals()) {
                NodeParser node = term.firstUpper(pattern, upper);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    default NodeParser previousMatch(NodeType pattern, SequenceParser upper) {
        Token prev = token().previous();
        if (prev != null) {
            for (TerminalParser term : prev.terminals()) {
                NodeParser node = term.lastUpper(pattern, upper);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }

    @Override
    default TerminalParser firstTerminal() {
        return this;
    }

    @Override
    default TerminalParser lastTerminal() {
        return this;
    }

    @Override
    default String asString() {
        return "T[" + type().asString() + "," + token() + "]";
    }

}
