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

    // Insert the call bindView method statement into the first matching method in the list below.
    var insertBindViewMethodIntoMethod = mutableListOf("onCreate", "onCreateView")
    // Insert the call bindView method statement after the first matching method call expression statement in the list below.
    var insertCallBindViewMethodAfterCallMethod = mutableListOf("setContentView", "inflate")

    var resBindStatement = mapOf(
            Pair(BindType.Anim, "%{SOURCE}.getResource().getAnimation(%{RES_ID})"),
            // Pair(BindType.Array, "%{SOURCE}.getResource().getIntArray(%{RES_ID})"),
            Pair(BindType.Bool, "%{SOURCE}.getResource().getBool(%{RES_ID})"),
            Pair(BindType.Color, "%{SOURCE}.getResource().getColor(%{RES_ID}, %{THEME})"),
            Pair(BindType.Dimen, "%{SOURCE}.getResource().getDimen(%{RES_ID})"),
            Pair(BindType.Drawable, "%{SOURCE}.getResource().getDrawable(%{RES_ID}, %{THEME})"),
            Pair(BindType.Float, "%{SOURCE}.getResource().getFloat(%{RES_ID})"),
            Pair(BindType.Int, "%{SOURCE}.getResource().getInt(%{RES_ID})"),
            Pair(BindType.String, "%{SOURCE}.getResource().getString(%{RES_ID})"),
            Pair(BindType.View, "%{SOURCE}.findViewById(%{RES_ID})"),
            Pair(BindType.Unknown, "")
    )

    object PsiTypes {

        val androidView by lazy { findByName("android.view.View") }
        val androidActivity by lazy { findByName("android.content.Activity") }
        val androidFragment by lazy { findByName("android.content.Fragment") }
        val androidXFragment by lazy { findByName("androidx.fragment.app.Fragment") }
        val androidDialog by lazy { findByName("android.app.Dialog") }

        private lateinit var project: Project

        fun init(project: Project) {
            this.project = project
        }

        private fun findByName(name: String, scope: GlobalSearchScope = GlobalSearchScope.allScope(project)): PsiType {
            return PsiType.getTypeByName(name, project, scope)
        }
    }
}