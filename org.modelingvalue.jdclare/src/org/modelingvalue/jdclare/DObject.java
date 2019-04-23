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

package org.modelingvalue.jdclare;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.NonLockingPrintWriter;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.jdclare.java.JObjectClass;
import org.modelingvalue.jdclare.meta.DObjectClass;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.meta.DRule;
import org.modelingvalue.jdclare.meta.DStructClass;
import org.modelingvalue.transactions.Contained;
import org.modelingvalue.transactions.EmptyMandatoryException;

@Extend(JObjectClass.class)
public interface DObject extends DStruct, Contained {

    @Override
    default Contained dContainer() {
        return dParent();
    }

    @Property({optional, hidden})
    DObject dParent();

    @Property({optional, hidden})
    DProperty<?, ?> dContainmentProperty();

    @Property(hidden)
    Set<DObject> dChildren();

    @SuppressWarnings("unchecked")
    default <C extends DObject> C dAncestor(Class<C> cls) {
        DObject parent = dParent();
        while (parent != null && !cls.isInstance(parent)) {
            parent = parent.dParent();
        }
        return (C) parent;
    }

    @SuppressWarnings("unchecked")
    @Property({constant, hidden})
    default <T extends DObject> DObjectClass<T> dObjectClass() {
        DStructClass<DStruct> dStructClass = dStructClass();
        return (DObjectClass<T>) (DStruct) dStructClass;
    }

    @Property(hidden)
    Map<Object, Set<DProblem>> dProblemsMap();

    @SuppressWarnings("unchecked")
    @Property(hidden)
    default Set<DProblem> dProblems() {
        return Collection.concat(dObjectClass().allValidations().flatMap(p -> (Collection<DProblem>) p.getCollection(this)), //
                dProblemsMap().flatMap(e -> e.getValue())).toSet();
    }

    @Property(hidden)
    default Set<DProblem> dAllProblems() {
        return dProblems().addAll(dChildren().flatMap(DObject::dAllProblems));
    }

    @SuppressWarnings("rawtypes")
    @Property({containment, hidden})
    Set<DRule> dObjectRules();

    @Property(hidden)
    default int dSize() {
        return 1 + dChildren().map(DObject::dSize).reduce(0, Integer::sum);
    }

    default String dString() {
        return dString("");
    }

    default String dString(String prefix) {
        StringBuffer sb = new StringBuffer();
        dDump(NonLockingPrintWriter.of(s -> sb.append(s)), prefix);
        return sb.toString();
    }

    default void dDump(PrintStream stream) {
        dDump(stream, "");
    }

    default void dDump(PrintWriter writer) {
        dDump(writer, "");
    }

    default void dDump(PrintStream stream, String prefix) {
        dDump(new PrintWriter(stream, true), prefix);
    }

    @SuppressWarnings("unchecked")
    default void dDump(PrintWriter writer, String prefix) {
        writer.println(prefix + this + " (" + dObjectClass().name() + ") {");
        DObjectClass<DObject> cls = dObjectClass();
        if (cls != null) {
            for (DProperty<DObject, ?> p : cls.allNonContainments().sorted()) {
                if (p.visible()) {
                    writer.println(prefix + "  " + p.name() + " = " + StringUtil.toString(p.get(this)));
                }
            }
            for (DProperty<DObject, ?> p : cls.allContainments().sorted()) {
                if (p.visible()) {
                    Collection<DObject> coll = (Collection<DObject>) p.getCollection(this);
                    if (!coll.isEmpty()) {
                        writer.println(prefix + "  " + p.name() + " = {");
                        for (DObject child : (coll instanceof List ? coll : coll.sorted())) {
                            child.dDump(writer, prefix + "    ");
                        }
                        writer.println(prefix + "  }");
                    } else {
                        writer.println(prefix + "  " + p.name() + " = {}");
                    }
                }
            }
        } else {
            writer.println(prefix + "   ???");
        }
        writer.println(prefix + "}");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({containment, hidden})
    private Set<ScopeChecker> scopeCheckers() {
        return dObjectClass().scopedProperties().map(p -> dclare(ScopeChecker.class, this, p.getKey(), p.getValue())).toSet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({containment, hidden})
    private Set<MandatoryChecker> mandatoryCheckers() {
        return dObjectClass().mandatoryProperties().map(p -> dclare(MandatoryChecker.class, this, p)).toSet();
    }

    interface ScopeChecker<T extends DObject> extends DObject, DStruct3<T, DProperty<T, ?>, DProperty<T, Collection<?>>> {

        @Property(key = 0)
        T object();

        @Property(key = 1)
        DProperty<T, ?> property();

        @Property(key = 2)
        DProperty<T, Collection<?>> scope();

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Property({validation, hidden})
        default Set<DProblem> scopeProblems() {
            DProperty<T, ?> p = property();
            Object v = p.get(object());
            Set s = scope().get(object()).toSet();
            if (v instanceof ContainingCollection) {
                return ((Collection) v).filter(e -> !s.contains(e)).map(e -> //
                dclare(DProblem.class, object(), "SCOPE", DSeverity.fatal, "the " + p.name() + " " + e + " is not in scope.")).toSet();
            } else if (v != null && !s.contains(v)) {
                return Set.of(dclare(DProblem.class, object(), "SCOPE", DSeverity.fatal, p.name() + " " + v + " is not in scope."));
            }
            return Set.of();
        }

    }

    interface MandatoryChecker<T extends DObject> extends DObject, DStruct2<T, DProperty<T, ?>> {

        @Property(key = 0)
        T object();

        @Property(key = 1)
        DProperty<T, ?> property();

        @Property({validation, hidden})
        default DProblem mandatoryProblem() {
            DProperty<T, ?> p = property();
            try {
                p.get(object());
            } catch (EmptyMandatoryException ooe) {
                return dclare(DProblem.class, object(), "MANDATORY", DSeverity.fatal, p.name() + " is empty.");
            }
            return null;
        }

    }

}
