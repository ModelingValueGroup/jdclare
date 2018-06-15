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

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DStruct3;
import org.modelingvalue.jdclare.Property;

public interface AnonymousSequenceType extends SequenceType, DStruct3<GrammarClass<?>, SyntaxProperty<?, ?>, List<SequenceElement>> {

    @Override
    @Property(key = 0)
    GrammarClass<?> grammar();

    @Property(key = 1)
    SyntaxProperty<?, ?> property();

    @Override
    @Property(key = 2)
    List<SequenceElement> sequenceElements();

    @Override
    default boolean match(NodeType synt) {
        return equals(synt);
    }

    @Override
    default String asString() {
        return sequenceElements().map(se -> se.nodeType()).toList().toString().substring(4);
    }

}
