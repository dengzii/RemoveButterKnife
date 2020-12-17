package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile

/**
 *
 * @author https://github.com/dengzii
 */
abstract class BaseCase {

    private var next: BaseCase? = null

    abstract fun dispose(psiClass: PsiClass, bindInfos: List<BindInfo>)

    fun setNext(next: BaseCase?) {
        this.next = next
    }

    protected operator fun next(): BaseCase? {
        return next
    }

    protected fun next(psiClass: PsiClass, bindInfos: List<BindInfo>) {
        if (next != null) {
            next!!.dispose(psiClass, bindInfos)
        }
    }
}