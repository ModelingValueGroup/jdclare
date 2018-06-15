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
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.Color;
import java.awt.event.KeyEvent;

import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent.DComponentNative;
import org.modelingvalue.jdclare.swing.InputDeviceData;
import org.modelingvalue.jdclare.swing.draw2d.DFilled.FilledNative;

@Native(FilledNative.class)
public interface DFilled extends DShape {

    @Property
    default Color color() {
        return selected() ? Color.yellow : Color.white;
    }

    @Property(optional)
    String text();

    @Override
    default DPoint position() {
        DPoint dragging = dragging();
        return dragging != null ? (canvas().deviceInput().pressedKeys().contains(KeyEvent.VK_ESCAPE) ? //
                dragStartPosition() : dragging.plus(canvas().deviceInput().mousePosition())) : position();
    }

    @Property(optional)
    DPoint dragStartPosition();

    @Property(optional)
    default DPoint dragging() {
        DCanvas canvas = canvas();
        InputDeviceData di = canvas.deviceInput();
        if (di.isLeftMouseDown() && !pre(di, InputDeviceData::isLeftMouseDown) && hit(di.mousePosition())) {
            set(this, DFilled::dragStartPosition, position());
            return position().minus(di.mousePosition());
        } else if (!di.isLeftMouseDown() || di.pressedKeys().contains(KeyEvent.VK_ESCAPE)) {
            set(this, DFilled::dragStartPosition, null);
            return null;
        } else {
            return dragging();
        }
    }

    abstract class FilledNative<S extends DFilled> extends ShapeNative<S> {

        public FilledNative(S visible) {
            super(visible);
        }

        public void color(Color pre, Color post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

        public void text(String pre, String post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

    }

}
