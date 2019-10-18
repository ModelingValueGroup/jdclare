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
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DUUObject;
import org.modelingvalue.jdclare.Property;

public interface Sudoku extends DObject, DUUObject {

    int             SIZE     = 3;
    List<Integer>   INTEGERS = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8);
    List<Character> VALUES   = List.of('1', '2', '3', '4', '5', '6', '7', '8', '9');

    @Property({containment, constant})
    default List<Row> rows() {
        return INTEGERS.map(i -> dclare(Row.class, this, i)).toList();
    }

    @Property({containment, constant})
    default List<Column> columns() {
        return INTEGERS.map(i -> dclare(Column.class, this, i)).toList();
    }

    @Property({containment, constant})
    default List<Block> blocks() {
        return INTEGERS.map(i -> dclare(Block.class, this, i)).toList();
    }

    @Property({containment, constant})
    default List<Cell> cells() {
        return INTEGERS.flatMap(r -> INTEGERS.map(c -> dclare(Cell.class, this, rows().get(r), columns().get(c), blocks().get((r % SIZE) + (c % SIZE) * SIZE)))).toList();
    }

    default Cell cell(Row row, Column column) {
        return cells().get(row.index() + column.index() * Sudoku.SIZE * Sudoku.SIZE);
    }

}
