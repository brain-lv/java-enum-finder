package lv.brain.gradle.parser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

public class SourceVisitor  extends VoidVisitorAdapter<Void> {
    private final Path filePath;
    private final Class<? extends Enum<?>> target;

    private final Collection<String> values;
    private final Collection<String> skip = new HashSet<>();

    @Getter
    private final Map<Enum<?>, List<Data>> data = new HashMap<>();
    private boolean hasStaticImport;

    public SourceVisitor(Path filePath, Class<? extends Enum<?>> target, Collection<String> values) {
        this.filePath = filePath;
        this.target = target;
        this.values = values;
    }

    @Override
    public void visit(ImportDeclaration n, Void arg) {
        if (n.isStatic() && n.isAsterisk() && n.getNameAsString().equals(target.getName())) {
            hasStaticImport = true;
        }
        else if(n.isStatic()){
            Optional<Name> qualifier = n.getName().getQualifier();
            if(qualifier.isPresent() && qualifier.get().asString().equals(target.getName())){
                hasStaticImport = true;
            }
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldDeclaration n, Void arg) {
        if(n.isStatic() && values.contains(n.getVariable(0).getNameAsString())){
            skip.add(n.getVariable(0).getNameAsString());
        }
        super.visit(n, arg);
    }

    @Override
    public void visit(FieldAccessExpr n, Void arg) {
        if(skip.contains(n.getScope().toString())){
            return;
        }
        boolean match = n.getScope().toString().equals(target.getName()) && values.contains(n.getNameAsString());

        if (match || values.contains(n.getNameAsString()) && n.getScope().toString().equals(target.getSimpleName())) {
            n.getBegin().ifPresent(position -> {
                int line = position.line;
                Enum<?> anEnum = Arrays.stream(this.target.getEnumConstants()).filter(k -> k.name().equals(n.getNameAsString())).findFirst().orElse(null);
                data.computeIfAbsent(anEnum, k ->new ArrayList<>()).add(new Data(anEnum, line, filePath));
            });
        }

        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, Void arg) {
        if (hasStaticImport && (values.contains(n.getNameAsString())&& !skip.contains(n.getNameAsString()))) {
                n.getBegin().ifPresent(position -> {
                    int line = position.line;
                    Enum<?> anEnum = Arrays.stream(this.target.getEnumConstants()).filter(k -> k.name().equals(n.getNameAsString())).findFirst().orElse(null);
                    data.computeIfAbsent(anEnum, k ->new ArrayList<>()).add(new Data(anEnum, line, filePath));

                });
        }
        super.visit(n, arg);
    }

    @Getter
    public static class Data implements Comparable<Data>{
        private final Enum<?> key;
        private final int line;
        private final Path path;

        public Data(Enum<?> key, int line, Path path) {
            this.key = key;
            this.line = line;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Data data = (Data) o;
            return line == data.line && Objects.equals(key, data.key) && Objects.equals(path, data.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, line, path);
        }

        @Override
        public int compareTo(@NotNull SourceVisitor.Data o) {
            final int keyResult = ((Comparable<Enum<?>>) o.key).compareTo(key);
            if (keyResult != 0) {
                return keyResult * -1;
            }
            final int pathResult = o.path.compareTo(path);
            if(pathResult != 0){
                return pathResult * -1;
            }
            return Integer.compare(o.line, line) * -1;
        }
    }
}
