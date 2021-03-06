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

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Extend;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.syntax.meta.GrammarClass;
import org.modelingvalue.jdclare.syntax.meta.NodeClass;
import org.modelingvalue.jdclare.syntax.meta.ObjectNodeClass;
import org.modelingvalue.jdclare.syntax.meta.StructNodeClass;
import org.modelingvalue.jdclare.syntax.parser.NodeParser;

@Extend(GrammarClass.class)
public interface Grammar extends DObject {

    @SuppressWarnings("unchecked")
    @Property({constant, hidden})
    default <T extends Grammar> GrammarClass<T> sSyntaxClass() {
        return (GrammarClass<T>) (DStruct) dClass();
    }

    @Extend(NodeClass.class)
    public interface Node extends DStruct {
    }

    @Extend(ObjectNodeClass.class)
    public interface ObjectNode extends DObject, Node, DStruct1<NodeParser> {
        @Property(key = 0)
        NodeParser sParserNode();

        default boolean fromEqualText(DObject object) {
            return object instanceof ObjectNode && ((ObjectNode) object).sParserNode().text().equals(sParserNode().text());
        }

    }

    @Extend(StructNodeClass.class)
    public interface StructNode extends Node {
    }

}
