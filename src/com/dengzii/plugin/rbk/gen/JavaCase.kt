package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.BindType
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.Constants
import com.dengzii.plugin.rbk.utils.acceptElement
import com.dengzii.plugin.rbk.utils.addLast
import com.dengzii.plugin.rbk.utils.getParameterExpressions
import com.dengzii.plugin.rbk.utils.getParameterTypes
import com.intellij.lang.Language
import com.intellij.psi.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.impl.source.codeStyle.CodeFormatterFacade
import com.intellij.refactoring.extractMethod.newImpl.ExtractMethodHelper.addSiblingAfter

/**
 *
 * @author https://github.com/dengzii
 */
class JavaCase : BaseCase() {

    private lateinit var factory: PsiElementFactory

    override fun dispose(psiClass: PsiClass, bindInfos: List<BindInfo>) {
        if (!psiClass.language.`is`(Config.LangeJava)) {
            next(psiClass, bindInfos)
            return
        }
        if (!this::factory.isInitialized) {
            factory = JavaPsiFacade.getElementFactory(psiClass.project)
        }
        // generate bind resource id to field method
        val methodBody = insertBindResourceMethod(psiClass).body!!

        // add bind view statement to method body
        for (bindInfo in bindInfos) {
            if (!bindInfo.enable) {
                continue
            }
            insertViewDeclareField(bindInfo, psiClass)
            insertBindResourceStatement(bindInfo, methodBody)
        }
        for (bindInfo in bindInfos) {
            if (bindInfo.isEventBind) {
                insertBindEvent(bindInfo, methodBody)
            }
        }
        // insert invoke bind view method to method.
        insertInvokeBindViewMethodStatement(psiClass)
    }

    /**
     * Insert call bind view method statement to specified method in [Config.insertBindViewMethodIntoMethod].
     * Search method name in class in order of list, the call statement will insert into first name matched method,
     * if none method call statement matched, the end of method will be insert.
     *
     * The insert place determine by [-]
     */
    @Throws(NoSuchMethodException::class, IllegalStateException::class)
    private fun insertInvokeBindViewMethodStatement(psiClass: PsiClass): Array<PsiParameter>? {

        var bindViewMethodParameterList: Array<PsiParameter>? = null
        val insertAfterMethod = Config.insertCallBindViewMethodAfterCallMethod
        val insertToMethod = Config.insertBindViewMethodIntoMethod
        val bindViewMethodName = Config.methodNameBindView

        var invokerMethodBody: PsiCodeBlock? = null
        // find statement insertion target method in class.
        for (m in insertToMethod) {
            invokerMethodBody = psiClass.findMethodsByName(m, false).firstOrNull()?.body ?: continue
        }
        if (invokerMethodBody == null) {
            throw NoSuchMethodError("Method $insertToMethod not found in class ${psiClass.qualifiedName}.")
        }

        var inserted = false
        var callStatement = factory.createStatementFromText("${bindViewMethodName}(getWindow().getDecorView());\n", null)
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

        // replace ButterKnife bind.
        val bind = methodExpressionMap["bind"]?.first { "ButterKnife.bind" in it.text }
        if (bind != null) {
            val paramExprs = bind.getParameterExpressions()
            val paramTypes = bind.getParameterTypes()
            if (paramTypes.isNullOrEmpty() || paramTypes.size != paramTypes.size) {
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
            invokerMethodBody.addLast(callStatement)
        }
        return bindViewMethodParameterList
    }

    /**
     * Insert `setXxxListener` code to bind method.
     *
     * @param bindInfo the event bind info.
     * @param bindMethodBody the bind method body.
     */
    private fun insertBindEvent(bindInfo: BindInfo, bindMethodBody: PsiCodeBlock): Boolean {

        val eventMethodParams = bindInfo.bindMethod?.parameterList?.parameters
        if (eventMethodParams != null && eventMethodParams.size > 1) {
            return false
        }
        val type = eventMethodParams?.getOrNull(0)?.type?.let {
            if (it == Config.PsiTypes.androidView) "" else "(${it.canonicalText})"
        }
        val castParam = if (type == null) "" else "${type}v"
        when (bindInfo.type) {
            BindType.OnClick -> {
                val statement = """
                    ${bindInfo.filedName}.setOnClickListener(v -> {
                        ${bindInfo.bindMethod!!.name}(${castParam});
                    });
                """.trimIndent()
                val psiStatement = factory.createStatementFromText(statement, null)
                bindMethodBody.addLast(psiStatement)
                bindInfo.bindAnnotation?.delete()
            }
            BindType.OnLongClick -> {
                val statement = """
                    ${bindInfo.filedName}.setOnLongClickListener(v -> {
                        ${bindInfo.bindMethod!!.name}(${castParam});
                    });
                """.trimIndent()
                val psiStatement = factory.createStatementFromText(statement, null)
                bindMethodBody.addLast(psiStatement)
                bindInfo.bindAnnotation?.delete()
            }
            else -> {
                // TODO add more event listener support.
            }
        }
        return true
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

    private fun insertViewDeclareField(bindInfo: BindInfo, psiClass: PsiClass): PsiField {
        var psiField = psiClass.findFieldByName(bindInfo.filedName, false)
        if (psiField == null) {
            val statement = "private ${bindInfo.viewClass} ${bindInfo.filedName};"
            psiField = factory.createFieldFromText(statement, null)
            psiClass.addAfter(psiField, psiClass.fields.lastOrNull())
        } else {
            if (Config.addPrivateModifier) {
                psiField.modifierList?.setModifierProperty(PsiModifier.PRIVATE, true)
            }
        }
        psiField.acceptChildren(object : PsiElementVisitor() {
            override fun visitWhiteSpace(space: PsiWhiteSpace) {
                super.visitWhiteSpace(space)
                if (space.text == "\n") {
                    space.delete()
                }
            }
        })
        return psiField
    }

    private fun insertBindResourceMethod(psiClass: PsiClass): PsiMethod {
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

    private fun insertBindResourceStatement(bindInfo: BindInfo, bindMethodBody: PsiCodeBlock) {
        var statementTemplate = Config.resBindStatement.getOrElse(bindInfo.type) { "" }
        if (bindInfo.isEventBind){
            statementTemplate = Config.resBindStatement.getValue(BindType.View)
        }
        if (statementTemplate.isBlank()) {
            //throw IllegalStateException("Unable to bind resource to field: unknown resource type.")
            return
        }
        val resourceExpression = statementTemplate
                .replace("%{SOURCE}", paramNameBindSourceView)
                .replace("%{RES_ID}", bindInfo.idResExpr)
                .replace("%{THEME}", "${paramNameBindSourceView}.getContext().getTheme()")

        val bindStatement = "%s = %s;".format(bindInfo.filedName, resourceExpression)
        val bindPsiStatement = factory.createStatementFromText(bindStatement, null)
        bindMethodBody.addLast(bindPsiStatement)
        if (!bindInfo.isEventBind) {
            bindInfo.bindAnnotation?.delete()
        }
    }

    companion object {
        private const val paramNameBindSourceView = "bindSource"
        private val codeFormatter = CodeFormatterFacade(CodeStyleSettings.getDefaults(),
                Language.findLanguageByID("JAVA"), false)
    }
}