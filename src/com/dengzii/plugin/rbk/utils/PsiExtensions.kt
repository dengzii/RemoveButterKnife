package com.dengzii.plugin.rbk.utils

import com.intellij.psi.*


fun PsiFile.getDeclaredClass(): List<PsiClass> {
    val ret = mutableListOf<PsiClass>()
    acceptElement {
        if (it is PsiClass) {
            ret.add(it)
        }
    }
    return ret
}

fun PsiClass.getInnerClass(): List<PsiClass> {
    val ret = mutableListOf<PsiClass>()
    acceptElement {
        if (it is PsiClass) {
            ret.add(it)
            ret.addAll(it.getInnerClass())
        }
    }
    return ret
}

fun PsiClass.isExtendsFrom(qualifiedClassName: String): Boolean {
    var superClass = this.superClass
    var ret = false
    while (superClass != null && !ret) {
        ret = superClass.qualifiedName == qualifiedClassName
        superClass = superClass.superClass
    }
    return ret
}

inline fun PsiElement.acceptElement(crossinline visitor: (PsiElement) -> Unit) {
    acceptChildren(object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            visitor.invoke(element)
        }
    })
}

inline fun PsiElement.acceptExpression(crossinline visitor: (PsiElement) -> Unit) {
    acceptChildren(object : PsiElementVisitor() {
        override fun visitElement(element: PsiElement) {
            super.visitElement(element)
            if (element is PsiExpression) {
                visitor.invoke(element)
            }
        }
    })
}