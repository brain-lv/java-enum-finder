package lv.brain.gradle;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaEnumFinderPluginFunctionalTest {
    private final File DIR_SCENARIOS = new File(Objects.requireNonNull(JavaEnumFinderPluginFunctionalTest.class.getClassLoader().getResource("scenarios")).getFile());

    @TempDir
    File projectDir;

    @ParameterizedTest
    @ArgumentsSource(ScenarioArgumentsProvider.class)
    void verify(final String scenario, String expectedFilePath) throws IOException {
        File inputDirectory = new File(DIR_SCENARIOS, scenario);

        FileUtils.copyDirectory(new File(inputDirectory, "input"), projectDir);
        List<String> expectedLines = FileUtils.readLines(new File(inputDirectory, "expected.csv"), Charset.defaultCharset());
        List<String> outputLines = FileUtils.readLines(new File(inputDirectory, "output.txt"), Charset.defaultCharset());

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("javaEnumFind");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        String output = result.getOutput().replaceAll(projectDir.getAbsolutePath(), "");
        List<String> lines = Arrays.stream(output.split("\\R"))
                .map(s->s.replaceAll("BUILD SUCCESSFUL in \\d+([ms]+)", "BUILD SUCCESSFUL in XXX"))
                .collect(Collectors.toList())
                ;

        assertEquals(expectedLines, FileUtils.readLines(new File(projectDir, expectedFilePath), Charset.defaultCharset()));

        assertEquals(outputLines, lines);
    }


    static class ScenarioArgumentsProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            return Stream.of(
                    Arguments.of("test1", "build/AccessMode.csv"),
                    Arguments.of("test2", "m2/build/AccessMode.csv"),
                    Arguments.of("test3", "m2/build/AccessSpecifier.csv")
            );
        }
    }
}
