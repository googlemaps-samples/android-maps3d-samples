package com.example.snippets.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class containing multiple snippet methods with category metadata.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SnippetGroup {
    /**
     * @return The displayed title for this snippet category.
     */
    String title();

    /**
     * @return A brief explanation of what the category contains.
     */
    String description() default "";
}
