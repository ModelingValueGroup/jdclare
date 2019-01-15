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

package org.modelingvalue.jdclare.swing.draw2d;

import static org.modelingvalue.jdclare.DClare.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.DComponent.DComponentNative;
import org.modelingvalue.jdclare.swing.DVisible;
import org.modelingvalue.jdclare.swing.InputDeviceData;
import org.modelingvalue.jdclare.swing.draw2d.DShape.ShapeNative;

@Native(ShapeNative.class)
public interface DShape extends DVisible {

    default DCanvas canvas() {
        return dAncestor(DCanvas.class);
    }

    @Property
    DPoint position();

    @Default
    @Property
    default boolean highlighted() {
        return false;
    }

    @Property
    default Color lineColor() {
        return highlighted() ? Color.red : Color.black;
    }

    boolean hit(DPoint pt);

    @Property
    default boolean selected() {
        if (DClare.dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE).equals(canvas().mode())) {
            InputDeviceData di = canvas().deviceInput();
            if (di.isLeftMouseDown() && !pre(di, InputDeviceData::isLeftMouseDown)) {
                return hit(di.mousePosition()) ? !pre(this, DShape::selected) : di.isCtrlDown() ? selected() : false;
            }
        }
        return selected();
    }

    @Rule
    default void delete() {
        if (canvas() != null && selected()) {
            InputDeviceData di = canvas().deviceInput();
            if (di.pressedKeys().contains(KeyEvent.VK_DELETE)) {
                DClare.unparent(this);
            }
        }
    }

    @Default
    @Property
    default DPoint centre() {
        return DPoint.NULL;
    }

    abstract class ShapeNative<S extends DShape> extends VisibleNative<S> {

        public ShapeNative(S visible) {
            super(visible);
        }

        public abstract void paint(Graphics2D g);

        public void position(DPoint pre, DPoint post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

        public void lineColor(Color pre, Color post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

    }

}
