package com.dengzii.plugin.rbk.gen.insertion

import com.intellij.psi.*
import com.jetbrains.rd.util.printlnError

class MethodCallStatementInsertion(
        private val place: InsertPlace,
        private val predicate: InsertPredicate
) : Insertion {

    var inserted = false
    var insertionCount = 1

    interface InsertPredicate {
        fun insert(target: PsiElement, psiMethod: PsiMethod): Boolean
    }

    companion object {

        fun create(place: InsertPlace,
                   predicate: (target: PsiElement, psiMethod: PsiMethod) -> Boolean
        ): MethodCallStatementInsertion {
            return MethodCallStatementInsertion(place, object : InsertPredicate {
                override fun insert(target: PsiElement, psiMethod: PsiMethod): Boolean {
                    return predicate.invoke(target, psiMethod)
                }
            })
        }

        fun create(place: InsertPlace, method: Array<String>): MethodCallStatementInsertion {
            return MethodCallStatementInsertion(place, object : InsertPredicate {
                val methods = method.map {
                    if ("." in it) it else ".".plus(it)
                }
                override fun insert(target: PsiElement, psiMethod: PsiMethod): Boolean {
                    val clazzName = psiMethod.containingClass?.qualifiedName.orEmpty()
                    val m = "${clazzName}.${psiMethod.name}"
                    return m in this.methods
                }
            })
        }
    }

    override fun insert(target: PsiElement, insertion: PsiElement) {
        if (inserted) {
            return
        }
        target.acceptChildren(object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (!inserted && element is PsiExpressionStatement) {
                    val psiMethodCallExpression = element.expression as? PsiMethodCallExpression ?: return

                    val resolvedMethod = psiMethodCallExpression.resolveMethod()
                    if (resolvedMethod == null) {
                        printlnError("can not resolve method: $psiMethodCallExpression")
                        return
                    }
                    val insert = predicate.insert(target, resolvedMethod)
                    if (!insert) {
                        return
                    }

                    when (place) {
                        InsertPlace.BEFORE -> {
                            target.addBefore(insertion, element)
                        }
                        InsertPlace.AFTER -> {
                            target.addAfter(insertion, element)
                        }
                        InsertPlace.REPLACE -> {
                            element.replace(insertion)
                        }
                        else -> {
                            throw UnsupportedOperationException("Cannot insert $place to method.")
                        }
                    }
                    if (--insertionCount <= 0) {
                        inserted = true
                    }
                    // visitMethodCallStatement(element, expression, target, insertion)
                }
            }
        })
    }

    private fun visitMethodCallStatement(origin: PsiElement, psiMethodCallExpression: PsiMethodCallExpression,
                                         target: PsiElement, insertion: PsiElement) {
        val resolvedMethod = psiMethodCallExpression.resolveMethod()
        if (resolvedMethod == null) {
            printlnError("can not resolve method: $psiMethodCallExpression")
            return
        }
        val insert = predicate.insert(target, resolvedMethod)
        if (!insert) {
            return
        }

        when (place) {
            InsertPlace.BEFORE -> {
                target.addBefore(insertion, origin)
            }
            InsertPlace.AFTER -> {
                target.addAfter(insertion, origin)
            }
            InsertPlace.REPLACE -> {
                origin.replace(insertion)
            }
            else -> {
                throw UnsupportedOperationException("Cannot insert $place to method.")
            }
        }

        val param = psiMethodCallExpression.argumentList
        val methodContainingClass = resolvedMethod.containingClass
        val methodName = resolvedMethod.name
        val methodParam = resolvedMethod.parameterList

        println(param.expressions.joinToString { it.text })
        println("${methodContainingClass?.qualifiedName}.${methodName}(${methodParam.parameters.map { it.type.canonicalText }})")
    }


}