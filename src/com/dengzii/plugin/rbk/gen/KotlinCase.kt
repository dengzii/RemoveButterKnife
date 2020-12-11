package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.Config
import com.dengzii.plugin.rbk.BindInfo
import com.dengzii.plugin.rbk.utils.KtPsiUtils.getFirstFun
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

    private val mFieldPlace = InsertPlace.FIRST
    private val mMethodPlace = InsertPlace.FIRST

    companion object {
        private const val STATEMENT_LAZY_INIT_VIEW = " %s val %s by lazy  { findViwById<%s>(%s) }"
        private const val FUN_INIT_VIEW = "private fun %s() {\n\n}"
        private const val MODIFIER_INIT_VIEW_PROPERTY = "private"
        private const val INIT_VIEW_BY_LAZY = true
    }

    override fun dispose(psiElement: PsiFile, bindInfos: List<BindInfo>) {
        if (!psiElement.language.`is`(Config.KOTLIN)) {
            next(psiElement, bindInfos)
            return
        }
        val ktClass = getKtClass(psiElement)
        if (Objects.isNull(ktClass)) {
            return
        }
        val ktPsiFactory = KtPsiFactory(psiElement.project)
        checkAndCreateClassBody(ktClass, ktPsiFactory)
        if (!INIT_VIEW_BY_LAZY) {
            insertInitViewKtFun(ktClass, ktPsiFactory)
        }
        val properties = ktClass!!.getProperties().map { it.name }
        for (viewInfo in bindInfos) {
            if (properties.contains(viewInfo.fileName)){
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
                bindInfo.fileName,
                bindInfo.type,
                bindInfo.idResExpr)
        val ktProperty = ktPsiFactory.createProperty(lazyViewProperty)
        body.addAfter(ktProperty, lBrace)
    }

    private fun insertInitViewKtFun(ktClass: KtClass?, factory: KtPsiFactory) {
        val firstFun: PsiElement? = getFirstFun(ktClass!!)
        val ktClassBody = ktClass.body
        val rBrace = ktClassBody!!.rBrace
        val initViewFun: KtFunction = factory.createFunction(String.format(FUN_INIT_VIEW, Config.METHOD_INIT_VIEW))
        ktClassBody.addBefore(initViewFun, if (Objects.isNull(firstFun)) rBrace else firstFun)
    }

    private fun checkAndCreateClassBody(ktClass: KtClass?, ktPsiFactory: KtPsiFactory) {
        if (Objects.isNull(ktClass!!.body)) {
            ktClass.add(ktPsiFactory.createEmptyClassBody())
        }
    }

    private fun getKtClass(file: PsiFile): KtClass? {
        val psiElements = file.children
        for (element in psiElements) {
            if (element is KtClass) {
                return element
            }
        }
        return null
    }
}