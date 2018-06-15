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
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Property;

public interface DMatcher extends DStruct2<DPattern, CharSequence> {
    @Property(key = 0)
    DPattern pattern();

    @Property(key = 1)
    CharSequence string();

    default Matcher matcher() {
        CharSequence string = string();
        return pattern().pattern().matcher(string == null ? "" : string);
    }

    default Collection<DMatch> matches(boolean include) {
        class DMatchIterator implements Iterator<DMatch> {

            private final Matcher      matcher = matcher();
            private final CharSequence input   = string();
            private final int          length  = input == null ? 0 : input.length();

            private int                current;
            private DMatch             next;
            private DMatch             nextNext;

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
                } else if (matcher.find()) {
                    if (current < matcher.start()) {
                        if (include) {
                            next = dclare(DMatch.class, current, input.subSequence(current, matcher.end()).toString(), 0);
                        } else {
                            next = dclare(DMatch.class, current, input.subSequence(current, matcher.start()).toString(), -1);
                            nextNext = dclare(DMatch.class, matcher.start(), matcher.group(), 0);
                        }
                    } else {
                        next = dclare(DMatch.class, matcher.start(), matcher.group(), 0);
                    }
                    current = matcher.end();
                    return true;
                } else {
                    next = dclare(DMatch.class, current, input.subSequence(current, length).toString(), -1);
                    current = length;
                    return true;
                }
            }
        }
        return Collection.of(Spliterators.spliteratorUnknownSize(new DMatchIterator(), Spliterator.ORDERED | Spliterator.NONNULL));
    }

}
