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

package org.modelingvalue.jdclare.meta;

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.function.BiFunction;
import java.util.function.Function;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.Abstract;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DNative.ChangeHandler;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;

@Abstract
public interface DProperty<O extends DStruct, V> extends DNamed {

    @Default
    @Property
    default boolean isAbstract() {
        return false;
    }

    @Property
    boolean key();

    @Property
    int keyNr();

    @Property
    boolean containment();

    @Property
    boolean constant();

    @Property
    boolean many();

    @Property
    boolean mandatory();

    @Property
    boolean derived();

    @Property(optional)
    Function<O, V> deriver();

    @Property
    boolean validation();

    @Property(optional)
    V defaultValue();

    @Property(optional)
    DProperty<?, ?> opposite();

    @Property(optional)
    DProperty<O, Set<?>> scopeProperty();

    @SuppressWarnings("rawtypes")
    @Property
    Class type();

    @SuppressWarnings("rawtypes")
    @Property
    Class elementClass();

    @SuppressWarnings("rawtypes")
    @Property
    Class objectClass();

    @Property(optional)
    ChangeHandler<DObject, V> nativeChangeHandler();

    @Default
    @Property
    default boolean visible() {
        return true;
    }

    default boolean checkConsistency() {
        return true;
    }

    default V get(O object) {
        return DClare.get(object, this);
    }

    default int getNrOfObservers(O object) {
        return DClare.getNrOfObservers(object, this);
    }

    default Collection<?> getCollection(O object) {
        return DClare.getCollection(object, this);
    }

    default void set(O object, V value) {
        DClare.set(object, this, value);
    }

    default <E> void set(O object, BiFunction<V, E, V> function, E element) {
        DClare.set(object, this, function, element);
    }

    @SuppressWarnings("unchecked")
    default DProperty<O, V> actualize(DStructClass<?> dClass) {
        return key() ? this : (DProperty<O, V>) dClass.allProperties().get(name());
    }

}
