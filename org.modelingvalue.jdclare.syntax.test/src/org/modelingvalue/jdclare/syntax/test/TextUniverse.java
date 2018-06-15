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

package org.modelingvalue.jdclare.syntax.test;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.UUID;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DStruct0;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;
import org.modelingvalue.jdclare.syntax.Text;
import org.modelingvalue.jdclare.syntax.test.simple.APackage;
import org.modelingvalue.jdclare.syntax.test.simple.APackageContainer;
import org.modelingvalue.jdclare.syntax.test.simple.AUniverse;

public interface TextUniverse extends AUniverse {

    UUID ID1 = UUID.randomUUID();
    UUID ID2 = UUID.randomUUID();

    @Rule
    default void initPackages() {
        if (aPackages().isEmpty()) {
            APackage p0 = dclare(APackage.class, ID1, set(DNamed::name, "jdclare"));
            set(this, APackageContainer::aPackages, Set.of(p0));
            APackage p1 = dclare(APackage.class, ID2, set(DNamed::name, "test"));
            set(p0, APackageContainer::aPackages, Set.of(p1));
        }
    }

    @Property({constant, containment})
    default MyText text() {
        return dclare(MyText.class, set(Text::syntaxClass, MySyntax.class), set(Text::string, inititalString()), set(Text::rootClass, MySyntax.Unit.class));
    }

    @Property({containment, optional})
    default MySyntax.Unit unit() {
        return text().root();
    }

    private String inititalString() {
        return "package jdclare.test;\n" + "\n" + "class ScrumTeam {\n" + "  String name = \"team\";\n" + "  Number experienceFactor = 3;\n" + "  ScrumMaster scrummaster;\n" + "  ScrumTeam me = scrummaster.team;\n" + "}\n" + "\n" + "class ScrumMaster {\n" + "  String name;\n" + "  Number numberOfCertificates = 1;\n" + "  ScrumTeam team;\n" + "  ScrumMaster me = team.scrummaster;\n" + "}";
    }

    interface MyText extends Text<MySyntax, MySyntax.Unit>, DStruct0 {
    }
}
