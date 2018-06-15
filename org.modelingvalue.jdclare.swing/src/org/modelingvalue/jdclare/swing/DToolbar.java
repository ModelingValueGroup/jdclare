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

import javax.swing.JPanel;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DToolbar.ToolbarNative;
import org.modelingvalue.transactions.Compound;

@Native(ToolbarNative.class)
public interface DToolbar extends DComponent {

    @Property(containment)
    List<DToolbarItem> items();

    class ToolbarNative extends DComponentNative<DToolbar, JPanel> {

        public ToolbarNative(DToolbar visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent, Compound tx) {
            swing = new JPanel();
            super.init(parent, tx);
        }

        public void items(List<DToolbarItem> pre, List<DToolbarItem> post) {
            swing.removeAll();
            for (DToolbarItem item : post) {
                swing.add(swing(item));
            }
        }

    }
}
