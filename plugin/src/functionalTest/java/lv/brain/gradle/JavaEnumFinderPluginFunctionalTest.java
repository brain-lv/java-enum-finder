/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lv.brain.gradle;

import org.gradle.internal.impldep.com.google.common.io.Files;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A simple functional test for the 'lv.brain.gradle.javaEnumFinder' plugin.
 */
class JavaEnumFinderPluginFunctionalTest {
    private static final String root = System.getProperty("user.dir");

    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunTask() throws IOException {
        File source = new File(root, "src/functionalTest/java/fake");
        File desc = new File(projectDir, "src/main/java/fake");
        if(desc.exists() || desc.mkdirs()) {
            for (File file : source.listFiles()) {
                Files.copy(file, new File(desc, file.getName()));
            }
        }
        else{
            throw new IOException("unable to copy source files");
        }

        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
                """
                        plugins {
                          id('java')
                          id('lv.brain.gradle.javaEnumFinder')
                        }
                        javaEnumFinder{
                          target java.nio.file.AccessMode
                        }
                        """);

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFind");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();
        List<String> actualLines = result.getOutput().replaceAll(root, "").replaceAll(projectDir.getAbsolutePath(), "").lines().filter(s -> !s.contains("BUILD SUCCESSFUL") && !s.contains(" actionable task")).collect(Collectors.toList());
        actualLines.remove("");
        actualLines.remove("> Task :javaEnumFind");
        actualLines.remove("");
        List<String> expectedLines = Arrays.asList(
            "EXECUTE / 7:/private/src/main/java/fake/ClassValue3.java",
            "EXECUTE / 7:/private/src/main/java/fake/ClassValue2.java",
            "WRITE / 14:/private/src/main/java/fake/ClassValue5.java",
            "READ / 10:/private/src/main/java/fake/ClassValue5.java",
            "EXECUTE / 12:/private/src/main/java/fake/ClassValue5.java",
            "EXECUTE / 13:/private/src/main/java/fake/ClassValue5.java",
            "EXECUTE / 7:/private/src/main/java/fake/ClassValue4.java"
        );
        actualLines.sort(String::compareTo);
        expectedLines.sort(String::compareTo);
        // Verify the result
        assertEquals(expectedLines, actualLines);
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
