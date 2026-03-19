package com.example.snippets.java.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method inside a [SnippetGroup] with execution metadata.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SnippetItem {
    /**
     * @return The displayed title for this individual snippet action.
     */
    String title();

    /**
     * @return A brief explanation of what the snippet achieves.
     */
    String description() default "";
}
