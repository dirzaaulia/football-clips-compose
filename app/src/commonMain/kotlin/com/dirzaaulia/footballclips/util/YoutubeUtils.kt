package com.dirzaaulia.footballclips.util

fun extractVideoId(input: String): String? {
    return if (input.contains("youtube.com/embed/")) {
        input.substringAfter("embed/")
            .substringBefore("?")
            .substringBefore("/")
            .substringBefore("\"")
            .substringBefore("'")
            .trim()
    } else if (input.contains("v=")) {
        input.substringAfter("v=")
            .substringBefore("&")
            .substringBefore("/")
            .substringBefore("\"")
            .substringBefore("'")
            .trim()
    } else if (input.contains("youtu.be/")) {
        input.substringAfter("youtu.be/")
            .substringBefore("?")
            .substringBefore("/")
            .substringBefore("\"")
            .substringBefore("'")
            .trim()
    } else if (input.length == 11) {
        input
    } else {
        null
    }
}
