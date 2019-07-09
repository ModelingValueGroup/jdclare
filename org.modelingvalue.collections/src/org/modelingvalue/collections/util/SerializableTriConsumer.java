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

@FunctionalInterface
public interface SerializableTriConsumer<A, B, C> extends TriConsumer<A, B, C>, LambdaReflection {

    @Override
    default SerializableTriConsumerImpl<A, B, C> of() {
        return this instanceof SerializableTriConsumerImpl ? (SerializableTriConsumerImpl<A, B, C>) this : new SerializableTriConsumerImpl<A, B, C>(this);
    }

    static class SerializableTriConsumerImpl<A, B, C> extends LambdaImpl<SerializableTriConsumer<A, B, C>> implements SerializableTriConsumer<A, B, C> {

        private static final long serialVersionUID = -6808570298306369071L;

        public SerializableTriConsumerImpl(SerializableTriConsumer<A, B, C> f) {
            super(f);
        }

        @Override
        public void accept(A s, B t, C u) {
            f.accept(s, t, u);
        }

    }

}
