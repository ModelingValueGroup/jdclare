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

package org.modelingvalue.dclare;

import java.util.ConcurrentModificationException;
import java.util.Objects;
import java.util.function.BiFunction;

import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.NotMergeableException;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TraceTimer;

public class ActionTransaction extends LeafTransaction implements StateMergeHandler {

    private final Setted setted = new Setted();
    private State        preState;

    protected ActionTransaction(UniverseTransaction universeTransaction) {
        super(universeTransaction);
    }

    public final Action<?> action() {
        return (Action<?>) cls();
    }

    @SuppressWarnings("unchecked")
    protected void run(State pre, UniverseTransaction universeTransaction) {
        ((Action<Mutable>) action()).run(parent().mutable());
    }

    @Override
    protected State run(State state) {
        TraceTimer.traceBegin(traceId());
        init(state);
        try {
            CURRENT.run(this, () -> run(state, universeTransaction()));
            return result();
        } catch (Throwable t) {
            universeTransaction().handleException(new TransactionException(parent().mutable(), new TransactionException(action(), t)));
            return state;
        } finally {
            clear();
            TraceTimer.traceEnd(traceId());
        }
    }

    protected String traceId() {
        return "leaf";
    }

    @Override
    public State state() {
        if (preState == null) {
            throw new ConcurrentModificationException();
        }
        return preState;
    }

    protected void init(State state) {
        if (preState != null) {
            throw new ConcurrentModificationException();
        }
        preState = state;
        setted.init(state);
    }

    protected void clear() {
        setted.clear();
        preState = null;
    }

    protected State result() {
        State result = setted.result();
        preState = null;
        return result;
    }

    @Override
    public <O, T> T current(O object, Getable<O, T> property) {
        return setted.get().get(object, property);
    }

    @Override
    public <O, T, E> T set(O object, Setable<O, T> property, BiFunction<T, E, T> function, E element) {
        return set(object, property, function.apply(setted.get().get(object, property), element));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <O, T> T set(O object, Setable<O, T> property, T post) {
        T pre = state().get(object, property);
        T[] oldNew = (T[]) new Object[2];
        if (setted.change(s -> s.set(object, property, (br, po) -> {
            if (Objects.equals(br, po)) {
                po = br;
            } else if (!Objects.equals(br, pre)) {
                if (pre instanceof Mergeable) {
                    po = (T) ((Mergeable) pre).merge(br, po);
                } else if (br != null && po != null) {
                    handleMergeConflict(object, property, pre, br, po);
                }
            }
            return po;
        }, post, oldNew))) {
            changed(object, property, oldNew[0], oldNew[1]);
        }
        return pre;
    }

    @Override
    public <O> void clear(O object) {
        super.clear(object);
        setted.change(s -> s.set(object, State.EMPTY_SETABLES_MAP));
    }

    private final class Setted extends Concurrent<State> {
        @Override
        protected State merge(State base, State[] branches, int length) {
            return base.merge(ActionTransaction.this, branches, length);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handleMergeConflict(Object object, Setable property, Object pre, Object... branches) {
        throw new NotMergeableException(object + "." + property + "= " + pre + " -> " + StringUtil.toString(branches));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void handleChange(Object o, DefaultMap<Setable, Object> ps, Entry<Setable, Object> p, DefaultMap<Setable, Object>[] psbs) {
    }

    @Override
    protected Mutable dParent(Mutable object) {
        return setted.get().getA(object, Mutable.D_PARENT_CONTAINING);
    }

    @Override
    public ActionInstance actionInstance() {
        return ActionInstance.of(parent().mutable(), action());
    }

}
