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

package org.modelingvalue.jdclare.syntax.regex;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.Property;

public interface DPattern extends DStruct1<String> {

    @Property(key = 0)
    String regex();

    @Property(constant)
    default Pattern pattern() {
        return Pattern.compile(regex());
    }

    default DMatcher matcher(CharSequence input) {
        return dclare(DMatcher.class, this, input);
    }

    default DMatch match(CharSequence input, int i) {
        Matcher matcher = pattern().matcher(input);
        if (matcher.find()) {
            if (matcher.start() > 0) {
                return dclare(DMatch.class, 0, input.subSequence(0, matcher.start()).toString(), i);
            } else {
                return dclare(DMatch.class, 0, matcher.group(), i);
            }
        } else {
            return dclare(DMatch.class, 0, input.toString(), i);
        }
    }

    @Property(constant)
    default int groupCount() {
        return pattern().matcher("").groupCount();
    }

}
