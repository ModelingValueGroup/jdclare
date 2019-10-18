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

package org.modelingvalue.jdclare.swing.examples.sudoku;

import static org.modelingvalue.jdclare.DClare.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.Table;

public interface SudokuTable extends Table<Row, Column, Character>, DStruct2<SudokuUniverse, Sudoku> {

    List<Character> VALUES = Sudoku.VALUES.prepend(' ');

    @Property(key = 0)
    SudokuUniverse univers();

    @Property(key = 1)
    Sudoku sudoku();

    @Override
    default List<Row> rowObjects() {
        return sudoku().rows();
    }

    @Override
    default List<Column> columnObjects() {
        return sudoku().columns();
    }

    @Override
    @Default
    default boolean isEditable(Row row, Column column) {
        return true;
    }

    @Override
    default Character value(Row row, Column column) {
        return sudoku().cell(row, column).value();
    }

    @Override
    default void setValue(Row row, Column column, Character value) {
        set(sudoku().cell(row, column), Cell::value, value == ' ' ? null : value);
    }

    @Override
    default Class<Character> type(Row row, Column column) {
        return Character.class;
    }

    @Override
    default List<Character> scope(Row row, Column column) {
        return VALUES;
    }

}
