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

package org.modelingvalue.collections.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

public final class TraceTimer {
    private static final String                              REST                     = "<REST>";
    private static final int                                 MIL                      = 1000000;
    private static final boolean                             TRACE_TIME               = Boolean.parseBoolean(System.getProperties().getProperty("TRACE_TIME", "false"));
    private static final boolean                             TRACE_TIME_STEP          = Boolean.parseBoolean(System.getProperties().getProperty("TRACE_TIME_STEP", "false"));
    private static final int                                 TRACE_TIME_DUMP_INTERVAL = Integer.parseInt(System.getProperties().getProperty("TRACE_TIME_DUMP_INTERVAL", "10")) * 1000;
    private static final int                                 TRACE_TIME_DUMP_NR       = Integer.parseInt(System.getProperties().getProperty("TRACE_TIME_DUMP_NR", "100"));
    private static final String                              TRACE_TIME_TOTAL         = System.getProperties().getProperty("TRACE_TIME_TOTAL");
    private static final Pattern                             TRACE_TIME_TOTAL_PATTERN = TRACE_TIME_TOTAL != null ? Pattern.compile(TRACE_TIME_TOTAL) : null;
    private static final String                              TRACE_PATTERN            = System.getProperties().getProperty("TRACE_PATTERN");
    private static final Pattern                             TRACE_PATTERN_PATTERN    = TRACE_PATTERN != null ? Pattern.compile(TRACE_PATTERN) : null;

    private static final Comparator<Map.Entry<String, Long>> COMPARATOR               = new Comparator<Map.Entry<String, Long>>() {
                                                                                          @Override
                                                                                          public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
                                                                                              return o2.getValue().compareTo(o1.getValue());
                                                                                          }
                                                                                      };

    private static final List<TraceTimer>                    ALL_TIMERS               = new ArrayList<TraceTimer>();
    private static final ThreadLocal<TraceTimer>             TIMER                    = new ThreadLocal<TraceTimer>() {
                                                                                          @Override
                                                                                          protected TraceTimer initialValue() {
                                                                                              TraceTimer tt = new TraceTimer(Thread.currentThread());
                                                                                              synchronized (ALL_TIMERS) {
                                                                                                  ALL_TIMERS.add(tt);
                                                                                                  Collections.sort(ALL_TIMERS, (o1, o2) -> o1.thread.getName().compareTo(o2.thread.getName()));
                                                                                              }
                                                                                              tt.init();
                                                                                              return tt;
                                                                                          }
                                                                                      };

    private static Timer                                     dumpTimer;

    static {
        if (TRACE_TIME) {
            initTimer();
        }
    }

    private static void initTimer() {
        dumpTimer = new Timer("Timer#TraceTimer", true);
        dumpTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    dumpAll();
                } catch (Throwable t) {
                    System.err.println("Throwable in TraceTimer:");
                    t.printStackTrace();
                }
            }
        }, TRACE_TIME_DUMP_INTERVAL, TRACE_TIME_DUMP_INTERVAL);
    }

    private static boolean             changed = false;

    private long                       time;
    private long                       grandTotal;
    private final Deque<String>        queue   = new LinkedList<String>();
    private final Map<String, Long>    total   = new LinkedHashMap<String, Long>();
    private final Map<String, Integer> count   = new LinkedHashMap<String, Integer>();
    private final Thread               thread;

    private TraceTimer(Thread thread) {
        this.thread = thread;
    }

    private void init() {
        queue.offerLast(REST);
        time = System.nanoTime();
    }

    private synchronized void clear() {
        grandTotal = 0l;
        queue.clear();
        total.clear();
        count.clear();
        init();
    }

    private synchronized void begin(String name) {
        long now = System.nanoTime();
        if (TRACE_PATTERN_PATTERN == null || TRACE_PATTERN_PATTERN.matcher(name).matches()) {
            String current = queue.peekLast();
            queue.offerLast(name);
            long delta = now - time;
            total(current, delta);
            if (TRACE_TIME_STEP) {
                System.out.printf("%-32s BEGIN %-44s at %16dns\n", thread.getName(), name, now);
            }
            changed = true;
            time = System.nanoTime();
        } else {
            time += System.nanoTime() - now;
        }
    }

    private synchronized void end(String name) {
        long now = System.nanoTime();
        if (TRACE_PATTERN_PATTERN == null || TRACE_PATTERN_PATTERN.matcher(name).matches()) {
            if (queue.size() > 1) {
                count(name);
                long delta = now - time;
                total(name, delta);
                String last = queue.pollLast();
                if (TRACE_TIME_STEP) {
                    System.out.printf("%-32s   END %-44s at %16dns\n", thread.getName(), name, now);
                }
                if (!name.equals(last)) {
                    System.err.println("Trace Timer begin/end mis match: '" + last + "' <> '" + name + "'");
                }
            }
            changed = true;
            time = System.nanoTime();
        } else {
            time += System.nanoTime() - now;
        }
    }

    private void count(String name) {
        Integer c = count.get(name);
        c = c != null ? c + 1 : 1;
        count.put(name, c);
    }

    private void total(String name, long delta) {
        if (name != REST) {
            grandTotal += delta;
        }
        Long t = total.get(name);
        t = t != null ? t + delta : delta;
        total.put(name, t);
    }

    private synchronized long sum(Map<String, Long> sumTotal, Map<String, Integer> sumCount) {
        for (Entry<String, Long> entry : total.entrySet()) {
            Long tot = sumTotal.get(entry.getKey());
            sumTotal.put(entry.getKey(), tot != null ? tot + entry.getValue() : entry.getValue());
        }
        for (Entry<String, Integer> entry : count.entrySet()) {
            Integer cnt = sumCount.get(entry.getKey());
            sumCount.put(entry.getKey(), cnt != null ? cnt + entry.getValue() : entry.getValue());
        }
        return grandTotal;
    }

    private synchronized void dump(StringBuilder log) {
        dump(log, thread.getName(), grandTotal, total, count, 1);
    }

    private static void dump(StringBuilder log, String name, long grandTotal, Map<String, Long> total, Map<String, Integer> count, int nrOffThreads) {
        if (grandTotal > 0l) {
            List<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(total.entrySet());
            Collections.sort(list, COMPARATOR);
            boolean preDone = false;
            for (int i = 0; i < TRACE_TIME_DUMP_NR && i < list.size(); i++) {
                Map.Entry<String, Long> entry = list.get(i);
                Long tot = total.get(entry.getKey());
                tot = tot != null ? tot : 0l;
                long prc = 100l * tot / grandTotal;
                if (!preDone) {
                    preDone = true;
                    log.append(String.format("------------%-32s%10dms--------------------\n", name, grandTotal / MIL).replace(' ', '-'));
                }
                Integer cnt = count.get(entry.getKey());
                cnt = cnt != null ? cnt : nrOffThreads;
                log.append(String.format(" %-35s%7d#%10dms%10dmus/#%4d%%\n", entry.getKey(), cnt, tot / MIL, tot / cnt / 1000, prc));
            }
        }
    }

    public static void traceBegin(String name) {
        if (TRACE_TIME) {
            TIMER.get().begin(name);
        }
    }

    public static void traceEnd(String name) {
        if (TRACE_TIME) {
            TIMER.get().end(name);
        }
    }

    public static void clearAll() {
        if (TRACE_TIME) {
            synchronized (ALL_TIMERS) {
                dumpTimer.cancel();
            }
            changed = false;
            TraceTimer[] all;
            synchronized (ALL_TIMERS) {
                all = ALL_TIMERS.toArray(new TraceTimer[ALL_TIMERS.size()]);
            }
            for (final TraceTimer tt : all) {
                tt.clear();
            }
            synchronized (ALL_TIMERS) {
                initTimer();
            }
        }
    }

    public static void dumpAll() {
        if (TRACE_TIME && changed) {
            changed = false;
            TraceTimer[] all;
            synchronized (ALL_TIMERS) {
                all = ALL_TIMERS.toArray(new TraceTimer[ALL_TIMERS.size()]);
            }
            StringBuilder log = new StringBuilder();
            if (TRACE_TIME_TOTAL != null) {
                long grandTotal = 0l;
                Map<String, Long> total = new LinkedHashMap<String, Long>();
                Map<String, Integer> count = new LinkedHashMap<String, Integer>();
                int nrOffThreads = 0;
                for (final TraceTimer tt : all) {
                    if (TRACE_TIME_TOTAL_PATTERN.matcher(tt.thread.getName()).matches()) {
                        grandTotal += tt.sum(total, count);
                        nrOffThreads++;
                    } else {
                        tt.dump(log);
                    }
                }
                dump(log, "Total of " + nrOffThreads + " " + TRACE_TIME_TOTAL + " threads", grandTotal, total, count, nrOffThreads);
            } else {
                for (final TraceTimer tt : all) {
                    tt.dump(log);
                }
            }
            if (log.length() > 0) {
                System.err.println(log);
            }
        }
    }
}
