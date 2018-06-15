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

package org.modelingvalue.jdclare.swing;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DTreeNode.DTreeNodeNative;
import org.modelingvalue.jdclare.swing.PopupMenu.PopupMenuNative;
import org.modelingvalue.jdclare.swing.Tree.TreeNative;
import org.modelingvalue.transactions.Compound;

@SuppressWarnings("rawtypes")
@Native(TreeNative.class)
public interface Tree extends DComponent {

    @Property({constant, containment})
    DTreeNode root();

    @Property(optional)
    DTreeNode selected();

    class TreeNative extends DComponentNative<Tree, JTree> implements TreeSelectionListener, TreeExpansionListener {

        public TreeNative(Tree visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent, Compound tx) {
            DTreeNodeNative<?> root = (DTreeNodeNative) dNative(visible.root());
            swing = new JTree(root);
            swing.setRootVisible(false);
            swing.setShowsRootHandles(true);
            swing.setCellRenderer(new DTreeNode.DTreeCellRenderer());
            swing.addTreeSelectionListener(this);
            swing.addTreeExpansionListener(this);
            super.init(parent, tx);
        }

        @Override
        public void exit(DObject parent, Compound tx) {
            super.exit(parent, tx);
            swing.removeTreeSelectionListener(this);
            swing.removeTreeExpansionListener(this);
        }

        @Override
        protected void popup(int x, int y) {
            TreePath path = swing.getClosestPathForLocation(x, y);
            if (path != null) {
                DTreeNode tn = ((DTreeNodeNative<?>) path.getLastPathComponent()).visible;
                PopupMenu popupMenu = tn.popupMenu();
                if (popupMenu != null) {
                    swing.setSelectionPath(path);
                    ((PopupMenuNative) dNative(popupMenu)).swing.show(swing, x, y);
                }
            }
        }

        protected DefaultTreeModel model() {
            return (DefaultTreeModel) swing.getModel();
        }

        public void selected(DTreeNode pre, DTreeNode post) {
            swing.setSelectionPath(post != null ? ((DTreeNodeNative) dNative(post)).path() : null);
        }

        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath p = e.getNewLeadSelectionPath();
            DTreeNode s = p != null ? ((DTreeNodeNative<?>) p.getLastPathComponent()).visible : null;
            set(Tree::selected, s);
        }

        @Override
        public void treeExpanded(TreeExpansionEvent e) {
            DTreeNode n = ((DTreeNodeNative<?>) e.getPath().getLastPathComponent()).visible;
            set(n, DTreeNode::expanded, true);
        }

        @Override
        public void treeCollapsed(TreeExpansionEvent e) {
            DTreeNode n = ((DTreeNodeNative<?>) e.getPath().getLastPathComponent()).visible;
            set(n, DTreeNode::expanded, false);
        }

    }

}
