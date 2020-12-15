package com.dengzii.plugin.rbk.gen.insertion

import com.intellij.patterns.PsiStatementPattern
import com.intellij.psi.*

abstract class StatementInsertion: Insertion {

    override fun insert(target: PsiElement, source: PsiElement) {
        target.acceptChildren(object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is PsiExpressionStatement){

                }
            }
        })
    }
}