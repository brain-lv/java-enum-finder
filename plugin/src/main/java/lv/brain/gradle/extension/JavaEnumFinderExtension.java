package lv.brain.gradle.extension;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JavaEnumFinderExtension {
    private Class<? extends Enum<?>> target;
}
