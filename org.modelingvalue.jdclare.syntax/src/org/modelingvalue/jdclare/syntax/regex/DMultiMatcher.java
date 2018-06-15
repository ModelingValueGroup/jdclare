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

package org.modelingvalue.jdclare.syntax.regex;

import static org.modelingvalue.jdclare.DClare.*;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;

public interface DMultiMatcher extends DStruct2<List<DPattern>, CharSequence> {
    @Property(key = 0)
    List<DPattern> patterns();

    @Property(key = 1)
    String string();

    default List<Matcher> matchers() {
        CharSequence string = string();
        return patterns().map(p -> p.pattern().matcher(string == null ? "" : string)).toList();
    }

    default Collection<DMatch> matches() {
        class DMatchIterator implements Iterator<DMatch> {

            private final List<Matcher> matchers     = matchers();
            private final int           nrOfMatchers = matchers.size();
            private final CharSequence  input        = string();
            private final int           length       = input == null ? 0 : input.length();

            private int                 current;
            private DMatch              next;
            private DMatch              nextNext;

            @Override
            public DMatch next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                } else {
                    DMatch n = next;
                    next = nextNext;
                    nextNext = null;
                    return n;
                }
            }

            @Override
            public boolean hasNext() {
                if (next != null) {
                    return true;
                } else if (current == length) {
                    return false;
                } else {
                    int r = Collection.range(nrOfMatchers).map(i -> {
                        Matcher m = matchers.get(i);
                        return m.find(current) ? i : -1;
                    }).filter(i -> i >= 0).reduce((x, y) -> {
                        Matcher a = matchers.get(x), b = matchers.get(y);
                        return a.start() < b.start() ? x : b.start() < a.start() ? y : a.end() > b.end() ? x : b.end() > a.end() ? y : x;
                    }).orElse(-1);
                    if (r >= 0) {
                        Matcher m = matchers.get(r);
                        if (current < m.start()) {
                            next = dclare(DMatch.class, current, input.subSequence(current, m.start()).toString(), -1);
                            nextNext = dclare(DMatch.class, m.start(), m.group(), r);
                        } else {
                            next = dclare(DMatch.class, m.start(), m.group(), r);
                        }
                        current = m.end();
                        return true;
                    } else {
                        next = dclare(DMatch.class, current, input.subSequence(current, length).toString(), -1);
                        current = length;
                        return true;
                    }
                }
            }
        }
        return Collection.of(Spliterators.spliteratorUnknownSize(new DMatchIterator(), Spliterator.ORDERED | Spliterator.NONNULL));
    }

}
