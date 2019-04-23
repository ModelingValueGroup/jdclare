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

import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Deferred;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.Frame.FrameNative;
import org.modelingvalue.jdclare.swing.draw2d.DDimension;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

@Native(FrameNative.class)
public interface Frame extends DContainer {

    @Property(containment)
    DComponent contentPane();

    @Property({containment, optional})
    DMenubar menubar();

    class FrameNative extends DContainerNative<Frame, JFrame> {

        private final WindowAdapter windowLsitener;

        public FrameNative(Frame visible) {
            super(visible);
            windowLsitener = new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    DClare.<GuiUniverse, Set<Frame>, Frame> set(((GuiUniverse) DClare.dUniverse()), GuiUniverse::frames, Set::remove, visible);
                }
            };
        }

        @Override
        public void init(DObject parent) {
            swing = new JFrame();
            swing.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            swing.addWindowListener(windowLsitener);
            super.init(parent);
        }

        @Override
        public void exit(DObject parent) {
            swing.removeWindowListener(windowLsitener);
            super.exit(parent);
        }

        @Override
        public void location(DPoint pre, DPoint post) {
            swing.setLocation((int) post.x(), (int) post.y());
        }

        @Override
        public void size(DDimension pre, DDimension post) {
        }

        public void contentPane(DComponent pre, DComponent post) {
            swing.setContentPane(post != null ? swing(post) : null);
            if (post != null) {
                swing.pack();
            }
        }

        public void menubar(DMenubar pre, DMenubar post) {
            JMenuBar menuBar = post != null ? (JMenuBar) swing(post) : null;
            swing.setJMenuBar(menuBar);
            if (post != null) {
                swing.pack();
            }
        }

        @Override
        @Deferred
        public void preferredSize(DDimension pre, DDimension post) {
            super.preferredSize(pre, post);
            if (post != null) {
                swing.pack();
            }
        }

    }
}
