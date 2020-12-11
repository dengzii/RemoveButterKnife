package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.intellij.psi.PsiFile
import com.intellij.util.ThrowableRunnable

/**
 *
 * @author https://github.com/dengzii
 */
class FindViewCodeWriter(
        private val psiFile: PsiFile,
        private val bindInfos: List<BindInfo>
) : ThrowableRunnable<RuntimeException?> {
    @Throws(RuntimeException::class)
    override fun run() {
        val javaCase = JavaCase()
        javaCase.setNext(KotlinCase())
        javaCase.dispose(psiFile, bindInfos)
    }
}