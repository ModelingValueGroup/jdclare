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

import java.util.function.Consumer;

@FunctionalInterface
public interface SerializableConsumer<U> extends Consumer<U>, LambdaReflection {

    @Override
    default SerializableConsumerImpl<U> of() {
        return this instanceof SerializableConsumerImpl ? (SerializableConsumerImpl<U>) this : new SerializableConsumerImpl<U>(this);
    }

    static class SerializableConsumerImpl<U> extends LambdaImpl<SerializableConsumer<U>> implements SerializableConsumer<U> {

        private static final long serialVersionUID = -6443217484725683637L;

        public SerializableConsumerImpl(SerializableConsumer<U> f) {
            super(f);
        }

        @Override
        public void accept(U t) {
            f.accept(t);
        }

    }

}
