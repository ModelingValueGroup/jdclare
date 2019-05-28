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

package org.modelingvalue.jdclare.examples;

import static org.modelingvalue.jdclare.DClare.*;
import static org.modelingvalue.jdclare.PropertyQualifier.*;

import org.modelingvalue.collections.Set;
import org.modelingvalue.jdclare.DNamed;
import org.modelingvalue.jdclare.DObject;
import org.modelingvalue.jdclare.DStruct1;
import org.modelingvalue.jdclare.DStruct2;
import org.modelingvalue.jdclare.DUniverse;
import org.modelingvalue.jdclare.IOString;
import org.modelingvalue.jdclare.Property;
import org.modelingvalue.jdclare.Rule;

public interface SalesUniverse extends DUniverse {

    @Property(containment)
    Set<Customer> customers();

    @Property(containment)
    Set<Product> products();

    @Property(containment)
    Set<Order> orders();

    interface Customer extends DStruct1<String>, DNamed {
        @Override
        @Property(key = 0)
        String name();
    }

    interface Product extends DStruct1<String>, DNamed {
        @Override
        @Property(key = 0)
        String name();

        @Property
        float price();
    }

    interface Order extends DStruct2<Customer, Integer>, DObject {
        @Property(key = 0)
        Customer customer();

        @Property(key = 1)
        int nr();

        @Property(containment)
        Set<OrderLine> lines();

        @Property
        default float price() {
            return lines().map(OrderLine::price).reduce(0.0f, Float::sum);
        }
    }

    interface OrderLine extends DStruct2<Order, Integer>, DObject {
        @Property(key = 0)
        Order order();

        @Property(key = 1)
        int nr();

        @Property
        int amount();

        @Property
        Product product();

        @Property
        default float price() {
            return amount() * product().price();
        }

    }

    // Code for testing only

    @Override
    default IOString output() {
        return IOString.ofln(orders().reduce("", (s, o) -> o.customer() + "=" + o.price(), (a, b) -> a + " " + b));
    }

    @Rule
    default void readInput() {
        String input = input().string();
        if (input.equals("stop")) {
            set(this, DUniverse::stop, true);
        } else if (!input.isEmpty()) {
            try {
                set(dclare(Product.class, "Bird"), Product::price, Float.parseFloat(input));
            } catch (NumberFormatException nfe) {
                set(this, DUniverse::error, IOString.ofln("Only amounts or 'stop' allowed"));
            }
        }
        set(this, DUniverse::input, IOString.of(""));
    }

    @Override
    default void init() {
        DUniverse.super.init();
        set(this, SalesUniverse::customers, Set.of(//
                dclare(Customer.class, "John"), //
                dclare(Customer.class, "Bill"))); //
        set(this, SalesUniverse::products, Set.of(//
                dclare(Product.class, "Car", set(Product::price, 30000.0f)), //
                dclare(Product.class, "Bird", set(Product::price, 300.0f)), //
                dclare(Product.class, "TV", set(Product::price, 1000.0f)))); //
        set(this, SalesUniverse::orders, Set.of(//
                dclare(Order.class, dclare(Customer.class, "John"), 1, o -> set(o, Order::lines, Set.of(//
                        dclare(OrderLine.class, o, 1, set(OrderLine::amount, 2), set(OrderLine::product, dclare(Product.class, "Car"))), //
                        dclare(OrderLine.class, o, 3, set(OrderLine::amount, 1), set(OrderLine::product, dclare(Product.class, "TV")))))), //
                dclare(Order.class, dclare(Customer.class, "Bill"), 1, o -> set(o, Order::lines, Set.of(//
                        dclare(OrderLine.class, o, 1, set(OrderLine::amount, 1), set(OrderLine::product, dclare(Product.class, "Car"))), //
                        dclare(OrderLine.class, o, 2, set(OrderLine::amount, 3), set(OrderLine::product, dclare(Product.class, "Bird"))), //
                        dclare(OrderLine.class, o, 3, set(OrderLine::amount, 2), set(OrderLine::product, dclare(Product.class, "TV"))))))));
    }

    static void main(String[] args) {
        runAndRead(SalesUniverse.class);
    }

}
