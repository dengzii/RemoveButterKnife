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

    var resBindStatement = mapOf(
            Pair(BindResType.Anim, "getResource().getAnimation(%{resId})"),
            Pair(BindResType.View, "findViewById(%{resId})"),
            Pair(BindResType.Array, "getResource().getIntArray(%{resId})"),
            Pair(BindResType.Bitmap, "getResource().getBitmap(%{resId})"),
            Pair(BindResType.Bool, "getResource().getBool(%{resId})"),
            Pair(BindResType.Color, "getResource().getColor(%{resId})"),
            Pair(BindResType.Dimen, "getResource().getDimen(%{resId})"),
            Pair(BindResType.Drawable, "getResource().getDrawable(%{resId})"),
            Pair(BindResType.Float, "getResource().getFloat(%{resId})"),
            Pair(BindResType.Int, "getResource().getInt(%{resId})"),
            Pair(BindResType.String, "getResource().getString(%{resId})"),
            Pair(BindResType.Views, ""),
            Pair(BindResType.Unknown, "null")
    )

    //15019472822, 13728669603
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