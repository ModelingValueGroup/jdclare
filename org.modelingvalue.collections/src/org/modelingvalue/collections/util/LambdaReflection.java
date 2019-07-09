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

import java.io.Serializable;
import java.lang.invoke.MethodHandleInfo;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.modelingvalue.collections.List;;

public interface LambdaReflection extends Serializable {

    static List<Class<?>> in(String signature) {
        String in = signature.substring(1, signature.lastIndexOf(')'));
        List<Class<?>> list = List.of();
        int i = 0;
        while (i < in.length()) {
            char charAt = in.charAt(i++);
            if (charAt == 'L') {
                int c = in.indexOf(';', i);
                int p = in.indexOf('<', i);
                list = list.append(cls(in.substring(i, p > 0 ? p : c).replace('/', '.')));
                i = c + 1;
            } else {
                list = list.append(cls(charAt));
            }
        }
        return list;
    }

    static Class<?> out(String signature) {
        String out = signature.substring(signature.lastIndexOf(')') + 1);
        int p = out.indexOf('<');
        return out.length() == 1 ? cls(out.charAt(0)) : cls(out.substring(1, p > 0 ? p : out.length() - 1).replace('/', '.'));
    }

    static Class<?> cls(char name) {
        switch (name) {
        case 'B':
            return byte.class;
        case 'C':
            return char.class;
        case 'D':
            return double.class;
        case 'F':
            return float.class;
        case 'I':
            return int.class;
        case 'J':
            return long.class;
        case 'S':
            return short.class;
        case 'Z':
            return boolean.class;
        default:
            return null;
        }
    }

    static Class<?> cls(String name) {
        try {
            return Class.forName(name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static Method implMethod(SerializedLambda lambda) {
        Class<?> cls = cls(lambda.getImplClass().replaceAll("/", "."));
        try {
            List<Class<?>> sign = in(lambda.getImplMethodSignature());
            Method method = cls.getDeclaredMethod(lambda.getImplMethodName(), sign.toArray(l -> new Class[l]));
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    static Method funcMethod(SerializedLambda lambda) {
        Class<?> cls = cls(lambda.getFunctionalInterfaceClass().replaceAll("/", "."));
        try {
            List<Class<?>> sign = in(lambda.getFunctionalInterfaceMethodSignature());
            Method method = cls.getDeclaredMethod(lambda.getFunctionalInterfaceMethodName(), sign.toArray(l -> new Class[l]));
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    static Object[] capturedArgs(SerializedLambda lambda) {
        Object[] args = new Object[lambda.getCapturedArgCount()];
        for (int i = 0; i < args.length; i++) {
            args[i] = lambda.getCapturedArg(i);
        }
        return args;
    }

    default SerializedLambda serialized() {
        try {
            Method replaceMethod = getClass().getDeclaredMethod("writeReplace");
            replaceMethod.setAccessible(true);
            return (SerializedLambda) replaceMethod.invoke(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static List<Class<?>> in(SerializedLambda lambda) {
        List<Class<?>> in = in(lambda.getImplMethodSignature());
        return in.sublist(lambda.getCapturedArgCount(), in.size());
    }

    static Class<?> out(SerializedLambda lambda) {
        return out(lambda.getImplMethodSignature());
    }

    static Object invoke(SerializedLambda lambda, LambdaReflection function, Object... args) {
        Method method = implMethod(lambda);
        method.setAccessible(true);
        try {
            Object[] capt = capturedArgs(lambda);
            if (capt.length != 0) {
                Object[] all = new Object[capt.length + args.length];
                System.arraycopy(capt, 0, all, 0, capt.length);
                System.arraycopy(args, 0, all, capt.length, args.length);
                args = all;
            }
            return method.invoke(function, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static int hash(SerializedLambda lambda) {
        return lambda.getCapturingClass().hashCode() ^ //
                lambda.getFunctionalInterfaceClass().hashCode() ^ //
                lambda.getFunctionalInterfaceMethodName().hashCode() ^ + //
                lambda.getFunctionalInterfaceMethodSignature().hashCode() ^ //
                lambda.getImplMethodKind() ^ //
                lambda.getImplClass().hashCode() ^ //
                lambda.getImplMethodName().hashCode() ^ //
                lambda.getImplMethodSignature().hashCode() ^ //
                lambda.getInstantiatedMethodType().hashCode() ^ //
                Arrays.hashCode(capturedArgs(lambda));
    }

    static boolean equal(SerializedLambda a, SerializedLambda b) {
        return a.getImplMethodKind() == b.getImplMethodKind() && //
                a.getCapturingClass().equals(b.getCapturingClass()) && //
                a.getFunctionalInterfaceClass().equals(b.getFunctionalInterfaceClass()) && //
                a.getFunctionalInterfaceMethodName().equals(b.getFunctionalInterfaceMethodName()) && //
                a.getFunctionalInterfaceMethodSignature().equals(b.getFunctionalInterfaceMethodSignature()) && //
                a.getImplClass().equals(b.getImplClass()) && //
                a.getImplMethodName().equals(b.getImplMethodName()) && //
                a.getImplMethodSignature().equals(b.getImplMethodSignature()) && //
                a.getInstantiatedMethodType().equals(b.getInstantiatedMethodType()) && //
                Arrays.equals(capturedArgs(a), capturedArgs(b));
    }

    static String string(SerializedLambda lambda) {
        String implKind = MethodHandleInfo.referenceKindToString(lambda.getImplMethodKind());
        return String.format("SerializedLambda[%s=%s, %s=%s.%s:%s, " + "%s=%s %s.%s:%s, %s=%s, %s=%s]", //
                "capturingClass", lambda.getCapturingClass(), //
                "functionalInterfaceMethod", lambda.getFunctionalInterfaceClass(), lambda.getFunctionalInterfaceMethodName(), lambda.getFunctionalInterfaceMethodSignature(), //
                "implementation", implKind, lambda.getImplClass(), lambda.getImplMethodName(), lambda.getImplMethodSignature(), //
                "instantiatedMethodType", lambda.getInstantiatedMethodType(), //
                "capturedArgs", StringUtil.toString(capturedArgs(lambda)));
    }

    LambdaImpl<?> of();

    default Method implMethod() {
        return of().implMethod();
    }

    default Method funcMethod() {
        return of().funcMethod();
    }

    default Object[] capturedArgs() {
        return of().capturedArgs();
    }

    default List<Class<?>> in() {
        return of().in();
    }

    default Class<?> out() {
        return of().out();
    }

    default Object invoke(Object... args) {
        return of().invoke(args);
    }

    default LambdaReflection original() {
        return this;
    }

    static abstract class LambdaImpl<F extends LambdaReflection> implements LambdaReflection {

        private static final long      serialVersionUID = 5814783501752526565L;

        protected final F              f;
        private final SerializedLambda serialized;
        private final int              hashCode;

        public LambdaImpl(F f) {
            this.f = f;
            this.serialized = f.serialized();
            this.hashCode = LambdaReflection.hash(serialized);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (getClass() != obj.getClass()) {
                return false;
            }
            LambdaImpl<?> other = (LambdaImpl) obj;
            if (hashCode != other.hashCode) {
                return false;
            } else {
                return LambdaReflection.equal(serialized, other.serialized);
            }
        }

        @Override
        public String toString() {
            return LambdaReflection.string(serialized);
        }

        @Override
        public Method implMethod() {
            return LambdaReflection.implMethod(serialized);
        }

        @Override
        public Method funcMethod() {
            return LambdaReflection.funcMethod(serialized);
        }

        @Override
        public Object[] capturedArgs() {
            return LambdaReflection.capturedArgs(serialized);
        }

        @Override
        public List<Class<?>> in() {
            return LambdaReflection.in(serialized);
        }

        @Override
        public Class<?> out() {
            return LambdaReflection.out(serialized);
        }

        @Override
        public Object invoke(Object... args) {
            return LambdaReflection.invoke(serialized, f, args);
        }

        @Override
        public F original() {
            return f;
        }

    }

}
