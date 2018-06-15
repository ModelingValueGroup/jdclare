JDclare
=====================================

What is (J)Dclare?
----------------

Dclare is a programming language in which you can specify programs declaratively. That means that you describe *what* you want, not *when* the computer should execute it.
A spreadsheet application (like Excel) is also declaratively defined. You define how the values are derived by means of expressions in the cells. However, where spreadsheets have a very limited structure (rows, columns and cells), Dclare has a full Object-Oriented type system enhanced with common modeling concepts. Dclare is designed to support the development of arbitrary applications including gui applications, rule-engines and bi-directional transformations. 

In Dclare you specify classes with properties and rules. Properties can be of any type, including references and containments of classes. Rules define the values of the properties. Rules will automatically be run when necessary, no subscriptions nor invocations of rules are needed. If a value of a property changes, then other properties of objects will possibly change in such a way that all rules will 'hold'. A Rule 'holds' if, when executed, no changes will take place. Dclare will only run those rules that are possibly effected by a change, hence it works incrementally. Rules may be defined circular (multi-directional) as long as the rules terminate and are not in conflict with each other. Since you can define rules on containment properties you can also define the existence of (hence, create and delete) objects. 

JDclare is an implementation of Dclare where you can specify your application in pure Java (version 9 and beyond). The properties and rules are defined in interfaces using methods with annotations to give those methods the Dclare specific meaning. 

Drivers can be made to integrate native Java libraries in JDclare. A limited set of drivers for the Java Swing gui library is included.

Structure
---------

Dclare uses its own collection framework 'org.modelingvalue.collections'. This collections framework supports non-mutable collections and hence is not a implementation of the standard java.util.* collection interfaces. All collections do however directly implement the Stream interface.

The transactions library 'org.modelingvalue.transactions' is the implementation of the reactive in-memory transactions framework. It depends on the collection framework.

All packages starting with 'org.modelingvalue.jdclare' are JDclare specific, and hence work with the property and rule annotations. 

License
-------

JDclare is released under the terms of the GNU LGPLv3 license. See <https://www.gnu.org/licenses/lgpl.html> for more
information.

Development Process
-------------------

The `master` branch is regularly built and tested, but is not guaranteed to be completely stable. 

There is no contribution workflow described yet. However, feel free to make a pull-request.

JDclare is very much work in progress. No backwards compatibility is guaranteed at this time. 

Examples
-------------------

The best way to learn a language is to go through some examples. You can run these examples yourself by importing the projects in your favorite IDE. Note that JDclare requires Java 9 or higher. 

*Example 1: HelloUniverse* [HelloUniverse](https://github.com/ModelingValueGroup/jdclare/blob/master/org.modelingvalue.jdclare.examples/src/org/modelingvalue/jdclare/examples/HelloUniverse.java)

	package org.modelingvalue.jdclare.examples;
	
	import static org.modelingvalue.jdclare.DClare.*;
	
	import org.modelingvalue.jdclare.DUniverse;
	import org.modelingvalue.jdclare.IOString;
	
	public interface HelloUniverse extends DUniverse {
	
	    static void main(String[] args) {
	        runAndStop(HelloUniverse.class);
	    }
	
	    @Override
	    default IOString output() {
	        return IOString.of("Hello Universe (counting " + dSize() + " objects) ");
	    }
	
	}

##### Why do you have to specify an Universe?

In JDclare you specify rules over a set of related objects. One particular kind of relation is define in containment properties. In JDclare all objects have to be contained (direct or indirect) by the Universe. The only exception is the Universe itself, the Universe is the only object that is not contained by another object. 
So, if you make an JDclare application you extend the standard Universe (DUniverse). The standard Universe contains all objects of JDclare itself. And in your extension of the standard Universe you can add the objects for your application.  


##### Why do you specify DUniverse in stead of Universe?

All interfaces starting with the letter 'D' are predefined interfaces part of the JDclare library and you can use or extend them in your application


##### What is the meaning of the main method? Do we still have to specify a process?

JDclare makes it possible to declaratively specify a program in Java. JDclare has to start somewhere, in Java this is done by providing a static void main method. The only thing you have to do here is to instantiate your Universe, in this case the HelloUniverse. This is done by the predefined runAndStop method in the JDclare library. This method will instantiate the HelloUniverse, makes sure that all rules hold, and then stop again.


##### How do you define a rule in JDclare? 

In JDclare you define a rule by constraining a property. A property is defined by a method on an interface without arguments and a return type and the method (or the method in the super class) has a @Property annotation : the name of the property is the name of the method, and the type of the property is the return type of the method. In the example there is a property named output with type IOString. The body of the method defines the rule that describes the invariant : something that is always holds. In this example the rule sets the string "Hello Universe ..." to the output property. The @Override indicates that the property output already exists in the DUniverse.
You can also define a rule outside a property - then you have to give the method a @Rule annotation.

##### Where does dSize() come from?

dSize() is a property defined on DObject and because DUniverse extends from DObject the property is also defined on DUniverse. All objects that have state that can be changed must be instances of a Class that extends from DObject. The dSize() is a property with a rule that calculates the number of contained objects + 1. All other objects in Dclare are immutable: they typically inherit from DStruct. All collections in JDclare are also immutable.


*Example 2: EchoUniverse* [EchoUniverse](https://github.com/ModelingValueGroup/jdclare/blob/master/org.modelingvalue.jdclare.examples/src/org/modelingvalue/jdclare/examples/EchoUniverse.java)

	package org.modelingvalue.jdclare.examples;
	
	import static org.modelingvalue.jdclare.DClare.*;
	
	import org.modelingvalue.jdclare.DUniverse;
	import org.modelingvalue.jdclare.IOString;
	
	public interface EchoUniverse extends DUniverse {
	
	    static void main(String[] args) {
	        runAndRead(EchoUniverse.class);
	    }
	
	    @Override
	    default IOString output() {
	        String input = input().string();
	        if (input.equals("stop")) {
	            set(this, DUniverse::stop, true);
	            return IOString.of("Goodbye");
	        } else {
	            return input.isEmpty() ? //
	                    IOString.of("Hello" + System.lineSeparator() + "> ") : //
	                    IOString.of(input + System.lineSeparator() + "> ");
	        }
	    }
	
	}

##### What does runAndRead do?

Run instantiates all objects in the Universe as in the previous example. Read listens for input. So the application does not stop.

##### What does the output property do?

The input is read, and is translated to the output. Note that input() is also a property defined on DUniverse. The property input() is read, and if you type "stop" the example changes the state of the DUniverse by setting the property stop to the value "true", and the example adds "Goodbye" to the output property. Setting the property stop to "true" will stop the JDclare engine. If you type something else then "stop" then the example will add it to the output. 
Note that the example uses the type String. In JDCLare you can use all Java types that are immutable (like String in this example). 

##### Who triggers the body of the output property (= rule) ?

JDclare has a reactive engine, it means that the system will react automatically on state changes. So when the input property changes by an event - in this case because a user types something - then the engine will try to make the system consistent again by running a minimal set of rules. 

*Example 3: SalesUniverse* [SalesUniverse](https://github.com/ModelingValueGroup/jdclare/blob/master/org.modelingvalue.jdclare.examples/src/org/modelingvalue/jdclare/examples/SalesUniverse.java)

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
	        if (!input.isEmpty()) {
	            if (input.equals("stop")) {
	                set(this, DUniverse::stop, true);
	            } else {
	                try {
	                    set(dclare(Product.class, "Bird"), Product::price, Float.parseFloat(input));
	                } catch (NumberFormatException nfe) {
	                    set(this, DUniverse::error, IOString.ofln("Only amounts or 'stop' allowed"));
	                }
	            }
	        }
	    }
	
	    @Override
	    default void init() {
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

##### Next to a main method, there is also an init() method. What is the purpose of the init() method?

The init() method is a method on DUniverse that can be overridden to initialize your world : it defines your starting state. This init() method for example 'dclares' two Customers named "John" and "Bill" and adds these customers to the SalesUniverse by setting the property SalesUniverse::customers of SalesUniverse using the set method. With 'dclare' you declare an object together with its key attributes or its identity. Everything in JDclare has an explicit identity. The identity of an object is defined by the keys and the type of the object. Next to Customers, there are also Products, Orders and Orderlines declared in the init method. Note that declaring an object or identity is different from creating an object : dclaring a second object with the same identify as an other object will have no effect.

##### How do you define for example a Product and a set of Products?

The Product in this example has two properties : A property name of type String, and a property price of type float. The Product is identified by one key property of type String because it inherits from DStruct1. To access this key property the example defines a property with keyword 'key = <number>'. The number corresponds the sequence number in the DStruct tuple. The product also extends from Named which extends from DObject. Named is only a convenience interface that implements toString() for example. In this case the Product has a name that is a key. Because the name is a defined as a key, the name cannot be changed and is a constant. This SalesUniverse contains a set of Products, this is done by adding a property products.


##### What do the annotations @property, @rule and @default mean?

With the @property annotation you define a property. With the @rule annotation you define a rule (or constraint, or invariant) : in this example there is a rule readInput defined. And with the @default annotation you define that the body of the property is the initial value of this property if this value is not set by something else. The property annotation may have additional attributes. The containment attribute specifies that the property is a containment. In JDclare every object that inherits from DObject has to be (direct or indirect) contained by the Universe, and this is done by adding containment relations. The mandatory attribute specifies if property is mandatory or optional. A property which has a multiplicity of one is by default mandatory, but properties which have a multiplicity of zero or more (e.g. Set) are by default non-mandatory or optional. 

Note that everything defined without these annotations is just plain Java. 

##### What does readInput() do ?

ReadInput is a rule that reads the input property, and depending on the input changes the state of the application : it sets the stop property of the DUniverse to true, or sets the price of the Bird, or adds a string to the error property. Because of the reactive engine the system will react automatically on state changes. So if the price of the Bird changes, then the order of Bill changes as well because Bill has ordered Birds. The order of John will not change.  

##### What does output() do ?

In JDclare you can specify the rule over a collection  : every collection is also a stream in the used collection framework [MVG collections](https://github.com/ModelingValueGroup/jdclare/tree/master/org.modelingvalue.collections) so you can use for instance map and reduce directly on the collection. In this example, the orders collection is reduced to show the total price of the order for all customers.

*Example 4: CyclicUniverse* [CyclicUniverse](https://github.com/ModelingValueGroup/jdclare/blob/master/org.modelingvalue.jdclare.examples/src/org/modelingvalue/jdclare/examples/CyclicUniverse.java)

	package org.modelingvalue.jdclare.examples;
	
	import static org.modelingvalue.jdclare.DClare.*;
	
	import org.modelingvalue.jdclare.DUniverse;
	import org.modelingvalue.jdclare.IOString;
	import org.modelingvalue.jdclare.Property;
	import org.modelingvalue.jdclare.Rule;
	
	public interface CyclicUniverse extends DUniverse {
	
	    static void main(String[] args) {
	        runAndRead(CyclicUniverse.class);
	    }
	
	    @Property
	    default int a() {
	        return b();
	    }
	
	    @Property
	    default int b() {
	        return a();
	    }
	
	    @Override
	    default IOString output() {
	        return IOString.of("a=" + a() + " b=" + b() + System.lineSeparator() + "> ");
	    }
	
	    @Rule
	    default void readInput() {
	        String input = input().string().replaceAll("\\s+", "");
	        if (input.equals("stop")) {
	            set(this, DUniverse::stop, true);
	        } else if (input.startsWith("a=")) {
	            try {
	                set(this, CyclicUniverse::a, Integer.parseInt(input.substring(2)));
	            } catch (NumberFormatException nfe) {
	                set(this, DUniverse::error, IOString.ofln("Only integers after 'a=' allowed"));
	            }
	        } else if (input.startsWith("b=")) {
	            try {
	                set(this, CyclicUniverse::b, Integer.parseInt(input.substring(2)));
	            } catch (NumberFormatException nfe) {
	                set(this, DUniverse::error, IOString.ofln("Only integers after 'b=' allowed"));
	            }
	        } else if (!input.isEmpty()) {
	            set(this, DUniverse::error, IOString.ofln("Only 'stop', 'a=<int>' or 'b=<int>'"));
	        }
	    }
	
	}

##### Why does this example work? JDclare could also reset "a" to its original value if you change "a"?

This is because JDclare uses a "push out" strategy to prioritize rules. If a property value has changed, then all rules are executed that or have read this property, or have written this property. However, the JDclare engine first tries to get to a consistent state by first running the rules that have read the property. Rules that are triggered because of a change in the value of a property they read are triggered before rules that triggered because of a change of a property they wrote.
In this example, if you change "a" the rule for b() is has higher priority, so property "b" will get the new value for "a", and because "b" changes, also "a" will get the new value.




