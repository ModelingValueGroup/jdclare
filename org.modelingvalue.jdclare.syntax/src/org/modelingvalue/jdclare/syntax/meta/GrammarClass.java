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

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.meta.DClass;
import org.modelingvalue.jdclare.syntax.Grammar;
import org.modelingvalue.jdclare.syntax.Node;
import org.modelingvalue.jdclare.syntax.Skipped;
import org.modelingvalue.jdclare.syntax.regex.DPattern;

public interface GrammarClass<T extends Grammar> extends DClass<T> {

    @Property(constant)
    default DPattern newLinePattern() {
        return dclare(DPattern.class, "\\r?\\n");
    }

    @Property(constant)
    default Set<TokenType> skipped() {
        Skipped skipped = ann(jClass(), Skipped.class);
        return skipped == null ? Set.of() : Collection.of(skipped.value()).map(r -> {
            return dclare(TokenType.class, GrammarClass.this, r, null, set(TokenType::skipped, true));
        }).toSet();
    }

    @SuppressWarnings("rawtypes")
    @Property(constant)
    default Set<NodeClass> syntaxNodes() {
        return classes().filter(NodeClass.class).toSet();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Property(constant)
    default Set<SequenceType> sequences() {
        Collection<SequenceClass<Node>> sequences = (Collection) syntaxNodes().filter(SequenceClass.class);
        return sequences.flatMap(s -> Collection.concat(s, s.syntaxProperties().flatMap(SyntaxProperty::anonymousSequenceTypes))).toSet();
    }

    @SuppressWarnings("rawtypes")
    @Property(constant)
    default Set<TerminalClass> terminals() {
        return syntaxNodes().filter(TerminalClass.class).toSet();
    }

    @Property({constant, containment})
    default List<TokenType> tokens() {
        return Collection.concat(terminals().map(TerminalClass::token), skipped()).sorted(TokenType::compare).toList();
    }

    @Property(constant)
    default List<DPattern> tokenPatterns() {
        return tokens().map(TokenType::pattern).toList();
    }

}
