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

import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent.DComponentNative;
import org.modelingvalue.jdclare.swing.draw2d.DRectangle.RectangleNative;

@Native(RectangleNative.class)
public interface DRectangle extends DFilled {

    @Default
    @Property
    default DDimension size() {
        return dclare(DDimension.class, 100.0, 100.0);
    }

    @Override
    default boolean hit(DPoint pt) {
        DPoint pos = position();
        DDimension siz = size();
        return pt.x() > pos.x() && pt.x() < pos.x() + siz.width() && pt.y() > pos.y() && pt.y() < pos.y() + siz.height();
    }

    @Override
    default DPoint centre() {
        return position().plus(size().toPoint().div(2.0));
    }

    class RectangleNative extends FilledNative<DRectangle> {

        public RectangleNative(DRectangle visible) {
            super(visible);
        }

        @Override
        public void paint(Graphics2D g) {
            DPoint pt = visible.position();
            DDimension sz = visible.size();
            Color fc = visible.color();
            Color lc = visible.lineColor();
            String str = visible.text();
            D2D.drawRect(g, (int) pt.x(), (int) pt.y(), (int) sz.width(), (int) sz.height(), fc, lc, str);
        }

        public void size(DDimension pre, DDimension post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

    }

}
