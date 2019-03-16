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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;

public interface Mergeables {

    static <T> T merge(T base, T[] branches) {
        return merge(base, branches, branches.length);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static <T> T merge(T base, T[] branches, int l) {
        return merge(base, (b, bs) -> {
            Mergeable merger = b instanceof Mergeable ? (Mergeable) ((Mergeable) b).getMerger() : null;
            if (merger == null) {
                for (int i = 0; i < bs.length; i++) {
                    if (bs[i] instanceof Mergeable) {
                        merger = (Mergeable) ((Mergeable) bs[i]).getMerger();
                        break;
                    }
                }
            }
            if (merger != null) {
                for (int i = 0; i < bs.length; i++) {
                    if (bs[i] == null) {
                        bs[i] = (T) merger;
                    }
                }
                return (T) ((Mergeable) (b == null ? merger : b)).merge(bs);
            } else {
                throw new NotMergeableException(b + " " + Arrays.toString(bs));
            }
        }, branches, l);
    }

    static <T> T merge(T base, BiFunction<T, T[], T> merger, T[] branches, int l) {
        boolean copied = false;
        for (int i = 0; i < l; i++) {
            if (Objects.equals(branches[i], base) || contains(branches, branches[i], i)) {
                if (i < --l) {
                    if (!copied) {
                        branches = Arrays.copyOf(branches, l + 1);
                        copied = true;
                    }
                    System.arraycopy(branches, i + 1, branches, i, l - i--);
                }
            }
        }
        if (l == 0) {
            return base;
        } else if (l == 1) {
            return branches[0];
        } else {
            return merger.apply(base, !copied || branches.length > l ? Arrays.copyOf(branches, l) : branches);
        }
    }

    static <T> boolean contains(T[] all, T e, int max) {
        for (int i = 0; i < max; i++) {
            if (Objects.equals(all[i], e)) {
                return true;
            }
        }
        return false;
    }
}
