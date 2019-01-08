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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Deferred;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DContainer.DContainerNative;
import org.modelingvalue.jdclare.swing.draw2d.DDimension;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.transactions.Compound;

@Native(DContainerNative.class)
public interface DContainer extends DVisible {

    @Default
    @Property
    default DPoint location() {
        return DPoint.NULL;
    }

    @Property(optional)
    DDimension preferredSize();

    @Property(optional)
    DDimension minimumSize();

    @Default
    @Property
    default DDimension size() {
        return DDimension.NULL;
    }

    class DContainerNative<N extends DContainer, T extends Container> extends VisibleNative<N> implements ComponentListener {

        protected T swing;

        public DContainerNative(N visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent, Compound tx) {
            super.init(parent, tx);
            swing.addComponentListener(this);
            swing.setVisible(true);
        }

        @Override
        public void exit(DObject parent, Compound tx) {
            swing.removeComponentListener(this);
            swing.setVisible(false);
            super.exit(parent, tx);
        }

        public void location(DPoint pre, DPoint post) {
            swing.setLocation((int) post.x(), (int) post.y());
        }

        public void size(DDimension pre, DDimension post) {
            swing.setSize((int) post.width(), (int) post.height());
        }

        @Deferred
        public void preferredSize(DDimension pre, DDimension post) {
            if (post != null) {
                swing.setPreferredSize(new Dimension((int) post.width(), (int) post.height()));
            }
        }

        public void minimumSize(DDimension pre, DDimension post) {
            if (post != null) {
                swing.setMinimumSize(new Dimension((int) post.width(), (int) post.height()));
            }
        }

        public T swing() {
            return swing;
        }

        @SuppressWarnings("unchecked")
        public static <C extends Container> C swing(DContainer visible) {
            return ((DContainerNative<?, C>) dNative(visible)).swing;
        }

        @Override
        public void componentResized(ComponentEvent e) {
            Dimension size = swing.getSize();
            set(DContainer::size, dclare(DDimension.class, (double) size.width, (double) size.height));
        }

        @Override
        public void componentMoved(ComponentEvent e) {
            Point loc = swing.getLocation();
            set(DContainer::location, dclare(DPoint.class, (double) loc.x, (double) loc.y));
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

    }

}
