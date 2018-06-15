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

package org.modelingvalue.jdclare.swing;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.swing.DTextArea.TextAreaNative;
import org.modelingvalue.transactions.Compound;

@Native(TextAreaNative.class)
public interface DTextArea extends DTextComponent {

    class TextAreaNative extends TextComponentNative<DTextArea, JTextArea> {
        public TextAreaNative(DTextArea visible) {
            super(visible);
        }

        @Override
        public void init(DObject parent, Compound tx) {
            swing = new JTextArea();
            swing.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            super.init(parent, tx);
        }
    }

}
