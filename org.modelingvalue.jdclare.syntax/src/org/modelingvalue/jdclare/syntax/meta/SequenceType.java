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

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Property;

public interface SequenceType extends NodeType {

    @Property(constant)
    List<SequenceElement> sequenceElements();

    @Property(constant)
    default SequenceElement startElement() {
        List<SequenceElement> singletons = sequenceElements().filter(e -> !e.many() && e.mandatory()).toList();
        return singletons.filter(e -> e.nodeType() instanceof TerminalClass && ((TerminalClass<?>) e.nodeType()).token().literal() != null).findFirst().orElse(singletons.first());
    }

    @Property(constant)
    default NodeType startClass() {
        SequenceElement startElement = startElement();
        return startElement != null ? startElement.nodeType() : null;
    }

    @Override
    @Property(constant)
    default Set<TerminalClass<?>> firstTerminals(Set<NodeType> done) {
        NodeType nodeType = sequenceElements().first().nodeType();
        return !done.contains(nodeType) ? nodeType.firstTerminals(done) : Set.of();
    }

    @Override
    @Property(constant)
    default Set<TerminalClass<?>> lastTerminals(Set<NodeType> done) {
        NodeType nodeType = sequenceElements().last().nodeType();
        return !done.contains(nodeType) ? nodeType.lastTerminals(done) : Set.of();
    }

}
