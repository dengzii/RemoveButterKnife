package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.Constants
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade

/**
 *
 * @author https://github.com/dengzii
 */
class JavaCase : BaseCase() {

    override fun dispose(psiElement: PsiFile, bindInfos: List<BindInfo>) {
        if (!psiElement.language.`is`(Config.LangeJava)) {
            next(psiElement, bindInfos)
            return
        }
        val factory = JavaPsiFacade.getElementFactory(psiElement.project)
        val psiClass = getPsiClass(psiElement)

        if (psiClass == null) {
            println("No class found.")
            // TODO notify not class found.
            return
        }

        // generate bind view id method
        val bindViewMethod = genInitViewMethod(factory, psiClass)
        // add bind view statement to method body
        for (viewInfo in bindInfos) {
            if (!viewInfo.enable) {
                continue
            }
            genViewDeclareField(factory, viewInfo, psiClass)

            val methodBody = bindViewMethod.body
            if (methodBody == null) {
                viewInfo.refactorSuccess = false
                return
            }
            val findViewStatement = genFindViewStatement(factory, viewInfo)
            methodBody.add(findViewStatement)
            viewInfo.bindAnnotation?.delete()
        }

        psiClass.acceptChildren(object : PsiElementVisitor() {

        })

        var superClass = psiClass.superClass
        while (superClass != null) {
            when (superClass.qualifiedName) {
                "android.app.Dialog" -> {

                }
                "android.app.Activity" -> {

                }
                "android.view.View" -> {

                }
                else -> {

                }
            }
            superClass = superClass.superClass
        }

        // insert invoke bind view method to method.
        insertInvokeBindViewMethodStatement(psiClass, factory)
    }

    private fun insertInvokeBindViewMethodStatement(psiClass: PsiClass, factory: PsiElementFactory) {

        for (methodName in Config.bindViewMethodInvoker) {
            val invoker = psiClass.findMethodsByName(methodName, false).firstOrNull() ?: continue
            val body = invoker.body ?: continue

            // default insert to the end of method
            var callSuper: PsiElement? = body.lastBodyElement

            body.acceptChildren(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    super.visitElement(element)
                    if (element is PsiExpressionStatement) {
                        val expr = element.expression as? PsiMethodCallExpression ?: return
                        // after supper.onCreate()
                        if (expr.text == "super.onCreate()") {
                            callSuper = element
                        }
                        // after setContentView
                        if (expr.resolveMethod()?.name == "setContentView") {
                            callSuper = element
                        }
                        // after inflater
                        if (expr.resolveMethod()?.name == "inflate") {
                            callSuper = element
                        }
                    }
                }
            })
            body.addAfter(factory.createStatementFromText("${Config.methodNameBindView}();\n", null), callSuper)
            break
        }
    }

    /**
     * Search ButterKnife bind statement in each class method.
     *
     * @param psiClass the class.
     * @return the ButterKnife bind statement.
     */
    private fun findButterKnifeBindStatement(psiClass: PsiClass): PsiExpressionStatement? {
        var butterKnifeBindStatement: PsiExpressionStatement? = null
        var foundFlag = false
        psiClass.allMethods.forEach {
            it.body?.acceptChildren(object : PsiElementVisitor() {
                override fun visitElement(element: PsiElement) {
                    super.visitElement(element)
                    if (foundFlag) {
                        return
                    }
                    if (element is PsiExpressionStatement && element.expression is PsiMethodCallExpression) {
                        val methodCallExpression = element.expression as PsiMethodCallExpression
                        val methodClassFullName = methodCallExpression.resolveMethod()?.containingClass?.qualifiedName
                        if (methodClassFullName != Constants.CLASS_BUTTTER_KNIFE) {
                            return
                        }
                        butterKnifeBindStatement = element
                        foundFlag = true
                    }
                }
            })
        }
        return butterKnifeBindStatement
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

    private fun genViewDeclareField(factory: PsiElementFactory, bindInfo: BindInfo, psiClass: PsiClass): PsiField {
        var psiField = psiClass.findFieldByName(bindInfo.filedName, false)
        if (psiField == null) {
            val statement = String.format(statementField, bindInfo.viewClass, bindInfo.filedName)
            psiField = factory.createFieldFromText(statement, null)
        } else {
            if (Config.addPrivateModifier) {
                psiField.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
            }
        }
        if (Config.formatCode) {
            psiField.acceptChildren(object : PsiElementVisitor() {
                override fun visitWhiteSpace(space: PsiWhiteSpace) {
                    super.visitWhiteSpace(space)
                    if (space.text == "\n") {
                        space.delete()
                    }
                }
            })
//            psiField = codeFormatter.processElement(psiField.node).getPsi(PsiField::class.java)
        }
        return psiField
    }

    private fun genInitViewMethod(factory: PsiElementFactory, psiClass: PsiClass): PsiMethod {
        var initViewMethod: PsiMethod? = psiClass.findMethodsByName(Config.methodNameBindView, false).firstOrNull()
        if (initViewMethod == null) {
            initViewMethod = factory.createMethod(Config.methodNameBindView, PsiType.VOID)
            initViewMethod.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)
            initViewMethod = psiClass.add(initViewMethod) as PsiMethod
        }
        return initViewMethod
    }

    private fun genFindViewStatement(factory: PsiElementFactory, bindInfo: BindInfo): PsiStatement {
        val findStatement = String.format(statementFindView, bindInfo.filedName, bindInfo.idResExpr)
        return factory.createStatementFromText(findStatement, null)
    }

    companion object {
        private const val statementField = "private %s %s;"
        private const val statementFindView = "%s = findViewById(%s);\n"
        private val codeFormatter = CodeFormatterFacade(CodeStyleSettings.getDefaults(),
                Language.findLanguageByID("JAVA"), false)
    }
}