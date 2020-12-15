package com.dengzii.plugin.rbk.gen.insertion

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiExpressionStatement
import com.intellij.psi.PsiMethodCallExpression
import com.jetbrains.rd.util.printlnError

class MethodCallStatementInsertion : Insertion {

    override fun insert(target: PsiElement, source: PsiElement) {
        target.acceptChildren(object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is PsiExpressionStatement) {
                    if (element.expression is PsiMethodCallExpression) {
                        visitMethodCallStatement(element.expression as PsiMethodCallExpression, target)
                    }
                }
            }
        })
    }

    private fun visitMethodCallStatement(psiMethodCallExpression: PsiMethodCallExpression, target: PsiElement) {

        val resolvedMethod = psiMethodCallExpression.resolveMethod()
        if (resolvedMethod == null) {
            printlnError("can not resolve method: $psiMethodCallExpression")
            return
        }
        val methodContainingClass = resolvedMethod.containingClass
        val methodName = resolvedMethod.name
        val methodParam = resolvedMethod.parameterList


    }
}