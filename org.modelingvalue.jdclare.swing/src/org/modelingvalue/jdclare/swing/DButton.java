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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DButton.ButtonNative;
import org.modelingvalue.jdclare.swing.draw2d.DImage;
import org.modelingvalue.transactions.Compound;

@Native(ButtonNative.class)
public interface DButton extends DComponent {

    @Property
    String text();

    @Property
    boolean selected();

    @Property(optional)
    DImage imageLink();

    @Property
    Consumer<ActionEvent> action();

    class ButtonNative extends DComponentNative<DButton, JButton> {

        public ButtonNative(DButton visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent, Compound tx) {
            swing = new JButton();
            swing.setBorder(new EmptyBorder(2, 2, 2, 2));
            swing.addActionListener(x -> {
                swing.setSelected(!swing.isSelected());
                Consumer<ActionEvent> action = visible.action();
                dClare().put(action, () -> action.accept(x));
                DClare.set(visible, DButton::selected, swing.isSelected());
            });
            updateImageAndText(visible.imageLink() != null ? visible.imageLink().image() : null, visible.text());
            super.init(parent, tx);
        }

        private void updateImageAndText(Image i, String text) {
            if (i != null) {
                swing.setIcon(new ImageIcon(i));
                swing.setText(null);
                swing.setToolTipText(text);
            } else {
                swing.setIcon(null);
                swing.setText(text);
            }
        }

        public void imageLink(DImage pre, DImage post) {
            if (pre != post) {
                updateImageAndText(post.image(), visible.text());
            }
        }

        public void text(String pre, String post) {
            updateImageAndText(visible.imageLink() != null ? visible.imageLink().image() : null, post);
        }
    }
}
