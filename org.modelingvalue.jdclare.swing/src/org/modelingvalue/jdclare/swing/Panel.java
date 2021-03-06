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

import java.awt.LayoutManager;

import javax.swing.JPanel;

import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.Panel.PanelNative;

@Native(PanelNative.class)
public interface Panel extends DComponent {

    @Property
    Map<DComponent, Object> content();

    @Property(constant)
    LayoutManager layoutManager();

    @Property(containment)
    default Set<DComponent> components() {
        return content().toKeys().toSet();
    }

    class PanelNative extends DComponentNative<Panel, JPanel> {

        public PanelNative(Panel visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent) {
            swing = new JPanel(visible.layoutManager());
            super.init(parent);
        }

        @Override
        public void exit(DObject parent) {
            super.exit(parent);
        }

        public void content(Map<DComponent, Object> pre, Map<DComponent, Object> post) {
            pre.removeAllKey(post).forEach(ct -> {
                swing.remove(swing(ct.getKey()));
            });
            post.removeAllKey(pre).forEach(ct -> {
                swing.add(swing(ct.getKey()), ct.getValue());
            });
            swing.invalidate();
            swing.repaint();
        }

    }

}
