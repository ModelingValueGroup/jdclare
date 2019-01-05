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

package org.modelingvalue.jdclare.swing.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent;
import org.modelingvalue.jdclare.swing.DToolbar;
import org.modelingvalue.jdclare.swing.DToolbarItem;
import org.modelingvalue.jdclare.swing.InputDeviceData;
import org.modelingvalue.jdclare.swing.ScrollPane;
import org.modelingvalue.jdclare.swing.SplitPane;
import org.modelingvalue.jdclare.swing.draw2d.ClickMode;
import org.modelingvalue.jdclare.swing.draw2d.DCanvas;
import org.modelingvalue.jdclare.swing.draw2d.DCircle;
import org.modelingvalue.jdclare.swing.draw2d.DDimension;
import org.modelingvalue.jdclare.swing.draw2d.DImage;
import org.modelingvalue.jdclare.swing.draw2d.DLine;
import org.modelingvalue.jdclare.swing.draw2d.DShape;
import org.modelingvalue.jdclare.swing.draw2d.LineMode;
import org.modelingvalue.jdclare.swing.draw2d.SelectionMode;

public interface NewtonMainWindow extends SplitPane, DStruct1<NewtonUniverse> {

    @Property(key = 0)
    NewtonUniverse universe();

    default DToolbarItem item(String text, String imageLink, Consumer<ActionEvent> action) {
        return dclareUU(DToolbarItem.class, //
                set(DToolbarItem::minimumSize, dclare(DDimension.class, 50, 50)), //
                set(DToolbarItem::text, text), //
                set(DToolbarItem::action, action), //
                set(DToolbarItem::imageLink, dclare(DImage.class, imageLink))//
        );
    }

    default void appendShape(DShape s) {
        set(canvas(), DCanvas::shapes, List::append, s);
    }

    default DCanvas canvas() {
        return (DCanvas) ((ScrollPane) leftComponent()).viewportView();
    }

    default void prependShape(DShape s) {
        set(canvas(), DCanvas::shapes, List::prepend, s);
    }

    @Override
    @Property(constant)
    default double resizeWeight() {
        return 1;
    }

    @Override
    default boolean vertical() {
        return false;
    }

    @Override
    default boolean disableDivider() {
        return true;
    }

    @Override
    @Property(constant)
    default DComponent leftComponent() {
        return dclareUU(ScrollPane.class, sp -> {
            DCanvas canvas = dclareUU(DCanvas.class, set(DCanvas::color, new Color(200, 255, 200)));
            set(sp, ScrollPane::viewportView, canvas);
            set(canvas, DCanvas::mode, selectionMode());
        });
    }

    @Property(constant)
    default ClickMode circleMode() {
        return dclareUU(ClickMode.class, set(ClickMode::action, c -> {
            InputDeviceData di = c.deviceInput();
            appendShape(dclareUU(DCircle.class, set(DShape::position, di.mousePosition())));
            set(c, DCanvas::mode, selectionMode());
        }));
    }

    @Property(constant)
    default LineMode lineMode() {
        return dclareUU(LineMode.class, set(LineMode::action, (c, sel) -> {
            DShape one = sel.get(0);
            DShape two = sel.get(1);
            prependShape(dclareUU(DLine.class, //
                    rule(DShape::position, l -> one.centre()), //
                    rule(DLine::endPoint, l -> two.centre()), //
                    rule("delete", l -> {
                        if ((pre(one, DObject::dParent) != null && one.dParent() == null) || //
                        (pre(two, DObject::dParent) != null && two.dParent() == null)) {
                            clear(l);
                        }
                    }) //
            ));
            set(c, DCanvas::mode, selectionMode());
        }));
    }

    @Property(constant)
    default SelectionMode selectionMode() {
        return dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE);
    }

    @Override
    @Property(constant)
    default DComponent rigthComponent() {
        return dclareUU(DToolbar.class, //
                set(DToolbar::preferredSize, dclare(DDimension.class, 40, 100)), //
                set(DToolbar::minimumSize, dclare(DDimension.class, 50, 100)), //
                set(DToolbar::items, List.of(//
                        item("Select", "selection.png", (x) -> {
                            set(canvas(), DCanvas::mode, selectionMode());
                        }), //
                        item("Circle", "circle.png", (x) -> {
                            set(canvas(), DCanvas::mode, circleMode());
                        }), //
                        item("Line", "line.png", (x) -> {
                            set(canvas(), DCanvas::mode, lineMode());
                        }) //
                )));
    }

}
