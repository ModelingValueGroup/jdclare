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

package org.modelingvalue.jdclare;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

public final class JavaOperators {

    private JavaOperators() {
    }

    public static boolean isa(Object a, Class<?> b) {
        return b.isInstance(a);
    }

    @SuppressWarnings("unchecked")
    public static <T> T as(Object a, Class<T> b) {
        return (T) a;
    }

    @Operator("+")
    @Precedence(30)
    public static String concatenate(String a, String b) {
        return a + b;
    }

    @Operator("==")
    @Precedence(60)
    public static boolean equals(Object a, Object b) {
        return Objects.equals(a, b);
    }

    @Operator("!=")
    @Precedence(60)
    public static boolean notEquals(Object a, Object b) {
        return !Objects.equals(a, b);
    }

    @Operator("!")
    @Precedence(10)
    public static boolean not(boolean a) {
        return !a;
    }

    @Operator("&&")
    @Precedence(100)
    public static boolean and(boolean a, BooleanSupplier b) {
        return a && b.getAsBoolean();
    }

    @Operator("||")
    @Precedence(110)
    public static boolean or(boolean a, BooleanSupplier b) {
        return a || b.getAsBoolean();
    }

    @Operator({"?", ":"})
    @Precedence(120)
    public static <T> T ternary(boolean b, Supplier<T> yes, Supplier<T> no) {
        return b ? yes.get() : no.get();
    }

    // Byte

    @Operator("-")
    @Precedence(10)
    public static int minus(byte a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static int multiply(byte a, byte b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static int divide(byte a, byte b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static int remain(byte a, byte b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static int add(byte a, byte b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static int minus(byte a, byte b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(byte a, byte b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(byte a, byte b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(byte a, byte b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(byte a, byte b) {
        return a >= b;
    }

    // Short

    @Operator("-")
    @Precedence(10)
    public static int minus(short a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static int multiply(short a, short b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static int divide(short a, short b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static int remain(short a, short b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static int add(short a, short b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static int minus(short a, short b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(short a, short b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(short a, short b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(short a, short b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(short a, short b) {
        return a >= b;
    }

    // Integer

    @Operator("-")
    @Precedence(10)
    public static int minus(int a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static int multiply(int a, int b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static int divide(int a, int b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static int remain(int a, int b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static int add(int a, int b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static int minus(int a, int b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(int a, int b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(int a, int b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(int a, int b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(int a, int b) {
        return a >= b;
    }

    // Long

    @Operator("-")
    @Precedence(10)
    public static long minus(long a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static long multiply(long a, long b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static long divide(long a, long b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static long remain(long a, long b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static long add(long a, long b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static long minus(long a, long b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(long a, long b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(long a, long b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(long a, long b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(long a, long b) {
        return a >= b;
    }

    // Float

    @Operator("-")
    @Precedence(10)
    public static float minus(float a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static float multiply(float a, float b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static float divide(float a, float b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static float remain(float a, float b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static float add(float a, float b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static float minus(float a, float b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(float a, float b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(float a, float b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(float a, float b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(float a, float b) {
        return a >= b;
    }

    // Double

    @Operator("-")
    @Precedence(10)
    public static double minus(double a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static double multiply(double a, double b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static double divide(double a, double b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static double remain(double a, double b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static double add(double a, double b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static double minus(double a, double b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(double a, double b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(double a, double b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(double a, double b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(double a, double b) {
        return a >= b;
    }

    // Character

    @Operator("-")
    @Precedence(10)
    public static int minus(char a) {
        return -a;
    }

    @Operator("*")
    @Precedence(20)
    public static int multiply(char a, char b) {
        return a * b;
    }

    @Operator("/")
    @Precedence(20)
    public static int divide(char a, char b) {
        return a / b;
    }

    @Operator("%")
    @Precedence(20)
    public static int remain(char a, char b) {
        return a % b;
    }

    @Operator("+")
    @Precedence(30)
    public static int add(char a, char b) {
        return a + b;
    }

    @Operator("-")
    @Precedence(30)
    public static int minus(char a, char b) {
        return a - b;
    }

    @Operator("<")
    @Precedence(50)
    public static boolean lessThan(char a, char b) {
        return a < b;
    }

    @Operator(">")
    @Precedence(50)
    public static boolean greaterThan(char a, char b) {
        return a > b;
    }

    @Operator("<=")
    @Precedence(50)
    public static boolean lessThanOrEqualTo(char a, char b) {
        return a <= b;
    }

    @Operator(">=")
    @Precedence(50)
    public static boolean greaterThanOrEqualTo(char a, char b) {
        return a >= b;
    }

}
