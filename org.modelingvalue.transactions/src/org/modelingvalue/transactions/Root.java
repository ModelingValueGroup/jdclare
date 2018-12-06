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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.ContextThread;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.StringUtil;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.Triple;

public class Root extends Compound {

    public static final ContextPool POOL                    = ContextThread.thePool();

    public static final int         MAX_IN_IN_QUEUE         = Integer.getInteger("MAX_IN_IN_QUEUE", 100);
    public static final int         MAX_TOTAL_NR_OF_CHANGES = Integer.getInteger("MAX_TOTAL_NR_OF_CHANGES", 128000);
    public static final int         MAX_NR_OF_CHANGES       = Integer.getInteger("MAX_NR_OF_CHANGES", 32);
    public static final int         MAX_NR_OF_HISTORY       = Integer.getInteger("MAX_NR_OF_HISTORY", 64) + 3;

    public static Root of(Object id, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory) {
        return new Root(id, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, null);
    }

    public static Root of(Object id, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory, Consumer<Root> cycle) {
        return new Root(id, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, cycle);
    }

    public static Root of(Object id) {
        return new Root(id, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static Root of(Object id, int maxInInQueue, Consumer<Root> cycle) {
        return new Root(id, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, cycle);
    }

    public static Root of(Object id, int maxInInQueue) {
        return new Root(id, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static final Setable<Root, Boolean>                                                                           STOPPED       = Setable.of("stopped", false);
    public static final Setable<Root, Set<AbstractLeaf>>                                                                 INTEGRATIONS  = Setable.of("integrations", Set.of());

    private final Leaf                                                                                                   pre;
    private final Leaf                                                                                                   dummy;
    private final Leaf                                                                                                   stop;
    private final Leaf                                                                                                   backward;
    private final Leaf                                                                                                   forward;
    private final BlockingQueue<Leaf>                                                                                    inQueue;
    private final BlockingQueue<State>                                                                                   resultQueue;

    private final State                                                                                                  emptyState    = new State(this, null);
    private List<State>                                                                                                  history       = List.of();
    private List<State>                                                                                                  future        = List.of();
    private State                                                                                                        preState;
    protected ConstantState                                                                                              constantState = new ConstantState();
    protected ConcurrentMap<Pair<Observer, Integer>, Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>>> tooManyChanges;
    protected Leaf                                                                                                       leaf;
    private long                                                                                                         count;
    private int                                                                                                          changes;
    private Throwable                                                                                                    error;
    final int                                                                                                            maxTotalNrOfChanges;
    final int                                                                                                            maxNrOfChanges;

    protected Root(Object id, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory, Consumer<Root> cycle) {
        super(id);
        this.maxTotalNrOfChanges = maxTotalNrOfChanges;
        this.maxNrOfChanges = maxNrOfChanges;
        inQueue = new LinkedBlockingQueue<>(maxInInQueue);
        resultQueue = new LinkedBlockingQueue<>(1);
        stop = Leaf.of("stop", this, () -> STOPPED.set(this, true));
        dummy = Leaf.of("dummy", this, () -> {
        });
        backward = Leaf.of("backward", this, () -> {
        });
        forward = Leaf.of("forward", this, () -> {
        });
        pre = cycle != null ? Leaf.of("cycle", this, () -> cycle.accept(this)) : null;
        POOL.execute(() -> {
            State state = emptyState;
            while (true) {
                TraceTimer.traceBegin("root");
                try {
                    changes = 0;
                    count++;
                    preState = state;
                    leaf = take();
                    if (leaf == backward) {
                        if (history.size() > 3) {
                            future = future.prepend(state);
                            state = history.last();
                            history = history.removeLast();
                        }
                    } else if (leaf == forward) {
                        if (!future.isEmpty()) {
                            history = history.append(state);
                            state = future.first();
                            future = future.removeFirst();
                        }
                    } else if (leaf != dummy) {
                        history = history.append(state);
                        future = List.of();
                        if (history.size() > maxNrOfHistory) {
                            history = history.removeFirst();
                        }
                        try {
                            state = post(apply(schedule(pre(state), leaf, Priority.high)));
                        } catch (TooManyChangesException tmce) {
                            count++;
                            tooManyChanges = new ConcurrentHashMap<>();
                            post(apply(schedule(pre(state), leaf, Priority.high)));
                            throw tmce;
                        }
                    }
                    state = apply(schedule(state, state.get(Root.this, INTEGRATIONS), Priority.high));
                    if (inQueue.isEmpty()) {
                        if (isStopped(state)) {
                            break;
                        } else if (pre != null) {
                            put(pre);
                        }
                    }
                } catch (Throwable t) {
                    error = t;
                    break;
                } finally {
                    TraceTimer.traceEnd("root");
                }
            }
            history = history.append(state);
            reportTooLong(state);
            putResult(state);
        });
    }

    public State emptyState() {
        return emptyState;
    }

    protected State pre(State state) {
        return state;
    }

    protected State post(State state) {
        return state;
    }

    public boolean isStopped(State state) {
        return tooManyChanges() || state.get(this, STOPPED);
    }

    public void put(Object id, Runnable action) {
        put(Leaf.of(id, this, action));
    }

    protected void put(Leaf action) {
        try {
            inQueue.put(action);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private Leaf take() {
        try {
            return inQueue.take();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    private void putResult(State state) {
        try {
            resultQueue.put(state);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public State waitForEnd() {
        try {
            State state = resultQueue.take();
            resultQueue.put(state);
            if (error != null) {
                throw new Error("Error in engine ", error);
            }
            return state;
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public void addIntegration(String id, TriConsumer<State, State, Boolean> diffHandler) {
        Leaf.getCurrent().set(Root.this, INTEGRATIONS, Set::add, Leaf.of(id, Root.this, () -> {
            diffHandler.accept(preState(), Leaf.getCurrent().state(), true);
        }));
    }

    public Imperative addIntegration(String id, TriConsumer<State, State, Boolean> diffHandler, Consumer<Runnable> scheduler) {
        Imperative n = Imperative.of(id, emptyState, this, scheduler, diffHandler);
        Leaf.getCurrent().set(Root.this, INTEGRATIONS, Set::add, Leaf.of(n, Root.this, () -> {
            State pre = Leaf.getCurrent().state();
            boolean timeTraveling = isTimeTraveling();
            n.schedule(() -> n.commit(pre, timeTraveling));
        }));
        return n;
    }

    @Override
    public Root root() {
        return this;
    }

    public void backward() {
        put(backward);
    }

    public void stop() {
        put(stop);
    }

    public void forward() {
        put(forward);
    }

    public void dummy() {
        put(dummy);
    }

    protected State preState() {
        return preState;
    }

    protected boolean isTimeTraveling() {
        return leaf == backward || leaf == forward;
    }

    public long count() {
        if (changes++ > maxTotalNrOfChanges) {
            throw new TooManyChangesException("Total changes: " + changes + ", running: " + Leaf.getCurrent());
        }
        return count;
    }

    public boolean tooManyChanges() {
        return tooManyChanges != null;
    }

    private void reportTooLong(State state) {
        if (tooManyChanges != null) {
            state.run(() -> {
                System.err.println("------------ MORE THEN " + maxNrOfChanges + " CHANGING RUNS OF 1 RULE IN 1 ROOT TRANSACTION -----------------------");
                Map<Pair<Observer, Integer>, Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>>> map = //
                        Collection.of(tooManyChanges.entrySet()).toMap(e -> Entry.of(e.getKey(), e.getValue()));
                boolean[] isCause = new boolean[1];
                boolean found = false;
                for (int i = 0; !found && i < maxNrOfChanges; i++) {
                    for (Pair<Observer, Integer> run : map.toKeys()) {
                        if (run.b() == i) {
                            isCause[0] = false;
                            map = filterCauses(run, map, isCause);
                            if (isCause[0]) {
                                found = true;
                                print("", run, null, map);
                            }
                        }
                    }
                }
                System.err.println("-----------------------------------------------------------------------------------------------");
            });
        }
    }

    private Map<Pair<Observer, Integer>, Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>>> filterCauses(Pair<Observer, Integer> run, //
            Map<Pair<Observer, Integer>, Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>>> map, boolean[] isCause) {
        Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>> writes = map.get(run);
        map = map.removeKey(run);
        boolean found = run.a().changes() > maxNrOfChanges;
        if (writes != null) {
            for (Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>> write : writes) {
                isCause[0] = false;
                map = filterCauses(write.a(), map, isCause);
                if (isCause[0]) {
                    found = true;
                } else {
                    writes = writes.remove(write);
                }
            }
        }
        if (found) {
            isCause[0] = true;
            return map.put(run, writes);
        } else {
            return map;
        }
    }

    private void print(String prefix, Pair<Observer, Integer> run, Triple<Object, Object, Object> trigger, //
            Map<Pair<Observer, Integer>, Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>>> map) {
        if (trigger != null) {
            System.err.println(prefix + StringUtil.toString(trigger.a()) + "." + StringUtil.toString(trigger.b()) + "=" + StringUtil.toString(trigger.c()));
        }
        Set<Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>>> writes = map.get(run);
        if (writes != null || !prefix.isEmpty()) {
            System.err.println(prefix + "-> " + StringUtil.toString(run.a()).substring(9) + " (" + (run.b() + 1) + ")");
        }
        if (writes != null) {
            map = map.removeKey(run);
            for (Pair<Pair<Observer, Integer>, Triple<Object, Object, Object>> write : writes) {
                print(prefix + "    ", write.a(), write.b(), map);
            }
        }
    }

}
