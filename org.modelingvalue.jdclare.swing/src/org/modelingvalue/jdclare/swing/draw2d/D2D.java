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
import java.awt.Graphics;
import java.awt.Polygon;

public class D2D {

    public static void drawRect(Graphics g, int x, int y, int w, int h, Color cFill, Color cLine, String text) {
        g.setColor(cFill);
        g.fillRect(x, y, w, h);
        g.setColor(cLine);
        g.drawRect(x, y, w, h);
        if (text != null) {
            g.setColor(Color.BLACK);
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x + w / 2 - tw / 2, y + h / 2);
        }
    }

    public static void drawOval(Graphics g, int x, int y, int w, int h, Color cFill, Color cLine, String text) {
        g.setColor(cFill);
        g.fillOval(x, y, w, h);
        g.setColor(cLine);
        g.drawOval(x, y, w, h);
        if (text != null) {
            g.setColor(Color.BLACK);
            int tw = g.getFontMetrics().stringWidth(text);
            g.drawString(text, x + w / 2 - tw / 2, y + h / 2);
        }
    }

    public static void drawLine(Graphics g, Color cLine, int... xy) {
        g.setColor(cLine);
        int[] xs = new int[xy.length / 2], ys = new int[xy.length / 2];
        for (int i = 0; i < xs.length; i++) {
            xs[i] = xy[i * 2];
            ys[i] = xy[i * 2 + 1];
        }
        g.drawPolyline(xs, ys, xs.length);
    }

    public static void drawPoly(Graphics g, Color cFill, Color cLine, String text, int... xy) {
        Polygon poly = new Polygon();
        Integer x = null;
        for (int i : xy) {
            if (x == null) {
                x = i;
            } else {
                poly.addPoint(x, i);
                x = null;
            }
        }
        g.setColor(cFill);
        g.fillPolygon(poly);
        g.setColor(cLine);
        g.drawPolygon(poly);
        if (text != null) {
            g.setColor(Color.BLACK);
            g.drawString(text, xy[0], xy[1]);
        }
    }

}
