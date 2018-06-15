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

package org.modelingvalue.jdclare.workbench;

import static org.modelingvalue.jdclare.DClare.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.Icon;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.ContainingCollection;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.meta.DObjectClass;
import org.modelingvalue.jdclare.meta.DProperty;
import org.modelingvalue.jdclare.swing.DTreeNode;
import org.modelingvalue.jdclare.swing.MenuItem;
import org.modelingvalue.jdclare.swing.PopupMenu;
import org.modelingvalue.jdclare.swing.Tree;
import org.modelingvalue.jdclare.swing.draw2d.D2D;
import org.modelingvalue.jdclare.syntax.Token;
import org.modelingvalue.jdclare.types.DClassReference;
import org.modelingvalue.jdclare.types.DType;

@SuppressWarnings("rawtypes")
public interface UniverseExplorer extends Tree, DStruct1<WBUniverse> {
    @Property(key = 0)
    WBUniverse wb();

    @Override
    default DTreeNode root() {
        return dclare(RootNode.class, null, DClare.dUniverse());
    }

    interface DObjectTreeNode extends DTreeNode<DObject> {
        @Override
        default Collection<DTreeNode> children(DObject object) {
            return object//
                    .dObjectClass()//
                    .allContainments()//
                    .filter(c -> c.visible())//
                    .sorted()//
                    .map(c -> dclare(DPropertyTreeNode.class, this, c));
        }

        @Override
        default PopupMenu popupMenu() {
            return dclare(DeleteMenu.class, this);
        }

        @Override
        default String name() {
            return object().toString();
        }
    }

    interface DPropertyTreeNode extends DTreeNode<DProperty> {
        Icon PROPERTY_ICON = new PropIcon();

        @SuppressWarnings("unchecked")
        @Override
        default Collection<DTreeNode> children(DProperty property) {
            Collection coll = property.getCollection((DObject) parent().object());
            return (coll instanceof List ? coll : coll.sorted()) //
                    .map(c -> dclare(DObjectTreeNode.class, this, (DObject) c));
        }

        @Override
        default String name() {
            return object().name();
        }

        @Override
        default Icon icon() {
            return PROPERTY_ICON;
        }

        @Override
        default PopupMenu popupMenu() {
            return dclare(CreateMenu.class, this);
        }

        class PropIcon implements Icon {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                D2D.drawRect(g, x, y + 3, 10, 3, Color.GREEN, Color.BLACK, null);
            }

            @Override
            public int getIconWidth() {
                return 12;
            }

            @Override
            public int getIconHeight() {
                return 9;
            }
        }
    }

    interface RootNode extends DObjectTreeNode {
        @Override
        default boolean expanded() {
            return true;
        }
    }

    private DTreeNode node(DObject object) {
        return object instanceof DUniverse ? root() : dclare(DObjectTreeNode.class, dclare(DPropertyTreeNode.class, node(object.dParent()), object.dContainmentProperty()), object);
    }

    @Rule
    default void setSelected() {
        DTreeNode selected = selected();
        set(wb(), WBUniverse::selected, selected != null ? (DObject) selected.object() : null);
    }

    @Rule
    default void setProblemSelection() {
        DProblem pre = pre(wb(), WBUniverse::selectedProblem);
        DProblem post = wb().selectedProblem();
        if (post != null && !post.equals(pre) && !(post.context() instanceof Token)) {
            set(this, Tree::selected, node(post.context()));
        }
    }

    interface CreateMenu extends PopupMenu, DStruct1<DPropertyTreeNode> {
        @Property(key = 0)
        DPropertyTreeNode node();

        @SuppressWarnings("unchecked")
        @Override
        default List<MenuItem> items() {
            DProperty prop = node().object();
            if (prop.many()) {
                DType et = prop.type().elementType();
                if (et instanceof DClassReference && ((DClassReference) et).referenced() instanceof DObjectClass) {
                    DObject parent = (DObject) node().parent().object();
                    DObjectClass cls = (DObjectClass) ((DClassReference) et).referenced();
                    return ((Set<DObjectClass<?>>) cls.allSubs())//
                            .filter(c -> !c.isAbstract() && c.keys().size() == 0)//
                            .map(c -> {
                                Consumer<ActionEvent> action = id(e -> dClare().put(new Object(), () -> {
                                    ((DProperty<DObject, ContainingCollection<DObject>>) prop).set(parent, ContainingCollection::add, dclareUU(c.jClass()));
                                    DClare.set(node(), DTreeNode::expanded, true);
                                }), this);
                                return dclare(MenuItem.class, this, "create " + c.name(), action);
                            })//
                            .toList();

                }
            }
            return List.of();
        }
    }

    interface DeleteMenu extends PopupMenu, DStruct1<DObjectTreeNode> {
        @Property(key = 0)
        DObjectTreeNode node();

        @SuppressWarnings("unchecked")
        @Override
        default List<MenuItem> items() {
            DObject child = node().object();
            if (child.getKeySize() == 1 && child.getKey(0) instanceof UUID) {
                DProperty prop = (DProperty) node().parent().object();
                if (prop.many()) {
                    DObject parent = (DObject) node().parent().parent().object();
                    Consumer<ActionEvent> action = id(e -> dClare().put(new Object(), () -> ((DProperty<DObject, ContainingCollection<DObject>>) prop).set(parent, ContainingCollection::remove, child)), this);
                    return List.of(dclare(MenuItem.class, this, "delete", action));
                }
            }
            return List.of();
        }
    }
}
