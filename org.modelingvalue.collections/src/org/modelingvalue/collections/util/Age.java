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

package org.modelingvalue.collections.util;

// @SuppressWarnings("restriction")
public final class Age {

    // private static sun.misc.Unsafe UNSAFE = null;

    //    static {
    //        try {
    //            Field declaredField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
    //            declaredField.setAccessible(true);
    //            UNSAFE = (sun.misc.Unsafe) declaredField.get(null);
    //        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
    //            e.printStackTrace();
    //        }
    //    }

    public static final int age(Object object) {
        return 0; // (UNSAFE.getByte(object, 0l) & 0x78) >> 3;
    }

    private Age() {

    }

}
