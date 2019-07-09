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
public interface SerializableTriFunction<A, B, C, D> extends TriFunction<A, B, C, D>, LambdaReflection {

    @Override
    default SerializableTriFunctionImpl<A, B, C, D> of() {
        return this instanceof SerializableTriFunctionImpl ? (SerializableTriFunctionImpl<A, B, C, D>) this : new SerializableTriFunctionImpl<A, B, C, D>(this);
    }

    static class SerializableTriFunctionImpl<A, B, C, D> extends LambdaImpl<SerializableTriFunction<A, B, C, D>> implements SerializableTriFunction<A, B, C, D> {

        private static final long serialVersionUID = -1175580467666540454L;

        public SerializableTriFunctionImpl(SerializableTriFunction<A, B, C, D> f) {
            super(f);
        }

        @Override
        public D apply(A s, B t, C u) {
            return f.apply(s, t, u);
        }

    }

}
