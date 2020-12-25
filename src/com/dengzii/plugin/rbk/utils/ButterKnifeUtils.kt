package com.dengzii.plugin.rbk.utils

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.BindType
import com.dengzii.plugin.rbk.Constants
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiImportList
import com.intellij.psi.PsiModifier
import com.intellij.psi.impl.source.tree.java.PsiArrayInitializerMemberValueImpl
import com.intellij.psi.impl.source.tree.java.PsiIdentifierImpl
import com.intellij.psi.impl.source.tree.java.PsiReferenceExpressionImpl

object ButterKnifeUtils {

    fun getButterKnifeViewBindInfo(psiClass: PsiClass): List<BindInfo> {
        val ret = mutableListOf<BindInfo>()

        val fields = psiClass.fields.filter {
            it.modifierList.let { modifier ->
                modifier != null && !modifier.hasModifierProperty(PsiModifier.PRIVATE)
                        && !modifier.hasModifierProperty(PsiModifier.FINAL)
            }
        }
        for (field in fields) {
            var optional = false
            // each non-private fields, find fields annotated with `BindXxx`
            for (annotation in field.annotations) {
                val annotationTypeName = annotation.qualifiedName
                if (annotationTypeName == Constants.ButterKnifeOptional) {
                    optional = true
                    continue
                }
                if (annotationTypeName !in Constants.ButterKnifeBindFieldAnnotation) {
                    continue
                }
                val parameter = annotation.parameterList.attributes
                if (parameter.size != 1) {
                    continue
                }
                val viewIdExpr = (parameter[0].detachedValue as? PsiReferenceExpressionImpl)?.element
                if (viewIdExpr == null) {
                    System.err.println("$parameter is null")
                    continue
                }
                val info = BindInfo(
                        viewClass = field.type.canonicalText,
                        idResExpr = viewIdExpr.text,
                        filedName = (field.nameIdentifier as PsiIdentifierImpl).text,
                        bindAnnotation = annotation,
                        type = BindType.get(annotation)
                )
                info.optional = optional
                ret.add(info)
                break
            }
        }

        // method
        val methods = psiClass.methods.filter {
            it.modifierList.let { modifier ->
                !modifier.hasModifierProperty(PsiModifier.PRIVATE)
            }
        }
        for (method in methods) {
            for (annotation in method.annotations) {
                val annotationTypeName = annotation.qualifiedName
                if (annotationTypeName !in Constants.ButterKnifeBindMethodAnnotation) {
                    continue
                }
                val annotationParams = annotation.parameterList.attributes
                for (param in annotationParams) {
                    val value = param.detachedValue ?: continue
                    val viewIdExprs = when (value) {
                        is PsiArrayInitializerMemberValueImpl -> value.initializers.map { it.text }
                        is PsiReferenceExpressionImpl -> listOf(value.element.text)
                        else -> listOf()
                    }
                    viewIdExprs.forEach {
                        val info = BindInfo(
                                viewClass = "android.view.View",
                                idResExpr = it,
                                bindAnnotation = annotation,
                                type = BindType.get(annotation),
                                isEventBind = true,
                                bindMethod = method
                        )
                        ret.add(info)
                    }
                }
            }
        }
        return ret
    }

    fun isImportedButterKnife(psiFile: PsiFile): Boolean {
        var ret = false
        psiFile.acceptElement {
            if (it is PsiImportList && ! ret) {
                ret = it.allImportStatements.filter { i ->
                    i.text.contains("butterknife.")
                }.isNotEmpty()
            }
        }
        return ret
    }
}