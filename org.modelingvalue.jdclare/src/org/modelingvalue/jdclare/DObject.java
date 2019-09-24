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

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.NonLockingPrintWriter;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.jdclare.meta.DClass;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.meta.DRule;
import org.modelingvalue.jdclare.meta.DStructClass;
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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    default Collection<? extends Observer<?>> dMutableObservers() {
        return (Collection) dObjectRules().map(DRule::observer);
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

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Property({constant, hidden})
    default DClass<DObject> dClass() {
        DStructClass<DStruct> dStructClass = dStructClass();
        return (DClass) dStructClass;
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

}
