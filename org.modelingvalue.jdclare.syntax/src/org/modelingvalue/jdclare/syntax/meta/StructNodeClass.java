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
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.syntax.StructNode;
import org.modelingvalue.jdclare.syntax.parser.ElementParser;
import org.modelingvalue.jdclare.syntax.parser.NodeParser;
import org.modelingvalue.jdclare.syntax.parser.SequenceElementParser;
import org.modelingvalue.jdclare.syntax.parser.SequenceParser;
import org.modelingvalue.jdclare.syntax.parser.TerminalParser;

public interface StructNodeClass<T extends StructNode> extends NodeClass<T> {

    @Override
    default T create(NodeParser nodeParser, Set<DProblem>[] problems) {
        List<DProperty<T, ?>> keys = keys();
        Object[] key = new Object[keys.size()];
        for (DProperty<T, ?> kp : keys) {
            if (kp instanceof SyntaxProperty) {
                SyntaxProperty<T, ?> sp = (SyntaxProperty<T, ?>) kp;
                ElementParser elementParser = nodeParser instanceof TerminalParser ? (TerminalParser) nodeParser : //
                        (SequenceElementParser) ((SequenceParser) nodeParser).sequenceElementParsers().get(sp.nr() * SyntaxProperty.STEP_SIZE);
                problems[0] = problems[0].addAll(sp.transform(elementParser, v -> key[sp.keyNr()] = v, null));
            }
        }
        return dStruct(jClass(), key);
    }

}
