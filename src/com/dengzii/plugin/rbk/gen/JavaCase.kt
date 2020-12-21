package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.Constants
import com.dengzii.plugin.rbk.utils.acceptElement
import com.dengzii.plugin.rbk.utils.getParameterExpressions
import com.dengzii.plugin.rbk.utils.getParameterTypes
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade
import com.intellij.refactoring.extractMethod.newImpl.ExtractMethodHelper.addSiblingAfter
import javax.swing.undo.UndoManager

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
        val methodBody = bindViewMethod.body ?: return

        // add bind view statement to method body
        for (viewInfo in bindInfos) {
            if (!viewInfo.enable) {
                continue
            }
            genViewDeclareField(factory, viewInfo, psiClass)
            val bindStatement = genBindStatement(factory, viewInfo, paramNameBindSourceView)
            methodBody.add(bindStatement)
            viewInfo.bindAnnotation?.delete()
        }

        // insert invoke bind view method to method.
        insertInvokeBindViewMethodStatement(psiClass, factory)
    }

    /**
     * Insert call bind view method statement to specified method in [Config.insertBindViewMethodIntoMethod].
     * Search method name in class in order of list, the call statement will insert into first name matched method,
     * if none method call statement matched, the end of method will be insert.
     *
     * The insert place determine by [-]
     */
    @Throws(NoSuchMethodException::class, IllegalStateException::class)
    private fun insertInvokeBindViewMethodStatement(psiClass: PsiClass, factory: PsiElementFactory): Array<PsiParameter>? {

        var bindViewMethodParameterList: Array<PsiParameter>? = null
        val insertAfterMethod = Config.insertCallBindViewMethodAfterCallMethod
        val insertToMethod = Config.insertBindViewMethodIntoMethod
        val bindViewMethodName = Config.methodNameBindView

        var invokerMethodBody: PsiCodeBlock? = null
        // find statement insertion target method in class.
        for (m in insertToMethod) {
            invokerMethodBody = psiClass.findMethodsByName(m, false).firstOrNull()?.body ?: continue
        }
        invokerMethodBody
                ?: throw NoSuchMethodError("Method $insertToMethod not found in class ${psiClass.qualifiedName}.")

        var callStatement = factory.createStatementFromText("${bindViewMethodName}();\n", null)

        val methodExpressionMap = mutableMapOf<String, MutableList<PsiMethodCallExpression>>()
        invokerMethodBody.acceptElement { element ->
            if (element is PsiExpressionStatement) {
                val expr = element.expression as? PsiMethodCallExpression ?: return@acceptElement
                val m = expr.resolveMethod() ?: return@acceptElement
                val expressions = methodExpressionMap.getOrPut(m.name) { mutableListOf() }
                expressions.add(expr)
            }
        }
        // already call bind view method
        if (bindViewMethodName in methodExpressionMap) {
            val exprText = methodExpressionMap[bindViewMethodName]?.firstOrNull()?.text
            if (exprText != null) {
                if (exprText.startsWith("this.${bindViewMethodName}") || exprText.startsWith(bindViewMethodName)) {
                    return null
                }
            }
        }
        var inserted = false

        // replace ButterKnife bind.
        val bind = methodExpressionMap["bind"]?.first { "ButterKnife.bind" in it.text }
        if (bind != null) {
            val paramExprs = bind.getParameterExpressions()
            val paramTypes = bind.getParameterTypes()
            if (paramTypes.isNullOrEmpty() ||
                    paramTypes.size != paramTypes.size) {
                throw IllegalStateException("Unexpected parameters size.")
            }
            val argSourceIndex = paramTypes.size - 1
            val sourceType = paramTypes[argSourceIndex].type
            val sourceExpr = paramExprs[argSourceIndex].text

            val source = if (sourceType == Config.PsiTypes.androidView) {
                sourceExpr
            } else {
                "$sourceExpr.getWindow().getDecorView()"
            }
            callStatement = factory.createStatementFromText("$bindViewMethodName($source);\n", null)
            bind.parent.addSiblingAfter(callStatement)
            bind.parent.delete()
            bindViewMethodParameterList = paramTypes
            inserted = true
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
            invokerMethodBody.addAfter(callStatement, invokerMethodBody.lastBodyElement)
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
        var ret: PsiMethod? = psiClass.findMethodsByName(Config.methodNameBindView, false).firstOrNull()
        if (ret == null) {
            ret = factory.createMethod(Config.methodNameBindView, PsiType.VOID)
            ret.modifierList.setModifierProperty(PsiModifier.PRIVATE, true)
            ret = psiClass.add(ret) as PsiMethod
        }
        val paramBindView = ret.parameterList.parameters.filter {
            it.name == paramNameBindSourceView
        }
        if (paramBindView.isNullOrEmpty()) {
            ret.parameterList.add(factory.createParameter(paramNameBindSourceView, Config.PsiTypes.androidView))
        }
        return ret
    }

    private fun genBindStatement(factory: PsiElementFactory, bindInfo: BindInfo, source: String): PsiStatement {

        val findStatement = String.format(statementFindView, bindInfo.filedName, source, bindInfo.idResExpr)
        return factory.createStatementFromText(findStatement, null)
    }

    companion object {
        private const val paramNameBindSourceView = "bindSource"
        private const val statementField = "private %s %s;"
        private const val statementFindView = "%s = %s.findViewById(%s);\n"
        private val codeFormatter = CodeFormatterFacade(CodeStyleSettings.getDefaults(),
                Language.findLanguageByID("JAVA"), false)
    }
}