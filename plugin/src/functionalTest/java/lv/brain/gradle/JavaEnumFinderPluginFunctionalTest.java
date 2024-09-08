package lv.brain.gradle;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaEnumFinderPluginFunctionalTest {

    @TempDir
    File projectDir;

    @Test
    void test1() throws IOException {
        File sourceDir = new File(this.getClass().getClassLoader().getResource("test1/input").getFile());

        FileUtils.copyDirectory(sourceDir, projectDir);

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFind");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        String output = result.getOutput().replaceAll(projectDir.getAbsolutePath(), "");
        Stream<String> lines = Arrays.stream(output.split("\\R"));
        List<String> actualLines = lines.filter(s -> !s.contains("BUILD SUCCESSFUL") && !s.contains(" actionable task")).collect(Collectors.toList());

        List<String> expectedLines = FileUtils.readLines(new File(this.getClass().getClassLoader().getResource("test1/expected.csv").getFile()), Charset.defaultCharset());

        assertEquals(expectedLines, FileUtils.readLines(new File(projectDir, "build/AccessMode.csv"), Charset.defaultCharset()));
    }


    @Test
    void test2() throws IOException {
        File sourceDir = new File(this.getClass().getClassLoader().getResource("test2/input").getFile());

        FileUtils.copyDirectory(sourceDir, projectDir);

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFind");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        String output = result.getOutput().replaceAll(projectDir.getAbsolutePath(), "");
        Stream<String> lines = Arrays.stream(output.split("\\R"));
        List<String> actualLines = lines.filter(s -> !s.contains("BUILD SUCCESSFUL") && !s.contains(" actionable task")).collect(Collectors.toList());

        List<String> expectedLines = FileUtils.readLines(new File(this.getClass().getClassLoader().getResource("test2/expected.csv").getFile()), Charset.defaultCharset());

        assertEquals(expectedLines, FileUtils.readLines(new File(projectDir, "m2/build/AccessMode.csv"), Charset.defaultCharset()));

    }


    @Test
    void test3() throws IOException {
        File sourceDir = new File(this.getClass().getClassLoader().getResource("test3/input").getFile());

        FileUtils.copyDirectory(sourceDir, projectDir);

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFind");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        String output = result.getOutput().replaceAll(projectDir.getAbsolutePath(), "");
        Stream<String> lines = Arrays.stream(output.split("\\R"));
        List<String> actualLines = lines.filter(s -> !s.contains("BUILD SUCCESSFUL") && !s.contains(" actionable task")).collect(Collectors.toList());
        List<String> expectedLines = FileUtils.readLines(new File(this.getClass().getClassLoader().getResource("test3/expected.csv").getFile()), Charset.defaultCharset());
        assertEquals(expectedLines, FileUtils.readLines(new File(projectDir, "m2/build/AccessSpecifier.csv"), Charset.defaultCharset()));

    }
}
