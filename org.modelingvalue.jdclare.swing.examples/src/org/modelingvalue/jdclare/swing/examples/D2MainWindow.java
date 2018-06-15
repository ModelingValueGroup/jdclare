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
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.DUUObject;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
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
import org.modelingvalue.jdclare.swing.draw2d.DFilled;
import org.modelingvalue.jdclare.swing.draw2d.DImage;
import org.modelingvalue.jdclare.swing.draw2d.DLine;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.jdclare.swing.draw2d.DRectangle;
import org.modelingvalue.jdclare.swing.draw2d.DShape;
import org.modelingvalue.jdclare.swing.draw2d.DTriangle;
import org.modelingvalue.jdclare.swing.draw2d.LineMode;
import org.modelingvalue.jdclare.swing.draw2d.SelectionMode;

public interface D2MainWindow extends SplitPane, DStruct1<D2Universe> {

    @Property(key = 0)
    D2Universe d2();

    @Override
    @Property(constant)
    default DComponent leftComponent() {
        return dclareUU(CirlcesAndSquaresEditor.class);
    }

    @Override
    @Property(constant)
    default DComponent rigthComponent() {
        return dclareUU(TrianglesEditor.class);
    }

    @Property(containment)
    default ExampleMapping1 mapping() {
        return dclare(ExampleMapping1.class, ((DiagramEditor) leftComponent()).canvas(), ((DiagramEditor) rigthComponent()).canvas());
    }

    @Override
    default boolean vertical() {
        return false;
    }

    interface DiagramEditor extends SplitPane {

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
    }

    interface CirlcesAndSquaresEditor extends DiagramEditor {

        @Override
        @Property(constant)
        default DComponent leftComponent() {
            return dclareUU(ScrollPane.class, (c) -> {
                DCanvas canvas = makeCanvas();
                set(c, ScrollPane::viewportView, canvas);
            });
        }

        default DCanvas makeCanvas() {
            return dclareUU(DCanvas.class, set(DCanvas::color, new Color(200, 255, 200)));
        }

        @Override
        @Property(constant)
        default DComponent rigthComponent() {
            return dclareUU(DToolbar.class, (c) -> {
                set(c, DToolbar::preferredSize, dclare(DDimension.class, 40, 100));
                set(c, DToolbar::minimumSize, dclare(DDimension.class, 50, 100));
                set(c, DToolbar::items, List.of(//
                        item("Select", "selection.png", (x) -> {
                            set(canvas(), DCanvas::mode, dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE));
                        }), //
                        item("Rectangle", "rectangle.png", (x) -> {
                            set(canvas(), DCanvas::mode, dclareUU(ClickMode.class, (e) -> {
                                set(e, ClickMode::action, (z) -> {
                                    InputDeviceData di = z.deviceInput();
                                    appendShape(dclareUU(DRectangle.class, set(DShape::position, di.mousePosition())));
                                    set(z, DCanvas::mode, dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE));
                                });
                            }));
                        }), //
                        item("Circle", "circle.png", (x) -> {
                            set(canvas(), DCanvas::mode, dclareUU(ClickMode.class, (e) -> {
                                set(e, ClickMode::action, (z) -> {
                                    InputDeviceData di = z.deviceInput();
                                    appendShape(dclareUU(DCircle.class, set(DShape::position, di.mousePosition())));
                                    set(z, DCanvas::mode, dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE));
                                });
                            }));
                        }), //
                        item("Line", "line.png", (x) -> {
                            DCanvas canvas = (DCanvas) ((ScrollPane) leftComponent()).viewportView();
                            set(canvas, DCanvas::mode, dclareUU(LineMode.class, (s) -> {
                                set(s, LineMode::action, (sel) -> {
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
                                    set(canvas(), DCanvas::mode, dclareUU(SelectionMode.class, DCanvas.SELECTION_MODE));
                                });
                            }));
                        }) //
                ));
            });
        }
    }

    interface TrianglesEditor extends DiagramEditor {

        @Override
        @Property(constant)
        default DComponent leftComponent() {
            return dclareUU(ScrollPane.class, (c) -> {
                DCanvas canvas = makeCanvas();
                set(c, ScrollPane::viewportView, canvas);
            });
        }

        default DCanvas makeCanvas() {
            return dclareUU(DCanvas.class, set(DCanvas::color, new Color(217, 233, 255)));
        }

        @Override
        @Property(constant)
        default DComponent rigthComponent() {
            return dclareUU(DToolbar.class, (c) -> {
                set(c, DToolbar::preferredSize, dclare(DDimension.class, 40, 100));
                set(c, DToolbar::minimumSize, dclare(DDimension.class, 50, 100));
                set(c, DToolbar::items, List.of(//
                        item("Triangle", "triangle.png", (x) -> {
                            appendShape(dclareUU(DTriangle.class, set(DShape::position, dclare(DPoint.class, 100, 100))));
                        }) //                        
                ));
            });
        }
    }

    interface ExampleMapping1 extends DObject, DStruct2<DCanvas, DCanvas> {

        @Property(key = 0)
        DCanvas rectanglesAndCircles();

        @Property(key = 1)
        DCanvas triangles();

        @Property(containment)
        Set<MappingData> mappings();

        @Rule
        default void mapFromTriangles() {
            set(this, ExampleMapping1::mappings, triangles().shapes().map(r -> dclare(MappingData.class, ((DUUObject) r).uuid())).toSet());
        }

        @Rule
        default void mapFromRectangles() {
            set(this, ExampleMapping1::mappings, rectanglesAndCircles().shapes().filter(DRectangle.class).map(r -> dclare(MappingData.class, ((DUUObject) r).uuid())).toSet());
        }

        @Rule
        default void mapToTriangles() {
            set(triangles(), DCanvas::shapes, mappings().map(MappingData::triangle).toList());
        }

        @Rule
        default void mapToRectangles() {
            List<DShape> circlesAndLines = rectanglesAndCircles().shapes().filter(s -> !(s instanceof DRectangle)).toList();
            set(rectanglesAndCircles(), DCanvas::shapes, circlesAndLines.addAll(mappings().map(MappingData::rectangle)));
        }
    }

    interface MappingData extends DObject, DUUObject {

        @Property(constant)
        default DRectangle rectangle() {
            return dclareUU(DRectangle.class, uuid());
        }

        @Property(constant)
        default DTriangle triangle() {
            DTriangle t = dclareUU(DTriangle.class, uuid(), //
                    rule(DTriangle::position, x -> {
                        DFilled f = rectangle();
                        if (f.dragging() != null) {
                            DPoint delta = f.position().minus(pre(f, DShape::position));
                            DPoint p = pre(x, DShape::position);
                            return p.plus(delta);
                        }
                        return x.position();
                    }), //
                    rule(DTriangle::highlighted, x -> rectangle().selected()), //
                    rule(DTriangle::radius, x -> {
                        DCanvas c = dAncestor(ExampleMapping1.class).rectanglesAndCircles();
                        long nr = c.shapes().filter(DLine.class).filter(l -> l.position().equals(rectangle().centre()) || l.endPoint().equals(rectangle().centre())).count();
                        return (int) (50 + nr * 10);
                    }));
            //mag niet? set(t, DTriangle::position, rectangle().position());
            return t;
        }
    }

}
