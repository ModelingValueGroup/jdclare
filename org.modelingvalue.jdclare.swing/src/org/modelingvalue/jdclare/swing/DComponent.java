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

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent.DComponentNative;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;

@Native(DComponentNative.class)
public interface DComponent extends DContainer {

    @Property({constant, containment})
    default InputDeviceData deviceInput() {
        return dclare(InputDeviceData.class, this);
    }

    class DComponentNative<D extends DComponent, J extends JComponent> extends DContainerNative<D, J> {

        private final MouseAdapter mouseAdapter;
        private final KeyAdapter   keyAdapter;

        public DComponentNative(D visible) {
            super(visible);
            mouseAdapter = new MouseAdapter() {

                @Override
                public void mouseClicked(MouseEvent arg0) {
                }

                @Override
                public void mouseDragged(MouseEvent evt) {
                    mouseMoved(evt);
                }

                @Override
                public void mouseEntered(MouseEvent arg0) {
                }

                @Override
                public void mouseExited(MouseEvent arg0) {
                }

                @Override
                public void mouseMoved(MouseEvent evt) {
                    InputDeviceData di = visible.deviceInput();
                    DPoint pt = DClare.dclare(DPoint.class, (double) evt.getX(), (double) evt.getY());
                    DClare.set(di, InputDeviceData::mousePosition, pt);
                }

                @Override
                public void mouseWheelMoved(MouseWheelEvent arg0) {
                }

                @Override
                public void mousePressed(MouseEvent evt) {
                    if (evt.isPopupTrigger()) {
                        evt.consume();
                        popup(evt.getX(), evt.getY());
                    } else {
                        swing.requestFocus();
                        InputDeviceData pre = visible.deviceInput();
                        boolean isLeft = evt.getButton() == MouseEvent.BUTTON1;
                        DClare.set(pre, InputDeviceData::isLeftMouseDown, isLeft);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent evt) {
                    if (evt.isPopupTrigger()) {
                        evt.consume();
                        popup(evt.getX(), evt.getY());
                    } else {
                        InputDeviceData pre = visible.deviceInput();
                        boolean isLeft = evt.getButton() == MouseEvent.BUTTON1;
                        DClare.set(pre, InputDeviceData::isLeftMouseDown, !isLeft);
                    }
                }
            };

            keyAdapter = new KeyAdapter() {

                @Override
                public void keyPressed(KeyEvent evt) {
                    InputDeviceData pre = visible.deviceInput();
                    int keyCode = evt.getKeyCode();
                    Set<Integer> keysPressed = pre.pressedKeys().add(keyCode);
                    DClare.set(pre, InputDeviceData::pressedKeys, keysPressed);
                }

                @Override
                public void keyReleased(KeyEvent evt) {
                    InputDeviceData pre = visible.deviceInput();
                    int keyCode = evt.getKeyCode();
                    Set<Integer> keysPressed = pre.pressedKeys().remove(Integer.valueOf(keyCode));
                    DClare.set(pre, InputDeviceData::pressedKeys, keysPressed);
                }

                @Override
                public void keyTyped(KeyEvent evt) {
                }
            };
        }

        @Override
        public void init(DObject parent) {
            swing.setOpaque(true);
            swing.addMouseListener(mouseAdapter);
            swing.addMouseMotionListener(mouseAdapter);
            swing.addKeyListener(keyAdapter);
            super.init(parent);
        }

        protected void popup(int x, int y) {
        }

        @Override
        public void exit(DObject parent) {
            super.exit(parent);
            swing.removeMouseListener(mouseAdapter);
        }

    }

}
