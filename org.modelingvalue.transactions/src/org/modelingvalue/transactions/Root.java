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
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.transactions.Leaf.LeafRun;
import org.modelingvalue.transactions.Observer.ObserverRun;
import org.modelingvalue.transactions.ReadOnly.ReadOnlyRun;

public class Root extends Compound {

    public static final int MAX_IN_IN_QUEUE         = Integer.getInteger("MAX_IN_IN_QUEUE", 100);
    public static final int MAX_TOTAL_NR_OF_CHANGES = Integer.getInteger("MAX_TOTAL_NR_OF_CHANGES", 40000);
    public static final int MAX_NR_OF_CHANGES       = Integer.getInteger("MAX_NR_OF_CHANGES", 32);
    public static final int MAX_NR_OF_OBSERVED      = Integer.getInteger("MAX_NR_OF_OBSERVED", 1000);
    public static final int MAX_NR_OF_OBSERVERS     = Integer.getInteger("MAX_NR_OF_OBSERVERS", 4000);
    public static final int MAX_NR_OF_HISTORY       = Integer.getInteger("MAX_NR_OF_HISTORY", 64) + 3;

    public static Root of(Object id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory) {
        return new Root(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, null);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<Root> cycle) {
        return new Root(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, cycle);
    }

    public static Root of(Object id, ContextPool pool) {
        return new Root(id, pool, null, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, null);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue, Consumer<Root> cycle) {
        return new Root(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, cycle);
    }

    public static Root of(Object id, ContextPool pool, int maxInInQueue) {
        return new Root(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory) {
        return new Root(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<Root> cycle) {
        return new Root(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, cycle);
    }

    public static Root of(Object id, ContextPool pool, State start) {
        return new Root(id, pool, start, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, null);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue, Consumer<Root> cycle) {
        return new Root(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, cycle);
    }

    public static Root of(Object id, ContextPool pool, State start, int maxInInQueue) {
        return new Root(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_HISTORY, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, null);
    }

    public static final Setable<Root, Boolean>                             STOPPED       = Setable.of("stopped", false);
    public static final Setable<Root, Set<AbstractLeaf>>                   INTEGRATIONS  = Setable.of("integrations", Set.of());

    protected final Concurrent<TransactionRunsList<Leaf, LeafRun<Leaf>>>   leafRuns;
    protected final Concurrent<TransactionRunsList<Observer, ObserverRun>> observerRuns;
    protected final Concurrent<TransactionRunsList<Compound, CompoundRun>> compoundRuns;
    protected final Concurrent<TransactionRunsList<ReadOnly, ReadOnlyRun>> readOnlyRuns;

    private final Leaf                                                     pre;
    private final Leaf                                                     dummy;
    private final Leaf                                                     stop;
    private final Leaf                                                     backward;
    private final Leaf                                                     forward;
    protected final BlockingQueue<Leaf>                                    inQueue;
    private final BlockingQueue<State>                                     resultQueue;
    private final State                                                    emptyState    = new State(this, null);
    private final int                                                      maxTotalNrOfChanges;
    private final int                                                      maxNrOfChanges;
    private final int                                                      maxNrOfObserved;
    private final int                                                      maxNrOfObservers;

    private List<State>                                                    history       = List.of();
    private List<State>                                                    future        = List.of();
    private State                                                          preState;
    protected ConstantState                                                constantState = new ConstantState();
    protected Leaf                                                         leaf;
    private long                                                           runCount;
    private int                                                            changes;
    private boolean                                                        debug;
    private boolean                                                        killed;
    private Throwable                                                      error;

    protected Root(Object id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<Root> cycle) {
        super(id);
        this.maxTotalNrOfChanges = maxTotalNrOfChanges;
        this.maxNrOfChanges = maxNrOfChanges;
        this.maxNrOfObserved = maxNrOfObserved;
        this.maxNrOfObservers = maxNrOfObservers;
        this.leafRuns = Concurrent.of(() -> new TransactionRunsList<>(() -> new LeafRun<Leaf>()));
        this.observerRuns = Concurrent.of(() -> new TransactionRunsList<>(() -> new ObserverRun()));
        this.compoundRuns = Concurrent.of(() -> new TransactionRunsList<>(() -> new CompoundRun()));
        this.readOnlyRuns = Concurrent.of(() -> new TransactionRunsList<>(() -> new ReadOnlyRun()));
        this.inQueue = new LinkedBlockingQueue<>(maxInInQueue);
        this.resultQueue = new LinkedBlockingQueue<>(1);
        this.stop = Leaf.of(Action.of("stop", o -> STOPPED.set(this, true)), this);
        this.dummy = Leaf.of(Action.of("dummy", o -> {
        }), this);
        this.backward = Leaf.of(Action.of("backward", o -> {
        }), this);
        this.forward = Leaf.of(Action.of("forward", o -> {
        }), this);
        this.pre = cycle != null ? Leaf.of(Action.of("cycle", o -> cycle.accept(this)), this) : null;
        pool.execute(() -> {
            State state = start != null ? start.clone(this) : emptyState;
            while (!killed) {
                TraceTimer.traceBegin("root");
                try {
                    changes = 0;
                    debug = false;
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
                        state = post(run(trigger(pre(state), leaf, leaf.initDirection())));
                    }
                    if (!killed) {
                        state = run(trigger(state, state.get(Root.this, INTEGRATIONS), Direction.forward));
                    }
                    if (!killed && inQueue.isEmpty()) {
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
            constantState.stop();
            putResult(state);
        });
    }

    @Override
    public Root root() {
        return this;
    }

    protected State run(State state) {
        return run(state, this);
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
        put(Leaf.of(Action.of(id, o -> action.run()), this));
    }

    public void put(Object id, Runnable action, Priority priority) {
        put(Leaf.of(Action.of(id, o -> action.run()), this, priority));
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
                throw new Error("Error in engine " + state.get(() -> error.getMessage()), error);
            }
            return state;
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public void addIntegration(String id, TriConsumer<State, State, Boolean> diffHandler) {
        Leaf.getCurrent().set(Root.this, INTEGRATIONS, Set::add, Leaf.of(Action.of(id, o -> {
            diffHandler.accept(preState(), Leaf.getCurrent().state(), true);
        }), Root.this, Priority.postDepth));
    }

    public Imperative addIntegration(String id, TriConsumer<State, State, Boolean> diffHandler, Consumer<Runnable> scheduler) {
        Imperative n = Imperative.of(id, preState, this, scheduler, diffHandler);
        Leaf.getCurrent().set(Root.this, INTEGRATIONS, Set::add, Leaf.of(Action.of(n, o -> {
            State pre = Leaf.getCurrent().state();
            boolean timeTraveling = isTimeTraveling();
            n.schedule(() -> n.commit(pre, timeTraveling));
        }), Root.this, Priority.postDepth));
        return n;
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

    public int maxTotalNrOfChanges() {
        return maxTotalNrOfChanges;
    }

    public int maxNrOfChanges() {
        return maxNrOfChanges;
    }

    public int maxNrOfObserved() {
        return maxNrOfObserved;
    }

    public int maxNrOfObservers() {
        return maxNrOfObservers;
    }

    public boolean isDebugging() {
        return debug;
    }

    public void setDebugging() {
        debug = true;
    }

    public boolean isKilled() {
        return killed;
    }

    public void kill() {
        killed = true;
        put(dummy);
    }

    public long runCount() {
        return runCount;
    }

    public int countTotalChanges() {
        if (changes > maxTotalNrOfChanges) {
            synchronized (this) {
                return changes++;
            }
        } else {
            return changes++;
        }
    }

    public int totalChanges() {
        if (error != null) {
            throw new Error(error);
        }
        return changes;
    }

    public void startPriority(Priority prio) {
    }

    public void endPriority(Priority prio) {
    }

    public void startOpposite() {
    }

}
