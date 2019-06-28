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
import org.modelingvalue.jdclare.meta.DClass;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.meta.DRule;
import org.modelingvalue.jdclare.meta.DStructClass;
import org.modelingvalue.transactions.Constant;
import org.modelingvalue.transactions.EmptyMandatoryException;
import org.modelingvalue.transactions.Mutable;
import org.modelingvalue.transactions.MutableTransaction;
import org.modelingvalue.transactions.Observer;
import org.modelingvalue.transactions.Setable;
import org.modelingvalue.transactions.State;
import org.modelingvalue.transactions.Transaction;
import org.modelingvalue.transactions.UniverseTransaction;

@Extend(DClass.class)
public interface DObject extends DStruct, Mutable {

    @Override
    default State run(State state, MutableTransaction parent) {
        return Mutable.super.run(state, parent);
    }

    @Override
    default DObject dParent() {
        return (DObject) Mutable.super.dParent();
    }

    default DProperty<?, ?> dContainmentProperty() {
        return (DProperty<?, ?>) Mutable.super.dContaining().id();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<DObject> dChildren() {
        return (Collection<DObject>) Mutable.super.dChildren();
    }

    @SuppressWarnings("unchecked")
    @Override
    default Collection<DObject> dChildren(State state) {
        return (Collection<DObject>) Mutable.super.dChildren(state);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Collection<? extends Observer<?>> dObservers() {
        return (Collection) Collection.concat(dClare().bootsTrap(this), dClass().allRules(), dObjectRules()).map(DRule::observer);
    }

    @Override
    default Collection<? extends Setable<? extends Mutable, ?>> dContainers() {
        return dClass().allContainments().map(DClare::setable);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    default Collection<? extends Constant<? extends Mutable, ?>> dConstants() {
        return dClass().allConstants().map(DClare::getable).filter(Constant.class).filter(c -> ((Constant) c).deriver() != null);
    }

    @Override
    default Setable<Mutable, ?> dContaining() {
        return Mutable.super.dContaining();
    }

    @Override
    default <C> C dAncestor(Class<C> cls) {
        return Mutable.super.dAncestor(cls);
    }

    @Override
    default <T> T dParent(Class<T> cls) {
        return Mutable.super.dParent(cls);
    }

    @Override
    default void dActivate() {
        Mutable.super.dActivate();
    }

    @Override
    default void dDeactivate() {
        Mutable.super.dDeactivate();
    }

    @Override
    default MutableTransaction openTransaction(MutableTransaction parent) {
        return Mutable.super.openTransaction(parent);
    }

    @Override
    default void closeTransaction(Transaction tx) {
        Mutable.super.closeTransaction(tx);
    }

    @Override
    default Mutable resolve(Mutable self) {
        return this;
    }

    @Override
    default MutableTransaction newTransaction(UniverseTransaction universeTransaction) {
        return Mutable.super.newTransaction(universeTransaction);
    }

    @SuppressWarnings("unchecked")
    @Property({constant, hidden})
    default <T extends DObject> DClass<T> dClass() {
        DStructClass<DStruct> dStructClass = dStructClass();
        return (DClass<T>) (DStruct) dStructClass;
    }

    @Property(hidden)
    Map<Object, Set<DProblem>> dProblemsMap();

    @SuppressWarnings("unchecked")
    @Property(hidden)
    default Set<DProblem> dProblems() {
        return Collection.concat(dClass().allValidations().flatMap(p -> (Collection<DProblem>) p.getCollection(this)), //
                dProblemsMap().flatMap(e -> e.getValue())).toSet();
    }

    @Property(hidden)
    default Set<DProblem> dAllProblems() {
        return dProblems().addAll(dChildren().flatMap(DObject::dAllProblems));
    }

    @SuppressWarnings("rawtypes")
    @Property({containment, hidden})
    Set<DRule> dObjectRules();

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
        DClass<DObject> cls = dClass();
        if (cls != null) {
            writer.println(prefix + this + " (" + cls.name() + ") {");
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
            writer.println(prefix + this + " (???) {");
        }
        writer.println(prefix + "}");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({containment, hidden})
    private Set<ScopeChecker> scopeCheckers() {
        return dClass().scopedProperties().map(p -> dclare(ScopeChecker.class, this, p.getKey(), p.getValue())).toSet();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({containment, hidden})
    private Set<MandatoryChecker> mandatoryCheckers() {
        return dClass().mandatoryProperties().map(p -> dclare(MandatoryChecker.class, this, p)).toSet();
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
