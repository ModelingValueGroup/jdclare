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

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.syntax.Grammar.Node;
import org.modelingvalue.jdclare.syntax.Sequence;

public interface SequenceClass<T extends Node> extends NodeClass<T>, SequenceType {

    @SuppressWarnings("resource")
    @Override
    default List<SequenceElement> sequenceElements() {
        Set<SequenceElement> elements = syntaxProperties().flatMap(p -> p.elements()).toSet();
        int[] nr = new int[1];
        for (Class<? extends Node> cls : DClare.ann(jClass(), Sequence.class).value()) {
            while (elements.anyMatch(e -> e.nr() == nr[0] * SyntaxProperty.STEP_SIZE)) {
                nr[0]++;
            }
            elements = elements.add(dclare(SequenceElement.class, SyntaxProperty.STEP_SIZE * nr[0]++, (NodeClass<?>) DClare.dClass(cls), true, false, null));
        }
        return elements.sorted((a, b) -> Integer.compare(a.nr(), b.nr())).toList();
    }

    @Override
    default boolean match(NodeType synt) {
        return equals(synt);
    }

    @Override
    default Set<TerminalClass<?>> firstTerminals(Set<NodeType> done) {
        return SequenceType.super.firstTerminals(done);
    }

    @Override
    default Set<TerminalClass<?>> lastTerminals(Set<NodeType> done) {
        return SequenceType.super.lastTerminals(done);
    }

}
