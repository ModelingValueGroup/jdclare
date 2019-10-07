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
import java.util.function.Function;

import org.modelingvalue.collections.DefaultMap;
import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Concurrent;
import org.modelingvalue.collections.util.ContextThread.ContextPool;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TraceTimer;
import org.modelingvalue.collections.util.TriConsumer;

public class UniverseTransaction extends MutableTransaction {

    public static final int MAX_IN_IN_QUEUE         = Integer.getInteger("MAX_IN_IN_QUEUE", 100);
    public static final int MAX_TOTAL_NR_OF_CHANGES = Integer.getInteger("MAX_TOTAL_NR_OF_CHANGES", 10000);
    public static final int MAX_NR_OF_CHANGES       = Integer.getInteger("MAX_NR_OF_CHANGES", 200);
    public static final int MAX_NR_OF_OBSERVED      = Integer.getInteger("MAX_NR_OF_OBSERVED", 1000);
    public static final int MAX_NR_OF_OBSERVERS     = Integer.getInteger("MAX_NR_OF_OBSERVERS", 1000);
    public static final int MAX_NR_OF_HISTORY       = Integer.getInteger("MAX_NR_OF_HISTORY", 64) + 3;

    public static UniverseTransaction of(Universe id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory) {
        return new UniverseTransaction(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, null);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<UniverseTransaction> cycle) {
        return new UniverseTransaction(id, pool, null, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, cycle);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool) {
        return new UniverseTransaction(id, pool, null, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, null);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, int maxInInQueue, Consumer<UniverseTransaction> cycle) {
        return new UniverseTransaction(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, cycle);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, int maxInInQueue) {
        return new UniverseTransaction(id, pool, null, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, null);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory) {
        return new UniverseTransaction(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, null);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<UniverseTransaction> cycle) {
        return new UniverseTransaction(id, pool, start, maxInInQueue, maxTotalNrOfChanges, maxNrOfChanges, maxNrOfObserved, maxNrOfObservers, maxNrOfHistory, cycle);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, State start) {
        return new UniverseTransaction(id, pool, start, MAX_IN_IN_QUEUE, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, null);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, State start, int maxInInQueue, Consumer<UniverseTransaction> cycle) {
        return new UniverseTransaction(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, cycle);
    }

    public static UniverseTransaction of(Universe id, ContextPool pool, State start, int maxInInQueue) {
        return new UniverseTransaction(id, pool, start, maxInInQueue, MAX_TOTAL_NR_OF_CHANGES, MAX_NR_OF_CHANGES, MAX_NR_OF_OBSERVED, MAX_NR_OF_OBSERVERS, MAX_NR_OF_HISTORY, null);
    }

    public static final Setable<Universe, Boolean>                                    STOPPED       = Setable.of("stopped", false);
    public static final Setable<Universe, Set<Action<Universe>>>                      POST_ACTIONS  = Setable.of("postActions", Set.of());

    protected final Concurrent<ReusableTransaction<Action<?>, ActionTransaction>>     actionTransactions;
    protected final Concurrent<ReusableTransaction<Observer<?>, ObserverTransaction>> observerTransactions;
    protected final Concurrent<ReusableTransaction<Mutable, MutableTransaction>>      mutableTransactions;
    protected final Concurrent<ReusableTransaction<ReadOnly, ReadOnlyTransaction>>    readOnlys;

    private final Action<Universe>                                                    cycle;
    private final Action<Universe>                                                    dummy;
    private final Action<Universe>                                                    stop;
    private final Action<Universe>                                                    backward;
    private final Action<Universe>                                                    forward;
    private final Action<Universe>                                                    clearOrphans;
    protected final BlockingQueue<Action<Universe>>                                   inQueue;
    private final BlockingQueue<State>                                                resultQueue;
    private final State                                                               emptyState    = new State(this, State.EMPTY_OBJECTS_MAP);
    private final int                                                                 maxTotalNrOfChanges;
    private final int                                                                 maxNrOfChanges;
    private final int                                                                 maxNrOfObserved;
    private final int                                                                 maxNrOfObservers;
    protected final ReadOnly                                                          runOnState    = new ReadOnly(this, Direction.forward, Priority.postDepth);

    private List<State>                                                               history       = List.of();
    private List<State>                                                               future        = List.of();
    private State                                                                     preState;
    private State                                                                     state;
    protected ConstantState                                                           constantState = new ConstantState();
    protected Action<Universe>                                                        leaf;
    private long                                                                      runCount;
    private int                                                                       changes;
    private boolean                                                                   debug;
    private boolean                                                                   killed;
    private Throwable                                                                 error;

    protected UniverseTransaction(Universe universe, ContextPool pool, State start, int maxInInQueue, int maxTotalNrOfChanges, int maxNrOfChanges, int maxNrOfObserved, int maxNrOfObservers, int maxNrOfHistory, Consumer<UniverseTransaction> cycle) {
        super(null);
        this.maxTotalNrOfChanges = maxTotalNrOfChanges;
        this.maxNrOfChanges = maxNrOfChanges;
        this.maxNrOfObserved = maxNrOfObserved;
        this.maxNrOfObservers = maxNrOfObservers;
        this.actionTransactions = Concurrent.of(() -> new ReusableTransaction<>(this));
        this.observerTransactions = Concurrent.of(() -> new ReusableTransaction<>(this));
        this.mutableTransactions = Concurrent.of(() -> new ReusableTransaction<>(this));
        this.readOnlys = Concurrent.of(() -> new ReusableTransaction<>(this));
        this.inQueue = new LinkedBlockingQueue<>(maxInInQueue);
        this.resultQueue = new LinkedBlockingQueue<>(1);
        this.stop = Action.of("$stop", o -> STOPPED.set(universe(), true));
        this.dummy = Action.of("$dummy", o -> {
        });
        this.backward = Action.of("$backward", o -> {
        });
        this.forward = Action.of("$forward", o -> {
        });
        this.cycle = cycle != null ? Action.of("$cycle", o -> cycle.accept(this)) : null;
        this.clearOrphans = Action.of("$clearOrphans", o -> clearOrphans(o));
        start(universe, null);
        pool.execute(() -> {
            state = start != null ? start.clone(this) : emptyState;
            while (!killed) {
                TraceTimer.traceBegin("root");
                try {
                    changes = 0;
                    debug = false;
                    runCount++;
                    preState = state;
                    leaf = take();
                    start(leaf);
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
                        state = state.get(() -> post(run(trigger(pre(state), universe(), leaf, leaf.initDirection()))));
                        if (isDebugging()) {
                            handleTooManyChanges(state);
                        }
                    }
                    if (!killed) {
                        state = state.get(() -> run(trigger(state, state.get(universe(), POST_ACTIONS), Direction.forward)));
                    }
                    if (!killed && inQueue.isEmpty()) {
                        if (isStopped(state)) {
                            break;
                        } else if (this.cycle != null) {
                            put(this.cycle);
                        }
                    }
                } catch (Throwable t) {
                    if (t instanceof TransactionException) {
                        state = ((TransactionException) t).getState();
                    }
                    state = handleException(state, t);
                } finally {
                    end(leaf);
                    TraceTimer.traceEnd("root");
                }
            }
            stop();
            history = history.append(state);
            constantState.stop();
            end(state);
        });
        init();
    }

    protected State handleException(State state, Throwable t) {
        error = t;
        kill();
        return state;
    }

    protected void init() {
        put("$init", () -> {
            addDiffHandler("$checkConsistency", (pre, post, last) -> {
                if (last) {
                    checkConsistency(pre, post);
                }
            });
            universe().init();
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void checkConsistency(State pre, State post) {
        pre.diff(post, o -> o instanceof Mutable).forEach(e0 -> {
            if (e0.getKey() instanceof Universe || e0.getValue().b().get(Mutable.D_PARENT) != null) {
                ((Mutable) e0.getKey()).dClass().dSetables().filter(Setable::checkConsistency).forEach(s -> {
                    ((Setable) s).checkConsistency(post, e0.getKey(), e0.getValue().a().get(s), e0.getValue().b().get(s));
                });
            } else {
                checkOrphanState(e0);
            }
        });
    }

    @SuppressWarnings("rawtypes")
    protected void checkOrphanState(Entry<Object, Pair<DefaultMap<Setable, Object>, DefaultMap<Setable, Object>>> e0) {
        if (!e0.getValue().b().isEmpty()) {
            throw new Error("Orphan '" + e0.getKey() + "' has state '" + e0.getValue().b() + "'");
        }
    }

    public Universe universe() {
        return (Universe) mutable();
    }

    protected <O extends Mutable> State trigger(State state, Set<Action<Universe>> actions, Direction direction) {
        for (Action<Universe> action : actions) {
            state = trigger(state, universe(), action, direction);
        }
        return state;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    protected void handleTooManyChanges(State state) {
        ObserverTrace trace = state.filter(o -> o instanceof Mutable, s -> s.id instanceof Pair && ((Pair) s.id).a() instanceof Observer && ((Pair) s.id).b().equals("TRACES")).//
                flatMap(e1 -> e1.getValue().map(e2 -> ((Set<ObserverTrace>) e2.getValue()).sorted().findFirst().orElse(null))).//
                sorted((a, b) -> Integer.compare(b.done().size(), a.done().size())).findFirst().orElse(null);
        throw new TooManyChangesException(state, trace, trace.done().size());
    }

    @Override
    public UniverseTransaction universeTransaction() {
        return this;
    }

    public State emptyState() {
        return emptyState;
    }

    protected State pre(State state) {
        return state;
    }

    @SuppressWarnings("rawtypes")
    protected void clearOrphans(Universe universe) {
        LeafTransaction tx = LeafTransaction.getCurrent();
        State st = tx.state();
        Map<Object, Map<Setable, Pair<Object, Object>>> changed = //
                preState().diff(st, o -> o instanceof Mutable && !(o instanceof Universe) && st.get((Mutable) o, Mutable.D_PARENT) == null, s -> true).toMap(Function.identity());
        changed.forEach(e0 -> {
            clear(tx, (Mutable) e0.getKey());
        });
        changed.forEach(e0 -> {
            clear(tx, (Mutable) e0.getKey());
        });
    }

    protected void clear(LeafTransaction tx, Mutable orphan) {
        tx.clear(orphan);
        for (Mutable child : orphan.dChildren()) {
            clear(tx, child);
        }
    }

    protected State post(State pre) {
        return run(trigger(pre, universe(), clearOrphans, Direction.backward));
    }

    public boolean isStopped(State state) {
        return state.get(universe(), STOPPED);
    }

    public void put(Object id, Runnable action) {
        put(Action.of(id, o -> action.run()));
    }

    public void put(Object id, Runnable action, Priority priority) {
        put(Action.of(id, o -> action.run(), priority));
    }

    protected void put(Action<Universe> action) {
        if (!killed) {
            try {
                inQueue.put(action);
            } catch (InterruptedException e) {
                throw new Error(e);
            }
        }
    }

    private Action<Universe> take() {
        try {
            return inQueue.take();
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    protected void end(State state) {
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

    public void addDiffHandler(String id, TriConsumer<State, State, Boolean> diffHandler) {
        ActionTransaction.getCurrent().set(universe(), POST_ACTIONS, Set::add, Action.<Universe> of(id, o -> {
            LeafTransaction tx = ActionTransaction.getCurrent();
            diffHandler.accept(tx.universeTransaction().preState(), tx.state(), true);
        }, Priority.postDepth));
    }

    public ImperativeTransaction addImperative(String id, TriConsumer<State, State, Boolean> diffHandler, Consumer<Runnable> scheduler) {
        ImperativeTransaction n = ImperativeTransaction.of(Imperative.of(id), preState, this, scheduler, diffHandler);
        ActionTransaction.getCurrent().set(universe(), POST_ACTIONS, Set::add, Action.<Universe> of(id, o -> {
            LeafTransaction tx = ActionTransaction.getCurrent();
            State pre = tx.state();
            boolean timeTraveling = tx.universeTransaction().isTimeTraveling();
            n.schedule(() -> n.commit(pre, timeTraveling));
        }, Priority.postDepth));
        return n;
    }

    public void backward() {
        put(backward);
    }

    @Override
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
        try {
            inQueue.put(dummy);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
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

    public void start(Action<Universe> action) {
    }

    public void end(Action<Universe> action) {
    }

    public void startPriority(Priority prio) {
    }

    public void endPriority(Priority prio) {
    }

    public void startOpposite() {
    }

}
