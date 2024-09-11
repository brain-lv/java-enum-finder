package lv.brain.gradle.extension;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.file.FileCollection;

import java.io.File;

@Getter
@Setter
public class JavaEnumFinderExtension {
    private String target;
    private File file = null;
    private FileCollection sources = null;

    public void setTarget(Class<? extends Enum<?>> target) {
        this.target = target.getName();
    }
}
