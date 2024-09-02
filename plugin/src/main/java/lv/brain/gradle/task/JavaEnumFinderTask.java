package lv.brain.gradle.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lv.brain.gradle.parser.SourceVisitor;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.Optional;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class JavaEnumFinderTask extends DefaultTask {
    private Class<? extends Enum<?>> target;
    private File file;
    private FileCollection sources;
    private final List<List<String>> data = new ArrayList<>();

    @TaskAction
    public void findJiraUsages() {
        List<String> enumValues = Arrays.stream(target.getEnumConstants()).map(Enum::name).toList();

        Set<File> mainJavaSrcDirs = getSourceFolders();
        data.clear();
        walk(mainJavaSrcDirs, enumValues);

        if(!getProject().getBuildDir().isDirectory()){
            if(!getProject().getBuildDir().mkdir()){
                throw new RuntimeException("Failed to create build dir");
            }
        }

        File destFile = file == null ? new File(getProject().getBuildDir(), target.getSimpleName()+".csv") : file;


        buildCSV(destFile, data);
    }

    @NotNull
    private Set<File> getSourceFolders() {
        if(sources!= null && !sources.isEmpty()){
            return sources.getFiles();
        }
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
                            parse(path, dir, enumValues);
                        } catch (IOException e) {
                            throw new IllegalArgumentException("unable to parse file: " + path, e);
                        }
                    });
        } catch (IOException e) {
            throw new IllegalArgumentException("unable to parse dir: " + dir, e);
        }
    }

    void parse(Path path, Path mainDir, List<String> enumValues) throws IOException {
        createJavaParser().parse(path).getResult().ifPresent(compilationUnit -> {
            SourceVisitor visitor = new SourceVisitor(path, target, enumValues);
            compilationUnit.accept(visitor, null);
            for (Map.Entry<Enum<?>, List<SourceVisitor.Data>> row : visitor.getData().entrySet()) {
                for (SourceVisitor.Data line : row.getValue()) {
                    data.add(Arrays.asList(line.getKey().name(), String.valueOf(line.getLine()), line.getPath().toString()));
                }
            }
        });
    }

    void buildCSV(File destFile, List<List<String>> data){
        System.out.println(destFile);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destFile))) {
            for (List<String> row : data) {
                String line = String.join(",", row);
                writer.write(line);
                writer.newLine();
                System.out.println(line);
            }
            //System.out.println("CSV file generated at: " + getProject().getBuildDir() + "/" + outputFileName);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    @Input
    public Class<? extends Enum<?>> getTarget() {
        return target;
    }

    @Optional
    @InputFiles
    public FileCollection getSources() {
        return sources;
    }

    @Optional
    @InputFile
    public File getFile() {
        return file;
    }

    public void setTarget(Class<? extends Enum<?>> target) {
        this.target = target;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setSources(FileCollection sources) {
        this.sources = sources;
    }
}
