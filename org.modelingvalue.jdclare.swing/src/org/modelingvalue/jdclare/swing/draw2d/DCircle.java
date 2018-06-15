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
import org.modelingvalue.jdclare.swing.draw2d.DCircle.CircleNative;

@Native(CircleNative.class)
public interface DCircle extends DFilled {

    @Default
    @Property
    default int radius() {
        return 50;
    }

    @Override
    default boolean hit(DPoint pt) {
        return pt.minus(position()).length() < radius();
    }

    @Override
    default DPoint centre() {
        return position();
    }

    class CircleNative extends FilledNative<DCircle> {

        public CircleNative(DCircle visible) {
            super(visible);
        }

        @Override
        public void paint(Graphics2D g) {
            DPoint pt = visible.position();
            int rad = visible.radius();
            Color fc = visible.color();
            Color lc = visible.lineColor();
            String str = visible.text();
            D2D.drawOval(g, pt.x() - rad, pt.y() - rad, rad * 2, rad * 2, fc, lc, str);
        }

        public void radius(int pre, int post) {
            ancestor(DComponentNative.class).swing().repaint();
        }

    }

}
