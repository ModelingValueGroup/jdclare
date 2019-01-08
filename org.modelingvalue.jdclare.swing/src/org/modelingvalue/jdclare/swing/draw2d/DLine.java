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

import java.awt.Color;
import java.awt.Graphics2D;

import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent.DComponentNative;
import org.modelingvalue.jdclare.swing.draw2d.DLine.LineNative;

@Native(LineNative.class)
public interface DLine extends DShape {

    @Default
    @Property
    default DPoint endPoint() {
        return DPoint.NULL;
    }

    @Override
    default boolean hit(DPoint pt) {
        DPoint start = position();
        DPoint end = endPoint();
        double lowx = start.x() < end.x() ? start.x() : end.x();
        double highx = start.x() < end.x() ? end.x() : start.x();

        double lowy = start.y() < end.y() ? start.y() : end.y();
        double highy = start.y() < end.y() ? end.y() : start.y();

        return pt.x() > lowx && pt.x() < highx && pt.y() > lowy && pt.y() < highy && //
                end.minus(start).hasEqualAngle(end.minus(pt));
    }

    @Override
    default DPoint centre() {
        DPoint pos = position();
        return pos.plus(endPoint().minus(pos).div(2.0));
    }

    class LineNative extends ShapeNative<DLine> {

        public LineNative(DLine visible) {
            super(visible);
        }

        @Override
        public void paint(Graphics2D g) {
            DPoint pt = visible.position();
            DPoint end = visible.endPoint();
            Color lc = visible.lineColor();
            D2D.drawLine(g, lc, (int) pt.x(), (int) pt.y(), (int) end.x(), (int) end.y());
        }

        public void endPoint(DPoint pre, DPoint post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

    }

}
