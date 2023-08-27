package lv.brain.gradle.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.nio.file.AccessMode.*;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceVisitorTest {
    private static final List<String> enumValues = stream(java.nio.file.AccessMode.class.getEnumConstants()).map(Enum::name).toList();
    private static final String root = System.getProperty("user.dir");

    @Test
    void t1() throws IOException {
        System.out.println(System.getProperty("user.dir"));
        assertEquals(Map.of(), helper(("src/functionalTest/java/fake/ClassValue1.java")));
    }

    @Test
    void t2() throws IOException {

        assertEquals(Map.of(
                EXECUTE, results("7:src/functionalTest/java/fake/ClassValue2.java")
        ), helper(("src/functionalTest/java/fake/ClassValue2.java")));
    }

    @Test
    void t3() throws IOException {

        assertEquals(Map.of(
                EXECUTE, results("7:src/functionalTest/java/fake/ClassValue3.java")
        ), helper(("src/functionalTest/java/fake/ClassValue3.java")));
    }

    @Test
    void t4() throws IOException {

        assertEquals(Map.of(
                EXECUTE, results("7:src/functionalTest/java/fake/ClassValue4.java")
        ), helper(("src/functionalTest/java/fake/ClassValue4.java")));
    }

    @Test
    void t5() throws IOException {

        assertEquals(Map.of(
                READ, results("10:src/functionalTest/java/fake/ClassValue5.java"),
                EXECUTE, results(
                        "12:src/functionalTest/java/fake/ClassValue5.java",
                        "13:src/functionalTest/java/fake/ClassValue5.java"
                ),
                WRITE, results(
                        "14:src/functionalTest/java/fake/ClassValue5.java"
                )
        ), helper(("src/functionalTest/java/fake/ClassValue5.java")));
    }

    static List<String> results(String... expected) {
        return stream(expected).map(v -> {
            String[] parts = v.split(":");
            parts[1] = root + "/" + parts[1];
            return String.join(":", parts);
        }).toList();
    }

    static Map<Enum<?>, List<String>> helper(String path) throws IOException {
        return helper(Path.of(root + "/" + path));
    }

    static Map<Enum<?>, List<String>> helper(Path path) throws IOException {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        JavaParser javaParser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolSolver));

        SourceVisitor sourceVisitor = new SourceVisitor(path, AccessMode.class, enumValues);

        javaParser.parse(path).getResult().ifPresent(compilationUnit -> compilationUnit.accept(sourceVisitor, null));
        return sourceVisitor.getData();
    }
}