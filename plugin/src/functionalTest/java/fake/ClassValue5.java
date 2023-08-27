package fake;

import java.nio.file.AccessMode;

import static java.nio.file.AccessMode.*;

public class ClassValue5 {
    private static final String EXECUTE = "FAKE_VALUE";
    public static void main(String[] args) {
        System.out.println("static all import"+ READ);
        System.out.println("static all import"+ EXECUTE);
        System.out.println("static all import"+ AccessMode.EXECUTE);
        System.out.println("static all import"+ java.nio.file.AccessMode.EXECUTE);
        System.out.println("static all import"+ java.nio.file.AccessMode.WRITE);
    }
}