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

import java.io.Serializable;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.util.Internable;

public final class This implements Mutable, Internable, Serializable {

    private static final long         serialVersionUID = 5000610308072466985L;

    private static final MutableClass THIS_CLASS       = new MutableClass() {

                                                           @Override
                                                           public Collection<? extends Observer<?>> dObservers() {
                                                               return Set.of();
                                                           }

                                                           @Override
                                                           public Collection<? extends Setable<? extends Mutable, ?>> dSetables() {
                                                               return Set.of();
                                                           }

                                                           @Override
                                                           public Collection<? extends Setable<? extends Mutable, ?>> dContainers() {
                                                               return Set.of();
                                                           }

                                                           @Override
                                                           public Collection<? extends Constant<? extends Mutable, ?>> dConstants() {
                                                               return Set.of();
                                                           }

                                                       };

    public This() {
        super();
    }

    @Override
    public String toString() {
        return "<this>";
    }

    @Override
    public MutableClass dClass() {
        return THIS_CLASS;
    }

    @Override
    public final Mutable resolve(Mutable self) {
        return self;
    }

    @Override
    public Collection<? extends Observer<?>> dMutableObservers() {
        return Set.of();
    }

}
