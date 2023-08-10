package com.iterexoff;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaticImportMojo {

    /**
     * Package name of class for static import.
     */
    private String packageName;

    /**
     * Simple name of class for static import.
     */
    private String simpleName;

    /**
     * Name of static param for static import.
     */
    private String paramName;
}
