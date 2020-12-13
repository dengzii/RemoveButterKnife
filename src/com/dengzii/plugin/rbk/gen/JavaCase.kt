package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.intellij.psi.*

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
        val psiClass = getPsiClass(psiElement) ?: return
        val initViewMethod = genInitViewMethod(factory, psiClass)
        val allFields = psiClass.allFields.map { it.name }

        for (viewInfo in bindInfos) {
            if (!viewInfo.enable) continue
            if (viewInfo.fileName !in allFields) {
                psiClass.add(genViewDeclareField(factory, viewInfo))
            }
            if (initViewMethod.body != null) {
                val findViewStatement = genFindViewStatement(factory, viewInfo)
                initViewMethod.body?.add(factory.createStatementFromText("int a = 1;", null))
                initViewMethod.body!!.add(findViewStatement)
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

    private fun genInitViewMethod(factory: PsiElementFactory, psiClass: PsiClass): PsiMethod {
        var initViewMethod: PsiMethod? = psiClass.findMethodsByName(Config.METHOD_INIT_VIEW, false).firstOrNull()
        if (initViewMethod == null) {
            initViewMethod = factory.createMethod(Config.METHOD_INIT_VIEW, PsiType.VOID)
            initViewMethod.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)
            psiClass.add(initViewMethod)
            psiClass.findMethodsByName(Config.METHOD_INIT_VIEW, false).firstOrNull()?.let {
                initViewMethod = it
            }
        }
        return initViewMethod as PsiMethod
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