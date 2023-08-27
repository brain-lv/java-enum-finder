package lv.brain.gradle.parser;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;

import java.nio.file.Path;
import java.util.*;

public class SourceVisitor  extends VoidVisitorAdapter<Void> {
    private final Path filePath;
    private final Class<? extends Enum<?>> target;

    private final Collection<String> values;
    private final Collection<String> skip = new HashSet<>();

    @Getter
    private final Map<Enum<?>, List<String>> data = new HashMap<>();
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
            int line = n.getBegin().get().line;
            Enum<?> anEnum = Arrays.stream(this.target.getEnumConstants()).filter(k -> k.name().equals(n.getNameAsString())).findFirst().orElse(null);
            data.computeIfAbsent(anEnum, k ->new ArrayList<>()).add(line + ":" +filePath);
        }

        super.visit(n, arg);
    }

    @Override
    public void visit(NameExpr n, Void arg) {
        if (hasStaticImport && (values.contains(n.getNameAsString())&& !skip.contains(n.getNameAsString()))) {
                int line = n.getBegin().get().line;
                Enum<?> anEnum = Arrays.stream(this.target.getEnumConstants()).filter(k -> k.name().equals(n.getNameAsString())).findFirst().orElse(null);
                data.computeIfAbsent(anEnum, k ->new ArrayList<>()).add(line + ":" +filePath);

        }
        super.visit(n, arg);
    }
}
