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

package org.modelingvalue.jdclare.workbench;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import org.modelingvalue.collections.List;
import org.modelingvalue.collections.Map;
import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DProblem;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.DComponent;
import org.modelingvalue.jdclare.swing.DMenu;
import org.modelingvalue.jdclare.swing.DMenubar;
import org.modelingvalue.jdclare.swing.Frame;
import org.modelingvalue.jdclare.swing.GuiUniverse;
import org.modelingvalue.jdclare.swing.Panel;
import org.modelingvalue.jdclare.swing.draw2d.DDimension;
import org.modelingvalue.jdclare.swing.draw2d.DPoint;
import org.modelingvalue.jdclare.syntax.Grammar.ObjectNode;
import org.modelingvalue.jdclare.syntax.Token;
import org.modelingvalue.jdclare.syntax.test.TextUniverse;

public interface WBUniverse extends GuiUniverse, TextUniverse {

    @Override
    default void init() {
        GuiUniverse.super.init();
        set(this, GuiUniverse::frames, Set.of(dclare(WBFrame.class, this)));
    }

    @Property(optional)
    DObject selected();

    @Property(optional)
    DProblem selectedProblem();

    @Property
    default Set<Token> problemTokens() {
        return dAllProblems().map(DProblem::context).flatMap(c -> c instanceof Token ? Set.of((Token) c) : //
                c instanceof ObjectNode ? Set.of(((ObjectNode) c).sParserNode().firstTerminal().token()) : Set.of()).toSet();
    }

    interface WBFrame extends Frame, DStruct1<WBUniverse> {
        @Property(key = 0)
        WBUniverse wb();

        @Override
        @Property(constant)
        default DComponent contentPane() {
            return dclare(WBPanel.class, wb());
        }

        @Override
        default DMenubar menubar() {
            return dclare(WBMenubar.class, this);
        }

        @Override
        @Default
        @Property
        default DPoint location() {
            return dclare(DPoint.class, 50.0, 50.0);
        }

        @Override
        @Default
        @Property
        default DDimension preferredSize() {
            return dclare(DDimension.class, 1300.0, 900.0);
        }
    }

    interface WBMenubar extends DMenubar {

        @Override
        default List<DMenu> menus() {
            return List.of(dclare(DMenu.class, this, "File"));
        }
    }

    interface WBPanel extends Panel, DStruct1<WBUniverse> {
        @Property(key = 0)
        WBUniverse wb();

        @Override
        @Property(constant)
        default Map<DComponent, Object> content() {
            return Map.<DComponent, Object> of()//
                    .put(dclare(WBOuterSplitPane.class, wb()), BorderLayout.CENTER);
        }

        @Override
        default LayoutManager layoutManager() {
            return new BorderLayout();
        }
    }

    static void main(String[] args) {
        start(WBUniverse.class, false).waitForEnd();
    }

}
