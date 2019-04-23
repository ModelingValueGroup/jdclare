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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuBar;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DMenu.DMenuNative;
import org.modelingvalue.jdclare.swing.DMenubar.DMenubarNative;

@Native(DMenubarNative.class)
public interface DMenubar extends DComponent, DStruct1<Frame> {

    @Property(key = 0)
    Frame frame();

    @Property(containment)
    List<DMenu> menus();

    class DMenubarNative extends DComponentNative<DMenubar, JMenuBar> implements ActionListener {

        public DMenubarNative(DMenubar visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent) {
            swing = new JMenuBar();
            super.init(parent);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

        }

        public void menus(List<DMenu> pre, List<DMenu> post) {
            swing.removeAll();
            post.forEach(i -> swing.add(((DMenuNative) dNative(i)).swing));
        }
    }

}
