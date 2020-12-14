package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiFile
import com.intellij.util.ThrowableRunnable

/**
 *
 * @author https://github.com/dengzii
 */
class FindViewCodeWriter private constructor(
        private val psiFile: PsiFile,
        private val bindInfos: List<BindInfo>
) : ThrowableRunnable<RuntimeException?> {

    companion object {
        fun run(psiFile: PsiFile, bindInfos: List<BindInfo>) {
            WriteCommandAction.writeCommandAction(psiFile.project)
                    .run(FindViewCodeWriter(psiFile, bindInfos))
        }
    }

    @Throws(RuntimeException::class)
    override fun run() {
        val javaCase = JavaCase()
        javaCase.setNext(KotlinCase())
        javaCase.dispose(psiFile, bindInfos)
    }
}