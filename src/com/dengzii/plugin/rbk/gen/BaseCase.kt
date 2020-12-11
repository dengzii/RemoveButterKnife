package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.intellij.psi.PsiFile

/**
 *
 * @author https://github.com/dengzii
 */
abstract class BaseCase {

    private var next: BaseCase? = null

    abstract fun dispose(psiElement: PsiFile, bindInfos: List<BindInfo>)

    fun setNext(next: BaseCase?) {
        this.next = next
    }

    protected operator fun next(): BaseCase? {
        return next
    }

    protected fun next(psiElement: PsiFile, bindInfos: List<BindInfo>) {
        if (next != null) {
            next!!.dispose(psiElement, bindInfos)
        }
    }
}