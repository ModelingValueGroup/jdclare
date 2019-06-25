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

package org.modelingvalue.jdclare.test;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DClare;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.Property;

public interface Scrum extends DUniverse {

    @Property(containment)
    Set<Team> teams();

    @Property({containment, optional})
    Set<Company> company();

    default void initTest(DClare<Scrum> dClare) {
        Company mvg = dclareUU(Company.class, set(DNamed::name, "Modeling Value Group B.V."));
        Team dclare = dclareUU(Team.class, set(DNamed::name, "DClare"));
        Person arjan = dclareUU(Person.class, set(DNamed::name, "Arjan Kok"));
        Person wim = dclareUU(Person.class, set(DNamed::name, "Wim Bast"));

        set(dClare.universe(), Scrum::company, Set.of(mvg));
        set(dClare.universe(), Scrum::teams, Set.of(dclare));
        set(mvg, Company::employees, Set.of(arjan, wim));
        set(dclare, Team::company, mvg);
        set(dclare, Team::scrumMaster, wim);
        set(dclare, Team::productOwner, wim);
        set(dclare, Team::developers, Set.of(wim));
    }

    default void initScopeProblem(DClare<Scrum> dClare) {
        Company mvg = dclareUU(Company.class, set(DNamed::name, "Modeling Value Group B.V."));
        Company abc = dclareUU(Company.class, set(DNamed::name, "ABC"));
        Team dclare = dclareUU(Team.class, set(DNamed::name, "DClare"));
        Person pieterpuk = dclareUU(Person.class, set(DNamed::name, "Pieter Puk"));

        //Person arjan = dclareUU(Person.class, set(DNamed::name, "Arjan Kok"));
        //Person wim = dclareUU(Person.class, set(DNamed::name, "Wim Bast"));
        //set(mvg, Company::employees, Set.of(arjan, wim));

        set(dClare.universe(), Scrum::company, Set.of(mvg, abc));
        set(dClare.universe(), Scrum::teams, Set.of(dclare));
        set(abc, Company::employees, Set.of(pieterpuk));

        //set(dclare, Team::company, mvg);
        set(dclare, Team::developers, Set.of(pieterpuk));
    }
}
