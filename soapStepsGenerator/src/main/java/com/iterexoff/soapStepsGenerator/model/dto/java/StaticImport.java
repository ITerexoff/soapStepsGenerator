package com.iterexoff.soapStepsGenerator.model.dto.java;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticImport {
    private Class<?> clazz;
    private ClassName className;
    private String[] names;

    public StaticImport(Class<?> clazz, String... names) {
        this.clazz = clazz;
        this.names = names;
    }

    public StaticImport(ClassName className, String... names) {
        this.className = className;
        this.names = names;
    }

    public void addStaticImportTo(JavaFile.Builder javaFileBuilder) {
        if (clazz != null) {
            javaFileBuilder.addStaticImport(clazz, names);
        } else if (className != null) {
            javaFileBuilder.addStaticImport(className, names);
        }
    }
}
