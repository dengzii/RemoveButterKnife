package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.Constants
import com.dengzii.plugin.rbk.utils.KtPsiUtils.getFirstFun
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtPsiFactory
import java.util.*

/**
 *
 * @author https://github.com/dengzii
 */
class KotlinCase : BaseCase() {

    companion object {
        private const val STATEMENT_LAZY_INIT_VIEW = " %s val %s by lazy  { findViwById<%s>(%s) }"
        private const val FUN_INIT_VIEW = "private fun %s() {\n\n}"
        private const val MODIFIER_INIT_VIEW_PROPERTY = "private"
        private const val INIT_VIEW_BY_LAZY = true
    }

    override fun dispose(psiClass: PsiClass, bindInfos: List<BindInfo>) {
        if (!psiClass.language.`is`(Constants.langKotlin)) {
            next(psiClass, bindInfos)
            return
        }
        val ktClass = getKtClass(psiClass) ?: return
        val ktPsiFactory = KtPsiFactory(psiClass.project)
        checkAndCreateClassBody(ktClass, ktPsiFactory)
        if (!INIT_VIEW_BY_LAZY) {
            insertInitViewKtFun(ktClass, ktPsiFactory)
        }
        val properties = ktClass.getProperties().map { it.name }
        for (viewInfo in bindInfos) {
            if (properties.contains(viewInfo.filedName)) {
                continue
            }
            insertViewField(viewInfo, ktPsiFactory, ktClass)
        }
    }

    private fun insertViewField(bindInfo: BindInfo, ktPsiFactory: KtPsiFactory, ktClass: KtClass?) {
        val body = ktClass!!.body
        val lBrace = body!!.lBrace
        val lazyViewProperty = String.format(STATEMENT_LAZY_INIT_VIEW,
                MODIFIER_INIT_VIEW_PROPERTY,
                bindInfo.filedName,
                bindInfo.viewClass,
                bindInfo.idResExpr)
        val ktProperty = ktPsiFactory.createProperty(lazyViewProperty)
        body.addAfter(ktProperty, lBrace)
    }

    private fun insertInitViewKtFun(ktClass: KtClass?, factory: KtPsiFactory) {
        val firstFun: PsiElement? = getFirstFun(ktClass!!)
        val ktClassBody = ktClass.body
        val rBrace = ktClassBody!!.rBrace
        val initViewFun: KtFunction = factory.createFunction(String.format(FUN_INIT_VIEW, Config.methodNameBindView))
        ktClassBody.addBefore(initViewFun, if (Objects.isNull(firstFun)) rBrace else firstFun)
    }

    private fun checkAndCreateClassBody(ktClass: KtClass?, ktPsiFactory: KtPsiFactory) {
        if (Objects.isNull(ktClass!!.body)) {
            ktClass.add(ktPsiFactory.createEmptyClassBody())
        }
    }

    private fun getKtClass(psiClass: PsiClass): KtClass? {
        for (element in psiClass.children) {
            if (element is KtClass) {
                return element
            }
        }
        return null
    }
}