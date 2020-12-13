package com.dengzii.plugin.rbk

import com.dengzii.plugin.rbk.gen.FindViewCodeWriter

import com.dengzii.plugin.rbk.utils.PsiFileUtils
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

/**
 *
 * @author https://github.com/dengzii
 */
class MainAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (isNull(project, psiFile, editor)) {
            return
        }
        val bindInfo = PsiFileUtils.getButterKnifeViewBindInfo(psiFile!!)
        WriteCommandAction.writeCommandAction(project).run(FindViewCodeWriter(psiFile, bindInfo))
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = true
        if (isNull(project, psiFile, editor)) {
            e.presentation.isEnabledAndVisible = false
            return
        }
        val language = psiFile!!.language
        if (!language.`is`(Config.JAVA) && !language.`is`(Config.KOTLIN)) {
            e.presentation.isEnabledAndVisible = false
        }
    }

    private fun isNull(vararg objects: Any?): Boolean {
        for (o in objects) {
            if (o == null) {
                return true
            }
        }
        return false
    }
}