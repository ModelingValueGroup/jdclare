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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DSeverity;
import org.modelingvalue.jdclare.DStruct3;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.Token;
import org.modelingvalue.jdclare.syntax.meta.NodeType;
import org.modelingvalue.jdclare.syntax.meta.SequenceElement;
import org.modelingvalue.jdclare.syntax.meta.SyntaxProperty;
import org.modelingvalue.jdclare.syntax.meta.TerminalClass;
import org.modelingvalue.jdclare.syntax.meta.TokenType;

public interface SequenceElementParser extends ElementParser, DStruct3<SequenceParser, Integer, SequenceElement> {

    @Constraints
    private void constraints() {
        OPPOSITE(SequenceElementParser::post, SequenceElementParser::pre);
    }

    @Property(key = 0)
    SequenceParser sequence();

    @Property(key = 1)
    int direction();

    @Property(value = visible, key = 2)
    SequenceElement element();

    @Override
    @Property(constant)
    SequenceElementParser post();

    @Override
    @Property(constant)
    SequenceElementParser pre();

    @Property(optional)
    default NodeParser first() {
        NodeParser first = nodes().first();
        if (first == null) {
            SequenceElementParser next = post();
            first = next != null ? next.first() : null;
        }
        return first;
    }

    @Property(optional)
    default NodeParser last() {
        NodeParser last = nodes().last();
        if (last == null) {
            SequenceElementParser previous = pre();
            last = previous != null ? previous.last() : null;
        }
        return last;
    }

    @Property(optional)
    default NodeParser fix() {
        int dir = direction();
        SequenceElementParser border = dir > 0 ? pre() : dir < 0 ? post() : null;
        return border != null && border.matched() ? (dir > 0 ? border.last() : border.first()) : null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    default SyntaxProperty property() {
        return element().property();
    }

    @Override
    default NodeParser base() {
        return sequence();
    }

    @Property
    default boolean matched() {
        return !element().mandatory() || !nodes().isEmpty();
    }

    @Override
    default List<NodeParser> nodes() {
        SequenceParser sequence = sequence();
        int dir = direction();
        if (dir == 0) {
            return List.of(sequence.start());
        }
        NodeParser node = fix();
        if (node == null) {
            return List.of();
        }
        SequenceElement element = element();
        NodeType pattern = element.nodeType();
        node = next(dir, node, pattern, sequence);
        if (node == null) {
            return List.of();
        }
        List<NodeParser> nodes = List.of(node);
        if (element.many()) {
            for (node = next(dir, node, pattern, sequence); node != null; node = next(dir, node, pattern, sequence)) {
                nodes = add(dir, nodes, node);
            }
        }
        return nodes;
    }

    private List<NodeParser> add(int dir, List<NodeParser> nodes, NodeParser node) {
        return dir > 0 ? nodes.append(node) : nodes.prepend(node);
    }

    private NodeParser next(int dir, NodeParser node, NodeType pattern, SequenceParser upper) {
        return dir > 0 ? node.nextMatch(pattern, upper) : node.previousMatch(pattern, upper);
    }

    @Property(validation)
    default DProblem problem() {
        if (element().mandatory() && !sequence().start().resolved() && !matched()) {
            NodeParser fix = fix();
            if (fix != null) {
                int dir = direction();
                TerminalParser terminal = dir > 0 ? fix.lastTerminal() : fix.firstTerminal();
                if (terminal != null) {
                    Token fixToken = terminal.token();
                    Token foundToken = dir > 0 ? fixToken.next() : fixToken.previous();
                    Set<TerminalClass<?>> expected = Set.of();
                    for (SequenceElementParser alt = this; alt != null; alt = dir > 0 ? alt.pre() : alt.post()) {
                        if (!alt.nodes().isEmpty()) {
                            break;
                        } else {
                            NodeType nodeType = alt.element().nodeType();
                            expected = expected.addAll(dir > 0 ? nodeType.firstTerminals(Set.of()) : nodeType.lastTerminals(Set.of()));
                        }
                    }
                    String expectedString = expected.map(TerminalClass::token).map(TokenType::string).sorted().//
                            reduce("", (a, b) -> a.length() == 0 || b.length() == 0 ? a + b : a + " or " + b);
                    return dclare(DProblem.class, foundToken != null ? foundToken : fixToken, "SYNTAX", DSeverity.error, //
                            "Expected: " + expectedString + "," + (dir > 0 ? " after: '" : " before: '") + fixToken.value() + //
                                    "', found: " + (foundToken != null ? ("'" + foundToken.value() + "'") : (dir > 0 ? "<END>" : "<BEGIN>")));
                }
            }
        }
        return null;
    }

    @Override
    default String asString() {
        return sequence().toString() + "." + direction();
    }

}
