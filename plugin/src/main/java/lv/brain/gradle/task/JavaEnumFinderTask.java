package lv.brain.gradle.task;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import lv.brain.gradle.parser.SourceVisitor;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class JavaEnumFinderTask extends DefaultTask {
    private Class<? extends Enum<?>> targetClass;
    private String target;
    private File file;
    private FileCollection sources;
    private final List<SourceVisitor.Data> data = new ArrayList<>();

    @TaskAction
    public void findJiraUsages() {
        List<String> enumValues = Arrays.stream(targetClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());

        Set<File> mainJavaSrcDirs = getSourceFolders();
        data.clear();
        walk(mainJavaSrcDirs, enumValues);
        data.sort(SourceVisitor.Data::compareTo);

        if(!getProject().getBuildDir().isDirectory()){
            if(!getProject().getBuildDir().mkdir()){
                throw new RuntimeException("Failed to create build dir");
            }
        }

        File destFile = file == null ? new File(getProject().getBuildDir(), targetClass.getSimpleName()+".csv") : file;


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
            SourceVisitor visitor = new SourceVisitor(path, targetClass, enumValues);
            compilationUnit.accept(visitor, null);
            for (Map.Entry<Enum<?>, List<SourceVisitor.Data>> row : visitor.getData().entrySet()) {
                data.addAll(row.getValue());
            }
        });
    }

    void buildCSV(File destFile, List<SourceVisitor.Data> data){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destFile))) {
            for (SourceVisitor.Data row : data) {
                writer.write(row.getKey().name());
                writer.write(",");
                writer.write(String.valueOf(row.getLine()));
                writer.write(",");
                writer.write(row.getPath().toString().replace(getProject().getRootProject().getRootDir().getPath(), ""));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV file", e);
        }
    }

    @Input
    public String getTarget() {
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

    public void setTarget(String target) {
        this.target = target;
        this.targetClass = buildTargetClass(target);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setSources(FileCollection sources) {
        this.sources = sources;
    }

    public Class<? extends Enum<?>> buildTargetClass(final String enumClassName){
        FileCollection runtimeClasspath = getProject().getConfigurations().getByName("runtimeClasspath");

        // Create a URLClassLoader with the runtime classpath
        try (URLClassLoader classLoader = new URLClassLoader(
                runtimeClasspath.getFiles().stream().map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        throw new GradleException("unable to resolve dependency: " + f,e);
                    }
                }).toArray(URL[]::new),
                this.getClass().getClassLoader())) {

            // Load the class dynamically from the runtime classpath
            Class<?> enumClass = classLoader.loadClass(enumClassName);

            if (Enum.class.isAssignableFrom(enumClass)) {
                return (Class<? extends Enum<?>>) enumClass;
            } else {
                System.out.println(enumClassName + " is not an enum.");
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new GradleException("unable to initialize enum: " + enumClassName, e);
        }
        return null;
    }
}
