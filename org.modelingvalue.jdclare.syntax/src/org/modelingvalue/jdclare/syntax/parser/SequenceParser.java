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
import org.modelingvalue.jdclare.Constraints;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.Text;
import org.modelingvalue.jdclare.syntax.meta.NodeType;
import org.modelingvalue.jdclare.syntax.meta.SequenceElement;
import org.modelingvalue.jdclare.syntax.meta.SequenceType;

public interface SequenceParser extends NodeParser, DStruct2<NodeParser, SequenceType> {

    @Constraints
    private void constraints() {
        OPPOSITE(SequenceParser::downs, NodeParser::uppers);
        OPPOSITE(SequenceParser::first, NodeParser::firstOppos);
        OPPOSITE(SequenceParser::last, NodeParser::lastOppos);
    }

    @Property(key = 0)
    NodeParser start();

    @Override
    @Property(key = 1)
    SequenceType type();

    @Override
    default Text<?, ?> text() {
        return start().text();
    }

    @Override
    default String value() {
        return null;
    }

    @Property({constant, containment})
    default List<SequenceElementParser> sequenceElementParsers() {
        SequenceType type = type();
        List<SequenceElement> elements = type.sequenceElements();
        int snr = type.startElement().nr();
        return elements.map(e -> dclare(SequenceElementParser.class, this, e.nr() - snr, e)).linked((p, e, n) -> {
            set(e, SequenceElementParser::pre, p);
            set(e, SequenceElementParser::post, n);
            return e;
        }).toList();
    }

    @Property
    default List<NodeParser> downs() {
        return sequenceElementParsers().flatMap(SequenceElementParser::nodes).toList();
    }

    @Override
    default boolean matched() {
        return sequenceElementParsers().sequential().allMatch(SequenceElementParser::matched);
    }

    @Property(optional)
    default NodeParser first() {
        return downs().first();
    }

    @Property(optional)
    default NodeParser last() {
        return downs().last();
    }

    @Override
    default TerminalParser firstTerminal() {
        NodeParser first = first();
        return first != null ? first.firstTerminal() : null;
    }

    @Override
    default TerminalParser lastTerminal() {
        NodeParser last = last();
        return last != null ? last.lastTerminal() : null;
    }

    @Override
    default NodeParser nextMatch(NodeType pattern, SequenceParser upper) {
        NodeParser last = last();
        return last == null ? null : last.nextMatch(pattern, upper);
    }

    @Override
    default NodeParser previousMatch(NodeType pattern, SequenceParser upper) {
        NodeParser first = first();
        return first == null ? null : first.previousMatch(pattern, upper);
    }

    @Override
    default String asString() {
        return "S[" + type().asString() + "," + start() + "]";
    }

}
