package com.dengzii.plugin.rbk.gen

import com.dengzii.plugin.rbk.BindInfo
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.util.ThrowableRunnable

/**
 *
 * @author https://github.com/dengzii
 */
class CodeWriter private constructor(
        private val psiClass: PsiClass,
        private val bindInfos: List<BindInfo>
) : ThrowableRunnable<RuntimeException?> {

    companion object {
        fun run(psiClass: PsiClass, bindInfos: List<BindInfo>) {
            WriteCommandAction.writeCommandAction(psiClass.project)
                    .run(CodeWriter(psiClass, bindInfos))
        }
    }

    @Throws(RuntimeException::class)
    override fun run() {
        val javaCase = JavaCase()
        javaCase.setNext(KotlinCase())
        javaCase.dispose(psiClass, bindInfos)
    }
}