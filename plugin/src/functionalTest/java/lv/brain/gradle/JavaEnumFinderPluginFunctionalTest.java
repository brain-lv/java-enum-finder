/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package lv.brain.gradle;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A simple functional test for the 'lv.brain.gradle.javaEnumFinder' plugin.
 */
class JavaEnumFinderPluginFunctionalTest {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
            "plugins {" +
            "  id('lv.brain.gradle.javaEnumFinder')" +
            "}");

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFinder");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        // Verify the result
        assertTrue(result.getOutput().contains("Hello from plugin 'lv.brain.gradle.javaEnumFinder'"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
