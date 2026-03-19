package com.example.snippets.kotlin.annotations

/**
 * Marks a method inside a [SnippetGroup] with execution metadata.
 *
 * @property title The displayed title for this individual snippet action.
 * @property description A brief explanation of what the snippet achieves.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SnippetItem(
    val title: String,
    val description: String = ""
)
