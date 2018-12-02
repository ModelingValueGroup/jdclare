
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StructGenerator {
    private static final int  MAX_NUM_TYPE_ARGS           = 25;
    private static final Path STRUCT_PACK_INTERFACES      = Paths.get("src", "org", "modelingvalue", "collections", "struct");
    private static final Path STRUCT_PACK_IMPLEMENTATIONS = Paths.get("src", "org", "modelingvalue", "collections", "struct", "impl");

    public static void main(String[] args) throws IOException {
        new StructGenerator().generate();
    }

    private void generate() throws IOException {
        sanityCheck();
        generateStructs();
    }

    private void sanityCheck() {
        Stream.of(STRUCT_PACK_INTERFACES, STRUCT_PACK_IMPLEMENTATIONS).forEach(p -> {
            if (!Files.isDirectory(p)) {
                throw new Error("no such dir: " + p);
            }
        });
    }

    private void generateStructs() throws IOException {
        for (int i = 0; i < MAX_NUM_TYPE_ARGS; i++) {
            generateStructInterface(i);
            generateStructImplementation(i);
        }
    }

    private void generateStructInterface(int i) throws IOException {
        List<String> f = new ArrayList<>();
        f.add("package org.modelingvalue.collections.struct;");
        f.add("");
        f.add("public interface " + structNameWithTypeArgs(i) + " extends " + structNameWithTypeArgs(i - 1) + " {");
        if (0 < i) {
            f.add("    T" + (i - 1) + " get" + (i - 1) + " ();");
        }
        f.add("}");
        Files.write(STRUCT_PACK_INTERFACES.resolve("Struct" + i + ".java"), f);
    }

    private void generateStructImplementation(int i) throws IOException {
        List<String> f = new ArrayList<>();
        f.add("package org.modelingvalue.collections.struct.impl;");
        f.add("");
        f.add("import org.modelingvalue.collections.struct.*;");
        f.add("");
        if (i > 0) {
            f.add("@SuppressWarnings(\"unchecked\")");
        }
        f.add("public class " + structImplNameWithTypeArgs(i) + " extends " + structImplNameWithTypeArgs(i - 1) + " implements " + structNameWithTypeArgs(i) + " {");
        f.add("");
        f.add("    private static final long serialVersionUID = -85170218" + i + "710134661L;");
        f.add("");
        f.add("    public Struct" + i + "Impl(" + argTypesWithParams(i) + ") {");
        if (i == 0) {
            f.add("        super();");
        } else {
            f.add("        this((Object) " + argParams(i) + ");");
        }
        f.add("    }");
        f.add("");
        f.add("    protected Struct" + i + "Impl(Object...data){");
        f.add("        super(data);");
        f.add("    }");

        if (0 < i) {
            f.add("");
            f.add("    public T" + (i - 1) + " get" + (i - 1) + "() {");
            f.add("        return (T" + (i - 1) + ") get(" + (i - 1) + ");");
            f.add("    }");
        }
        f.add("}");
        Files.write(STRUCT_PACK_IMPLEMENTATIONS.resolve("Struct" + i + "Impl.java"), f);
    }

    private static String structNameWithTypeArgs(int i) {
        return "Struct" + (i < 0 ? "" : i) + argTypes(i);
    }

    private static String structImplNameWithTypeArgs(int i) {
        return "Struct" + (i < 0 ? "" : i) + "Impl" + argTypes(i);
    }

    private static String argTypes(int i) {
        return i <= 0 ? "" : "<" + seq(i, "T%d", ",") + ">";
    }

    private static String argTypesWithParams(int i) {
        return i <= 0 ? "" : seq(i, "T%d t%d", ",");
    }

    private static String argParams(int i) {
        return i <= 0 ? "" : seq(i, "t%d", ",");
    }

    private static String seq(int n, String fmt, String del) {
        return IntStream.range(0, n).mapToObj(i -> String.format(fmt, i, i, i, i, i, i, i, i)).collect(Collectors.joining(del));
    }
}
