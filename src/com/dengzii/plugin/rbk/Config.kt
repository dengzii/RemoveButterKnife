package com.dengzii.plugin.rbk

import com.intellij.lang.Language

/**
 *
 * @author https://github.com/dengzii
 */
object Config {

    val LangeJava = Language.findLanguageByID("JAVA")
    val LangeKotlin = Language.findLanguageByID("kotlin")
    var methodNameBindView = "bindView"
    var fieldNamePrefix = "m"
    var addPrivateModifier = true

    var bindViewMethodInvoker = mutableListOf("onCreate", "onCreateView")

    var bindViewMethodInvokerFirstLine = true

    var formatCode = true
}