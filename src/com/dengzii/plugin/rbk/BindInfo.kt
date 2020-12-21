package com.dengzii.plugin.rbk

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiReference

/**
 *
 * @author https://github.com/dengzii
 */
class BindInfo(
        val viewClass: String,
        val idResExpr: String,
        var filedName: String? = null,
        var resType: BindResType,
        var enable: Boolean = true,
        var optional: Boolean = false,
        var bindAnnotation: PsiAnnotation? = null
) {

    var refactorSuccess: Boolean = false
    var bindView: PsiReference? = null

    fun genMappingField() {
        val builder = StringBuilder(Config.fieldNamePrefix)
        if (idResExpr.contains("_")) {
            val split = idResExpr.toLowerCase().split("_".toRegex()).toTypedArray()
            for (s in split) {
                if (s.isNotEmpty()) {
                    val c = s.substring(0, 1).toUpperCase()
                    builder.append(c).append(s.substring(1))
                }
            }
        } else {
            val c = idResExpr.substring(0, 1).toUpperCase()
            builder.append(c).append(idResExpr.substring(1))
        }
        filedName = builder.toString()
    }
}