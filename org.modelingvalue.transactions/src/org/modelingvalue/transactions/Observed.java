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

package org.modelingvalue.transactions;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadConsumer;

public class Observed<O, T> extends Setable<O, T> {

    public static <C, V> Observed<C, V> of(Object id, V def) {
        return of(id, def, null);
    }

    public static <C, V> Observed<C, V> of(Object id, V def, QuadConsumer<AbstractLeaf, C, V, V> changed) {
        return new Observed<C, V>(id, def, changed);
    }

    private final Observers<O, T>[] observers;

    @SuppressWarnings("unchecked")
    protected Observed(Object id, T def, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        this(id, def, new Observers[]{
                Observers.of(Pair.of(id, Priority.first), Priority.first), //
                Observers.of(Pair.of(id, Priority.high), Priority.high), //
                Observers.of(Pair.of(id, Priority.low), Priority.low)}, changed);
    }

    private Observed(Object id, T def, Observers<O, T>[] observers, QuadConsumer<AbstractLeaf, O, T, T> changed) {
        super(id, def, ($, o, p, n) -> {
            if (changed != null) {
                changed.accept($, o, p, n);
            }
            for (Observers<O, T> observ : observers) {
                Set<Observer> triggered = $.get(o, observ);
                if (!triggered.isEmpty()) {
                    $.trigger(triggered, observ.prio(), o, id, n);
                }
            }
        });
        this.observers = observers;
        for (Observers<O, T> observ : observers) {
            observ.observed = this;
        }
    }

    public Observers<O, T> observers(Priority prio) {
        return observers[prio.nr];
    }

    public Observers<O, T>[] observers() {
        return observers;
    }

    public static final class Observers<O, T> extends Setable<O, Set<Observer>> {

        private Observed<O, T> observed;
        private final Priority prio;

        public static <C, V> Observers<C, V> of(Object id, Priority prio) {
            return new Observers<C, V>(id, prio);
        }

        private Observers(Object id, Priority prio) {
            super(id, Set.of(), null);
            this.prio = prio;
        }

        public Observed<O, T> observed() {
            return observed;
        }

        public Priority prio() {
            return prio;
        }

    }

}
