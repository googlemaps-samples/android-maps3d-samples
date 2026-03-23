package com.example.snippets.kotlin.annotations

/**
 * Marks a class containing multiple snippet methods with category metadata.
 *
 * @property title The displayed title for this snippet category.
 * @property description A brief explanation of what the category contains.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class SnippetGroup(
    val title: String,
    val description: String = "",
)
