package lv.brain.gradle.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.*;
import static java.nio.file.AccessMode.*;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SourceVisitorTest {
    private static final List<String> enumValues = stream(java.nio.file.AccessMode.class.getEnumConstants()).map(Enum::name).collect(toList());
    private static final String root = System.getProperty("user.dir");

    @Test
    void t1() throws IOException {
        assertEquals(of(), helper(("src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue1.java")));
    }

    @Test
    void t2() throws IOException {

        assertEquals(of(
                EXECUTE, rows(data(EXECUTE, 7,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue2.java"))
        ), helper(("src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue2.java")));
    }

    @Test
    void t3() throws IOException {

        assertEquals(of(
                EXECUTE, rows(data(EXECUTE, 7,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue3.java"))
        ), helper(("src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue3.java")));
    }

    @Test
    void t4() throws IOException {

        assertEquals(of(
                EXECUTE, rows(data(EXECUTE, 7,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue4.java"))
        ), helper(("src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue4.java")));
    }

    @Test
    void t5() throws IOException {

        assertEquals(of(
                READ, rows(
                        data(READ, 10,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue5.java")
                ),
                EXECUTE, rows(
                        data(EXECUTE, 12,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue5.java"),
                        data(EXECUTE, 13,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue5.java")
                ),
                WRITE, rows(
                        data(WRITE,14,"src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue5.java")
                )
        ), helper(("src/functionalTest/resources/test1/input/src/main/java/fake/ClassValue5.java")));
    }

    static List<SourceVisitor.Data> rows(SourceVisitor.Data... rows){
        return asList(rows);
    }

    static SourceVisitor.Data data(Enum<?> key, int line, String path){
        return new SourceVisitor.Data(key, line, new File(root + "/" + path).toPath());
    }

    static Map<Enum<?>, List<SourceVisitor.Data>> helper(String path) throws IOException {
        return helper(new File(root + "/" + path).toPath());
    }

    static Map<Enum<?>, List<SourceVisitor.Data>> helper(Path path) throws IOException {
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        JavaParser javaParser = new JavaParser(new ParserConfiguration().setSymbolResolver(symbolSolver));

        SourceVisitor sourceVisitor = new SourceVisitor(path, AccessMode.class, enumValues);

        javaParser.parse(path).getResult().ifPresent(compilationUnit -> compilationUnit.accept(sourceVisitor, null));
        return sourceVisitor.getData();
    }
}