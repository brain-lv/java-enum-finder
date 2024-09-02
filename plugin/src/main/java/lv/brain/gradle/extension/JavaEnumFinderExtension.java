package lv.brain.gradle.extension;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
public class JavaEnumFinderExtension {
    private Class<? extends Enum<?>> target;
    private File file = null;
    private FileCollection sources=null;
}
