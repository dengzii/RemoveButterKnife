package com.dengzii.plugin.rbk.gen.insertion

import com.intellij.psi.PsiElement

interface Insertion {
    fun insert(target: PsiElement, source: PsiElement)
}