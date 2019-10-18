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

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.List;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.Default;
import org.modelingvalue.jdclare.Native;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.swing.Table.TableNative;

@Native(TableNative.class)
public interface Table<R, C, V> extends DComponent {

    @Property(containment)
    default List<Row<R, C, V>> rows() {
        List<R> rowObjects = rowObjects();
        return Collection.range(0, rowObjects.size()).map(i -> row(rowObjects.get(i), i)).toList();
    }

    @Property(containment)
    default List<Column<R, C, V>> columns() {
        List<C> columnObjects = columnObjects();
        return Collection.range(0, columnObjects.size()).map(i -> column(columnObjects.get(i), i)).toList();
    }

    List<R> rowObjects();

    List<C> columnObjects();

    V value(R row, C column);

    @Default
    default boolean isEditable(R row, C column) {
        return false;
    }

    void setValue(R row, C column, V value);

    Class<V> type(R row, C column);

    List<V> scope(R row, C column);

    @Property(optional)
    Row<R, C, V> selectedRow();

    default int preferredWidth(C column) {
        return 100 / columns().size();
    }

    default int preferredHeight(R row) {
        return 100 / rows().size();
    }

    @SuppressWarnings("unchecked")
    default Row<R, C, V> row(R object, int i) {
        return dclare(Row.class, this, object, i);
    }

    @SuppressWarnings("unchecked")
    default Column<R, C, V> column(C object, int i) {
        return dclare(Column.class, this, object, i);
    }

    @SuppressWarnings("unchecked")
    default Cell<R, C, V> cell(Row<R, C, V> row, Column<R, C, V> column) {
        return dclare(Cell.class, this, row, column);
    }

    class TableNative<R, C, V> extends DComponentNative<Table<R, C, V>, JTable> implements ListSelectionListener {

        private ListSelectionModel selectionModel;

        public TableNative(Table<R, C, V> visible) {
            super(visible);
        }

        @SuppressWarnings("serial")
        @Override
        public void init(DObject parent) {
            DTableModel<R, C, V> model = new DTableModel<R, C, V>(visible);
            swing = new JTable(model) {
                @SuppressWarnings({"rawtypes", "unchecked"})
                @Override
                public TableCellEditor getCellEditor(int row, int column) {
                    R r = visible.rows().get(row).object();
                    C c = visible.columns().get(column).object();
                    List<V> scope = visible.scope(r, c);
                    if (scope != null) {
                        return new DefaultCellEditor(new JComboBox(scope.toArray()));
                    } else {
                        return getDefaultEditor(visible.type(r, c));
                    }
                }

                @Override
                public String getToolTipText(MouseEvent e) {
                    Point p = e.getPoint();
                    int rowIndex = rowAtPoint(p);
                    int colIndex = columnAtPoint(p);
                    Object val = model.getValueAt(rowIndex, colIndex);
                    return val != null ? val.toString() : null;
                }
            };
            selectionModel = swing.getSelectionModel();
            selectionModel.addListSelectionListener(this);
            super.init(parent);
        }

        @Override
        public void exit(DObject parent) {
            super.exit(parent);
            selectionModel.removeListSelectionListener(this);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                int selected = selectionModel.getLeadSelectionIndex();
                Object value = selected >= 0 ? visible.rows().get(selected) : null;
                set(Table::selectedRow, value);
            }
        }

        public void rows(List<Row<R, C, V>> pre, List<Row<R, C, V>> post) {
            ((AbstractTableModel) swing.getModel()).fireTableStructureChanged();
            resetWidths();
        }

        public void columns(List<Column<R, C, V>> pre, List<Column<R, C, V>> post) {
            ((AbstractTableModel) swing.getModel()).fireTableStructureChanged();
            resetWidths();
        }

        private void resetWidths() {
            SwingUtilities.invokeLater(() -> {
                int wTotal = swing.getWidth();
                int hTtotal = swing.getHeight();
                if (wTotal > 0 && hTtotal > 0) {
                    TableColumnModel cModel = swing.getColumnModel();
                    int i = 0;
                    for (Column<R, C, V> c : visible.columns()) {
                        int width = wTotal * visible.preferredWidth(c.object()) / 100;
                        cModel.getColumn(i++).setPreferredWidth(width);
                    }
                    i = 0;
                    for (Row<R, C, V> r : visible.rows()) {
                        int width = hTtotal * visible.preferredHeight(r.object()) / 100;
                        swing.setRowHeight(i++, width);
                    }
                }
            });
        }

        @SuppressWarnings("serial")
        private static final class DTableModel<R, C, V> extends AbstractTableModel {

            private final Table<R, C, V> table;

            private DTableModel(Table<R, C, V> table) {
                this.table = table;
            }

            @Override
            public String getColumnName(int column) {
                return table.columns().get(column).object().toString();
            }

            @Override
            public int getRowCount() {
                return table.rows().size();
            }

            @Override
            public int getColumnCount() {
                return table.columns().size();
            }

            @Override
            public Object getValueAt(int row, int column) {
                return table.rows().get(row).cells().get(column).value();
            }

            @SuppressWarnings("unchecked")
            @Override
            public void setValueAt(Object value, int row, int column) {
                table.rows().get(row).cells().get(column).setValue((V) value);
                fireTableCellUpdated(row, column);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return table.isEditable(table.rows().get(row).object(), table.columns().get(column).object());
            }

        }

    }

}
