package com.dengzii.plugin.rbk

import com.dengzii.plugin.rbk.gen.CodeWriter
import com.dengzii.plugin.rbk.ui.MainDialog
import com.dengzii.plugin.rbk.utils.ButterKnifeUtils
import com.dengzii.plugin.rbk.utils.getDeclaredClass
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

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
        Config.PsiTypes.init(project!!)
        val psiClass = psiFile!!.getDeclaredClass().firstOrNull() ?: return

        if (!ButterKnifeUtils.isImportedButterKnife(psiFile)) {
            return
        }
        MainDialog.show_ {
            val bindInfo = ButterKnifeUtils.getButterKnifeViewBindInfo(psiClass)
            if (bindInfo.isEmpty()) {
                return@show_
            }
            CodeWriter.run(psiClass, bindInfo)
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val project = e.project
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = true
        if (isNull(project, psiFile, editor)) {
            e.presentation.isEnabled = false
            return
        }
        val language = psiFile!!.language
        if (!language.`is`(Constants.langJava) && !language.`is`(Constants.langKotlin)) {
            e.presentation.isEnabled = false
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