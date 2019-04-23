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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.JMenuItem;

import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct3;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.MenuItem.MenuItemNative;

@Native(MenuItemNative.class)
public interface MenuItem extends DStruct3<DComponent, String, Consumer<ActionEvent>>, DComponent, DNamed {

    @Property(key = 0)
    DComponent menu();

    @Override
    @Property(key = 1)
    String name();

    @Property(key = 2)
    Consumer<ActionEvent> action();

    class MenuItemNative extends DComponentNative<MenuItem, JMenuItem> implements ActionListener {

        public MenuItemNative(MenuItem visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent) {
            swing = new JMenuItem(visible.name());
            swing.addActionListener(this);
        }

        @Override
        public void exit(DObject parent) {
            if (swing != null) {
                swing.removeActionListener(this);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Consumer<ActionEvent> action = visible.action();
            if (action != null) {
                action.accept(e);
            }
        }

    }
}
