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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.collections.util.TriConsumer;

public class Root extends Compound {

    public static final int MAX_IN_IN_QUEUE         = Integer.getInteger("MAX_IN_IN_QUEUE", 100);
    public static final int MAX_TOTAL_NR_OF_CHANGES = Integer.getInteger("MAX_TOTAL_NR_OF_CHANGES", 128000);
    public static final int MAX_NR_OF_CHANGES       = Integer.getInteger("MAX_NR_OF_CHANGES", 32);
    public static final int MAX_NR_OF_HISTORY       = Integer.getInteger("MAX_NR_OF_HISTORY", 64) + 3;

    public static Root of(Object id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory) {
        return new Root(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, null);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory, Consumer<Root> cycle) {
        return new Root(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, cycle);
    }

    public static Root of(Object id, ContextPool pool) {
        return new Root(id, pool, null, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue, Consumer<Root> cycle) {
        return new Root(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, cycle);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue) {
        return new Root(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory) {
        return new Root(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory, Consumer<Root> cycle) {
        return new Root(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfHistory, cycle);
    }

    public static Root of(Object id, ContextPool pool, State start) {
        return new Root(id, pool, start, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, Consumer<Root> cycle) {
        return new Root(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, cycle);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue) {
        return new Root(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, null);
    }

    public static final Setable<Root, Boolean>           STOPPED       = Setable.of("stopped", false);
    public static final Setable<Root, Set<AbstractLeaf>> INTEGRATIONS  = Setable.of("integrations", Set.of());

    private final Leaf                                   pre;
    private final Leaf                                   dummy;
    private final Leaf                                   stop;
    private final Leaf                                   backward;
    private final Leaf                                   forward;
    protected final BlockingQueue<Leaf>                  inQueue;
    private final BlockingQueue<State>                   resultQueue;

    private final State                                  emptyState    = new State(this, null);
    private List<State>                                  history       = List.of();
    private List<State>                                  future        = List.of();
    private State                                        preState;
    protected ConstantState                              constantState = new ConstantState();
    protected Leaf                                       leaf;
    private long                                         runCount;
    private int                                          changes;
    private Throwable                                    error;
    final int                                            maxTotalNrOfChanges;
    final int                                            maxNrOfChanges;

    protected Root(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfHistory, Consumer<Root> cycle) {
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
        pool.execute(() -> {
            State state = start != null ? new State(this, start.map) : emptyState;
            while (true) {
                TraceTimer.traceBegin("root");
                try {
                    changes = 0;
                    runCount++;
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
                        state = post(apply(schedule(pre(state), leaf, Priority.high)));
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
        return state.get(this, STOPPED);
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
        Imperative n = Imperative.of(id, preState, this, scheduler, diffHandler);
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

    public State preState() {
        return preState;
    }

    protected boolean isTimeTraveling() {
        return leaf == backward || leaf == forward;
    }

    public long runCount() {
        return runCount;
    }

    public int countTotalChanges() {
        return changes++;
    }

    public int totalChanges() {
        if (error != null) {
            throw new Error(error);
        }
        return changes;
    }

}
