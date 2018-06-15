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

import java.util.function.Consumer;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DSeverity;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.java.JProperty;
import org.modelingvalue.jdclare.syntax.Node;
import org.modelingvalue.jdclare.syntax.StructNode;
import org.modelingvalue.jdclare.syntax.Syntax;
import org.modelingvalue.jdclare.syntax.parser.ElementParser;
import org.modelingvalue.jdclare.syntax.parser.NodeParser;
import org.modelingvalue.jdclare.syntax.parser.SequenceParser;

public interface SyntaxProperty<O extends Node, V> extends JProperty<O, V> {

    int STEP_SIZE = 2;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Property(constant)
    default NodeClass<?> nodeClass() {
        Class<? extends Node> cls = (Class) method().getDeclaringClass();
        return cls != null ? (NodeClass) dClass(cls) : null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property(constant)
    default NodeClass<?> nodeType() {
        if (nodeClass() instanceof SequenceClass) {
            Class<? extends Node> type = DClare.ann(method(), Syntax.class).type();
            return (NodeClass) dClass(type != Node.class ? type : elementClass());
        } else {
            return nodeClass();
        }
    }

    @Property(constant)
    default Class<?> rawType() {
        return method().getReturnType();
    }

    @Property(constant)
    default GrammarClass<?> syntax() {
        return nodeClass().grammar();
    }

    @Property(constant)
    default int nr() {
        return DClare.ann(method(), Syntax.class).nr();
    }

    @Property({constant, containment})
    default Set<SequenceType> anonymousSequenceTypes() {
        Set<SequenceType> types = Set.of();
        SequenceType prePost = prePostFixSequenceType();
        if (prePost != null) {
            types = types.add(prePost);
        }
        SequenceType separator = separatorSequenceType();
        if (separator != null) {
            types = types.add(separator);
        }
        return types;
    }

    @Property(constant)
    default NodeType elementType() {
        NodeType prePost = prePostFixSequenceType();
        return prePost != null ? prePost : nodeType();
    }

    @Property(constant)
    default SequenceType prePostFixSequenceType() {
        if (nodeClass() instanceof SequenceClass) {
            Syntax syntax = DClare.ann(method(), Syntax.class);
            Class<? extends Node>[] prefix = syntax.prefix();
            Class<? extends Node>[] postfix = syntax.postfix();
            if (prefix.length > 0 || postfix.length > 0) {
                int nr = 0;
                List<SequenceElement> elements = List.of();
                for (int i = 0; i < prefix.length; i++) {
                    elements = elements.add(dclare(SequenceElement.class, nr++, (NodeClass<?>) dClass(prefix[i]), true, false, null));
                }
                elements = elements.add(dclare(SequenceElement.class, nr++, nodeType(), true, false, null));
                for (int i = 0; i < postfix.length; i++) {
                    elements = elements.add(dclare(SequenceElement.class, nr++, (NodeClass<?>) dClass(postfix[i]), true, false, null));
                }
                return dclare(AnonymousSequenceType.class, syntax(), this, elements);
            }
        }
        return null;
    }

    @Property(constant)
    default SequenceType separatorSequenceType() {
        if (nodeClass() instanceof SequenceClass) {
            Syntax syntax = DClare.ann(method(), Syntax.class);
            Class<? extends Node>[] separators = syntax.separator();
            if (separators.length > 0) {
                List<SequenceElement> elements = List.of();
                int nr = 0;
                for (int i = 0; i < separators.length; i++) {
                    elements = elements.add(dclare(SequenceElement.class, nr++, (NodeClass<?>) dClass(separators[i]), true, false, null));
                }
                elements = elements.add(dclare(SequenceElement.class, nr++, elementType(), true, false, null));
                return dclare(AnonymousSequenceType.class, syntax(), this, elements);
            }
        }
        return null;
    }

    @Property(constant)
    default Class<? extends Node>[] separators() {
        return DClare.ann(method(), Syntax.class).separator();
    }

    @Property(constant)
    default Class<? extends Node>[] prefix() {
        return DClare.ann(method(), Syntax.class).prefix();
    }

    @Property(constant)
    default Class<? extends Node>[] postfix() {
        return DClare.ann(method(), Syntax.class).postfix();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default Set<DProblem> transform(ElementParser parser, Consumer<V> setter, DObject instance) {
        Class<? extends Node>[] separators = separators();
        Class<? extends Node>[] prefix = prefix();
        Class<? extends Node>[] postfix = postfix();
        Class<? extends Node> type = nodeType().jClass();
        Class<?> cls = elementClass();
        Set<DProblem> problems = Set.of();
        List<NodeParser> nodes = parser.nodes();
        if (separators.length > 0 && !nodes.isEmpty()) {
            nodes = nodes.addAll(parser.post().nodes().filter(SequenceParser.class).flatMap(p -> p.sequenceElementParsers().get(1).nodes()));
        }
        if (prefix.length > 0 || postfix.length > 0) {
            nodes = nodes.filter(SequenceParser.class).flatMap(n -> n.sequenceElementParsers().get(prefix.length).nodes()).toList();
        }
        List list = List.of();
        Set scope = null;
        for (NodeParser node : nodes) {
            if (cls.isAssignableFrom(type)) {
                Node value = node.abstractNode();
                if (value != null) {
                    if (value instanceof StructNode && !StructNode.class.isAssignableFrom(cls)) {
                        Class target = DClare.<Class> supers(cls, jClass(value), (r, s) -> !Node.class.isAssignableFrom(s) && r.isAssignableFrom(s) ? s : r);
                        list = list.append(dStruct(target, handler(value).key));
                    } else {
                        list = list.append(value);
                    }
                }
            } else {
                String name = node.value();
                if (cls.isAssignableFrom(String.class)) {
                    list = list.append(name);
                } else if (cls.isAssignableFrom(int.class) || cls.isAssignableFrom(Integer.class)) {
                    try {
                        list = list.append(Integer.parseInt(name));
                    } catch (NumberFormatException nfe) {
                    }
                } else if (cls.isAssignableFrom(boolean.class) || cls.isAssignableFrom(Boolean.class)) {
                    list = list.append(Boolean.parseBoolean(name));
                } else {
                    if (scope == null) {
                        scope = scopeProperty().get((O) instance).toSet();
                    }
                    Set found = scope.filter(v -> name.equals(v.toString())).toSet();
                    if (found.size() != 1) {
                        problems = problems.add(dclare(DProblem.class, node.firstTerminal().token(), //
                                "REFERENCE", DSeverity.error, "'" + name + "' cannot be resolved to a " + cls.getSimpleName() + //
                                        ", must be one of " + scope.toString().substring(4)));
                    } else {
                        list = list.append(found.get(0));
                    }
                }
            }
        }
        setter.accept(many() ? Set.class.isAssignableFrom(rawType()) ? (V) list.toSet() : (V) list : list.isEmpty() ? defaultValue() : (V) list.get(0));
        return problems;
    }

    @Property(constant)
    default Set<SequenceElement> elements() {
        Set<SequenceElement> elements = Set.of();
        if (nodeClass() instanceof SequenceClass) {
            Syntax syntax = DClare.ann(method(), Syntax.class);
            int nr = nr() * STEP_SIZE;
            if (syntax.separator().length > 0) {
                elements = elements.add(dclare(SequenceElement.class, nr++, elementType(), mandatory(), false, this));
                elements = elements.add(dclare(SequenceElement.class, nr++, separatorSequenceType(), false, true, null));
            } else {
                elements = elements.add(dclare(SequenceElement.class, nr++, elementType(), mandatory(), many(), this));
            }
        }
        return elements;
    }

}
