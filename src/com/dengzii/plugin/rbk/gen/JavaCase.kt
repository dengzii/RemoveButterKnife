package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.Constants
import com.dengzii.plugin.rbk.utils.acceptElement
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade
import com.intellij.refactoring.extractMethod.newImpl.ExtractMethodHelper.addSiblingAfter
import org.jetbrains.kotlin.idea.refactoring.getLineNumber
import java.util.*

/**
 *
 * @author https://github.com/dengzii
 */
class JavaCase : BaseCase() {

    override fun dispose(psiClass: PsiClass, bindInfos: List<BindInfo>) {
        if (!psiClass.language.`is`(Config.LangeJava)) {
            next(psiClass, bindInfos)
            return
        }
        val factory = JavaPsiFacade.getElementFactory(psiClass.project)

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

        // insert invoke bind view method to method.
        insertInvokeBindViewMethodStatement(psiClass, factory)
    }

    /**
     * Insert call bind view method statement to specified method in [Config.bindViewMethodInvoker].
     * Search method name in class in order of list, the call statement will insert into first name matched method,
     * if none method call statement matched, the end of method will be insert.
     *
     * The insert place determine by [-]
     */
    private fun insertInvokeBindViewMethodStatement(psiClass: PsiClass, factory: PsiElementFactory): Array<PsiExpression>? {

        var bindViewMethodParameterList: Array<PsiExpression>? = null
        val insertAfterMethod = Config.insertCallBindViewMethodAfterMethod

        for (methodName in Config.bindViewMethodInvoker) {
            // find statement insertion target method in class.
            val invokerMethod = psiClass.findMethodsByName(methodName, false).firstOrNull() ?: continue
            val methodBody = invokerMethod.body ?: continue

            val callStatement = factory.createStatementFromText("${Config.methodNameBindView}();\n", null)

            val methodExpressionMap = mutableMapOf<String, MutableList<PsiMethodCallExpression>>()
            methodBody.acceptElement { element ->
                if (element is PsiExpressionStatement) {
                    val expr = element.expression as? PsiMethodCallExpression ?: return@acceptElement
                    val m = expr.resolveMethod() ?: return@acceptElement
                    val expressions = methodExpressionMap.getOrPut(m.name) { mutableListOf() }
                    expressions.add(expr)
                }
            }
            var inserted = false

            // replace bind.
            methodExpressionMap["bind"]?.forEach {
                if ("ButterKnife.bind" in it.text) {
                    bindViewMethodParameterList = it.argumentList.expressions
                    it.parent.replace(callStatement)
                    inserted = true
                }
            }
            // if not bind, insert to other statement.
            if (!inserted) {
                for (m in insertAfterMethod) {
                    if (methodExpressionMap.containsKey(m)) {
                        val methodCallExpressions = methodExpressionMap[m]!!.first()
                        methodCallExpressions.parent.addSiblingAfter(callStatement)
                        inserted = true
                    }
                }
            }

            if (!inserted) {
                methodBody.addAfter(callStatement, methodBody.lastBodyElement)
            }
            break
        }
        return bindViewMethodParameterList
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