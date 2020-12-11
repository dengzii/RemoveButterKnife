package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.utils.PsiClassUtils.getMethod
import com.intellij.psi.*
import java.util.*

/**
 *
 * @author https://github.com/dengzii
 */
class JavaCase : BaseCase() {

    //    private static final InsertPlace mFieldInsertPlace = InsertPlace.FIRST; // TODO: 2019/9/30 add insert place support
    //    private static final InsertPlace mFindViewInsertPlace = InsertPlace.FIRST; // TODO: 2019/9/30

    override fun dispose(psiElement: PsiFile, bindInfos: List<BindInfo>) {
        if (!psiElement.language.`is`(Config.JAVA)) {
            next(psiElement, bindInfos)
            return
        }
        val factory = JavaPsiFacade.getElementFactory(psiElement.project)
        val psiClass = getPsiClass(psiElement)
        if (Objects.isNull(psiClass)) {
            return
        }
        val initViewMethod = genInitViewMethod(factory, psiClass)
        val allFields = psiClass!!.allFields.map { it.name }
        for (viewInfo in bindInfos) {
            if (!viewInfo.enable) continue
            if (!allFields.contains(viewInfo.fileName)) {
                psiClass.add(genViewDeclareField(factory, viewInfo))
            }
            if (!Objects.isNull(initViewMethod!!.body)) {
                initViewMethod.body!!.add(genFindViewStatement(factory, viewInfo))
            }
        }
    }

    private fun getPsiClass(file: PsiFile): PsiClass? {
        val psiElements = file.children
        for (element in psiElements) {
            if (element is PsiClass) {
                return element
            }
        }
        return null
    }

    private fun genViewDeclareField(factory: PsiElementFactory, bindInfo: BindInfo): PsiField {
        val statement = String.format(STATEMENT_FIELD, bindInfo.type, bindInfo.fileName)
        return factory.createFieldFromText(statement, null)
    }

    private fun genInitViewMethod(factory: PsiElementFactory, psiClass: PsiClass?): PsiMethod? {
        var method1 = getMethod(psiClass!!, Config.METHOD_INIT_VIEW)
        if (Objects.isNull(method1)) {
            method1 = factory.createMethod(Config.METHOD_INIT_VIEW, PsiType.VOID)
            psiClass.add(method1)
        }
        method1!!.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)
        return method1
    }

    private fun genFindViewStatement(factory: PsiElementFactory, bindInfo: BindInfo): PsiStatement {
        val findStatement = String.format(STATEMENT_FIND_VIEW, bindInfo.fileName, bindInfo.idResExpr)
        return factory.createStatementFromText(findStatement, null)
    }

    companion object {
        private const val STATEMENT_FIELD = "private %s %s;"
        private const val STATEMENT_FIND_VIEW = "%s = findViewById(%s);\n"
    }
}