package com.dengzii.plugin.rbk.utils

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiMethod

/**
 *
 * @author https://github.com/dengzii
 */
object PsiClassUtils {

    fun getMethod(psiClass: PsiClass, methodName: String): PsiMethod? {
        val methods = getMethods(psiClass)
        for (method in methods) {
            if (method.name == methodName) {
                return method
            }
        }
        return null
    }

    fun getMethods(psiClass: PsiClass): Array<PsiMethod> {
        return psiClass.methods
    }
}