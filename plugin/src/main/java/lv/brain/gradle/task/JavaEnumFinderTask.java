package lv.brain.gradle.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lv.brain.gradle.parser.SourceVisitor;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JavaEnumFinderTask extends DefaultTask {
    private Class<? extends Enum<?>> target;

    @TaskAction
    public void findJiraUsages() {
        List<String> enumValues = Arrays.stream(target.getEnumConstants()).map(Enum::name).toList();

        Set<File> mainJavaSrcDirs = getSourceFolders();

        walk(mainJavaSrcDirs, enumValues);
    }

    @NotNull
    private Set<File> getSourceFolders() {
        JavaPluginExtension javaExtension = getProject().getExtensions().getByType(JavaPluginExtension.class);
        SourceSet main = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        return main.getJava().getSrcDirs();
    }

    @NotNull
    private static JavaParser createJavaParser() {

        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver());
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        return new JavaParser(new ParserConfiguration().setSymbolResolver(symbolSolver));
    }

    private void walk(Set<File> dirs, List<String> enumValues) {
        dirs.stream().map(File::toPath).forEach(dir -> walk(enumValues, dir));
    }

    private void walk(List<String> enumValues, Path dir) {
        try {
            Files.walk(dir)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            parse(path, enumValues);
                        } catch (IOException e) {
                            throw new IllegalArgumentException("unable to parse file: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to parse dir: " + dir, e);
        }
    }

    void parse(Path path, List<String> enumValues) throws IOException {
        createJavaParser().parse(path).getResult().ifPresent(compilationUnit -> {
            SourceVisitor visitor = new SourceVisitor(path, target, enumValues);
            compilationUnit.accept(visitor, null);
            for (Map.Entry<Enum<?>, List<String>> row : visitor.getData().entrySet()) {
                for (String line : row.getValue()) {
                    System.out.println(row.getKey() + " / " + line);
                }
            }
        });
    }

    @Input
    public Class<? extends Enum<?>> getTarget() {
        return target;
    }

    public void setTarget(Class<? extends Enum<?>> target) {
        this.target = target;
    }
}
