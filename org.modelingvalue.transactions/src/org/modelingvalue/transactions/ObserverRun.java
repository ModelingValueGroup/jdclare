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

import org.modelingvalue.collections.Entry;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;

public class ObserverRun implements Comparable<ObserverRun> {

    private final Observer                    observer;
    private final int                         nrOfChanges;
    private final ObserverRun                 previous;
    private final Map<Slot, Object>           read;
    private final Map<Slot, Object>           written;
    private final Set<ObserverRun>            done;
    private final Map<Slot, Set<ObserverRun>> backTrace;

    protected ObserverRun(Observer observer, ObserverRun previous, int nrOfChanges, Map<Slot, Object> read, Map<Slot, Object> written) {
        this.observer = observer;
        this.nrOfChanges = nrOfChanges;
        this.previous = previous;
        this.read = read;
        this.written = written;
        for (Entry<Slot, Object> e : read) {
            e.getKey().observed().readers().set(e.getKey().object(), Set::add, this);
        }
        for (Entry<Slot, Object> e : written) {
            e.getKey().observed().writers().set(e.getKey().object(), Set::add, this);
        }
        Set<ObserverRun> done = previous != null ? previous.done : Set.of();
        Map<Slot, Set<ObserverRun>> backTrace = read.toMap(e -> {
            Slot slot = e.getKey();
            Set<ObserverRun> writers = slot.observed().writers().get(slot.object());
            return Entry.of(slot, writers.removeAll(done).remove(this));
        });
        Set<ObserverRun> back = backTrace.flatMap(Entry::getValue).toSet();
        Set<ObserverRun> backDone = back.flatMap(ObserverRun::done).toSet();
        this.backTrace = backTrace.toMap(e -> Entry.of(e.getKey(), e.getValue().removeAll(backDone)));
        this.done = done.addAll(back).addAll(backDone).addAll(previous != null ? previous.done.add(previous) : Set.of());
    }

    public Observer observer() {
        return observer;
    }

    public int nrOfChanges() {
        return nrOfChanges;
    }

    public Set<ObserverRun> done() {
        return done;
    }

    public Map<Slot, Object> read() {
        return read;
    }

    public Map<Slot, Object> written() {
        return written;
    }

    public ObserverRun previous() {
        return previous;
    }

    public Map<Slot, Set<ObserverRun>> backTrace() {
        return backTrace;
    }

    @Override
    public String toString() {
        return observer + "#" + nrOfChanges;
    }

    @Override
    public int compareTo(ObserverRun o) {
        return Integer.compare(o.nrOfChanges, nrOfChanges);
    }

    @SuppressWarnings("unchecked")
    public String trace(String prefix, String message, int length) {
        return trace(prefix, message, new Set[]{Set.of()}, length);
    }

    private String trace(String prefix, String message, Set<ObserverRun>[] done, int length) {
        message += prefix + "run: " + observer() + " nr: " + nrOfChanges;
        if (done[0].size() < length && !done[0].contains(this)) {
            done[0] = done[0].add(this);
            for (Entry<Slot, Set<ObserverRun>> e : backTrace()) {
                if (!e.getValue().isEmpty()) {
                    message += prefix + "read: " + e.getKey().object() + "." + e.getKey().observed() + "=" + read().get(e.getKey());
                    for (ObserverRun writer : e.getValue()) {
                        message += prefix + "  write: " + e.getKey().object() + "." + e.getKey().observed() + "=" + writer.written().get(e.getKey());
                        message = writer.trace(prefix + "  ", message, done, length);
                    }
                }
            }
        }
        return message;
    }

}
