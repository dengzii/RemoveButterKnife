package com.dengzii.plugin.rbk

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiType
import com.intellij.psi.search.GlobalSearchScope

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

    var insertBindViewMethodIntoMethod = mutableListOf("onCreate", "onCreateView")

    var insertCallBindViewMethodAfterCallMethod = mutableListOf("setContentView", "inflate")

    var formatCode = true

    object PsiTypes {

        val androidView by lazy { findByName("android.view.View") }
        val androidActivity by lazy { findByName("android.content.Activity") }

        private lateinit var project: Project

        fun init(project: Project) {
            this.project = project
        }
        private fun findByName(name: String, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): PsiType {
            return PsiType.getTypeByName(name, project, scope)
        }
    }
}