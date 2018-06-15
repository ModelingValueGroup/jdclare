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

package org.modelingvalue.jdclare.test;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface Reparents extends DUniverse {

    @Property(containment)
    Set<Node> tree();

    @Override
    default void init() {
        DUniverse.super.init();
        set(this, Reparents::tree, Collection.range(0, 100).map(nr -> dclare(Node.class, nr)).toSet());
    }

    interface Node extends DObject, DStruct1<Integer> {

        @Property(key = 0)
        int nr();

        @Property(containment)
        Set<Node> children();

        @Rule
        default void makeTree() {
            if (dParent() instanceof Reparents) {
                if (nr() < 100 && nr() % 10 == 0) {
                    int g = nr() / 10 + 1;
                    Node parent = dclare(Node.class, g * 100);
                    Set<Node> children = Collection.range(nr(), nr() + 10).map(nr -> dclare(Node.class, nr)).toSet();
                    reparent(parent, children);
                }
                if (nr() == 100) {
                    Node parent = dclare(Node.class, 10000);
                    Set<Node> children = Collection.range(1, 11).map(nr -> dclare(Node.class, nr * 100)).toSet();
                    reparent(parent, children);
                }
            }
        }

        default void reparent(Node parent, Set<Node> children) {
            set((Reparents) dParent(), Reparents::tree, Set::add, parent);
            set(parent, Node::children, Set::addAll, children);
        }

    }

}
