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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.swing.InputDeviceData;

public interface LineMode extends CanvasMode {

    @Property
    List<DShape> shapes();

    @Property(constant)
    BiConsumer<DCanvas, List<DShape>> action();

    @Rule
    default void mode() {
        DCanvas c = dAncestor(DCanvas.class);
        DShape s = findClickedShape();
        if (s != null) {
            if (shapes().isEmpty()) {
                DClare.set(this, LineMode::shapes, List::add, s);
                DClare.set(s, DShape::highlighted, true);
            } else if (!shapes().contains(s)) {
                action().accept(c, shapes().add(s));
                DClare.set(shapes().get(0), DShape::highlighted, false);
            }
        }
    }

    @Property(optional)
    default DShape findClickedShape() {
        DCanvas c = dAncestor(DCanvas.class);
        InputDeviceData di = c.deviceInput();
        if (di.isLeftMouseDown() && !pre(di, InputDeviceData::isLeftMouseDown)) {
            Optional<DShape> shape = c.shapes().filter(s -> s.hit(di.mousePosition())).findFirst();
            return shape.isPresent() ? shape.get() : null;
        }
        return null;
    }

}
