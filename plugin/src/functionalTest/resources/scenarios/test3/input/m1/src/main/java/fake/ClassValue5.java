package fake;

import com.github.javaparser.ast.AccessSpecifier;

import static com.github.javaparser.ast.AccessSpecifier.PRIVATE;

public class ClassValue5 {
    private static final String PUBLIC = "FAKE_VALUE";
    public static void main(String[] args) {
        System.out.println("static all import"+ PRIVATE);
        System.out.println("static all import"+ PUBLIC);
        System.out.println("static all import"+ AccessSpecifier.PUBLIC);
        System.out.println("static all import"+ com.github.javaparser.ast.AccessSpecifier.PUBLIC);
        System.out.println("static all import"+ com.github.javaparser.ast.AccessSpecifier.PRIVATE);
    }
}