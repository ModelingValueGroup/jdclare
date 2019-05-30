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

package org.modelingvalue.jdclare.syntax.meta;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.regex.Pattern;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.Grammar.Node;
import org.modelingvalue.jdclare.syntax.Literal;
import org.modelingvalue.jdclare.syntax.Regex;

public interface TerminalClass<T extends Node> extends NodeClass<T> {

    @Constraints
    private void tConstraints() {
        DClare.<TerminalClass<?>, TokenType, TokenType, Set<TerminalClass<?>>> OPPOSITE(TerminalClass::token, TokenType::terminals);
    }

    @Property(constant)
    default TokenType token() {
        return dclare(TokenType.class, grammar(), regex(), literal(), set(TokenType::skipped, false));
    }

    private String regex() {
        String literal = literal();
        if (literal != null) {
            return Pattern.quote(literal);
        } else {
            Regex regex = ann(jClass(), Regex.class);
            return regex != null ? regex.value() : ".+";
        }
    }

    private String literal() {
        Literal literal = ann(jClass(), Literal.class);
        return literal != null ? literal.value() : null;
    }

    @Override
    default boolean match(NodeType synt) {
        return equals(synt);
    }

    @Override
    default Set<TerminalClass<?>> firstTerminals(Set<NodeType> done) {
        return Set.of(this);
    }

    @Override
    default Set<TerminalClass<?>> lastTerminals(Set<NodeType> done) {
        return Set.of(this);
    }

    @Property(constant)
    default SyntaxProperty<Node, Object> syntaxProperty() {
        Collection<SyntaxProperty<Node, Object>> properties = syntaxProperties();
        return properties.findAny().orElse(null);
    }

}
