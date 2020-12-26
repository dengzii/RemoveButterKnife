package com.dengzii.plugin.rbk

import com.dengzii.plugin.rbk.gen.CodeWriter
import com.dengzii.plugin.rbk.ui.MainDialog
import com.dengzii.plugin.rbk.utils.ButterKnifeUtils
import com.dengzii.plugin.rbk.utils.NotificationUtils
import com.dengzii.plugin.rbk.utils.getDeclaredClass
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.idea.KotlinFileType

class ProjectAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        val allFile = mutableListOf<VirtualFile>()
        doEachFile(virtualFile, allFile)
        val psiFiles = allFile.filter {
            it.fileType is JavaFileType || it.fileType is KotlinFileType
        }.mapNotNull {
            PsiManager.getInstance(project).findFile(it)
        }
        MainDialog.show_ {
            Config.PsiTypes.init(project)
            psiFiles.forEach { psiFile ->
                val psiClass = psiFile.getDeclaredClass().firstOrNull() ?: return@forEach

                if (!ButterKnifeUtils.isImportedButterKnife(psiFile)) {
                    return@forEach
                }
                val bindInfo = ButterKnifeUtils.getButterKnifeViewBindInfo(psiClass)
                if (bindInfo.isEmpty()) {
                    return@forEach
                }
                CodeWriter.run(psiClass, bindInfo)
            }
            NotificationUtils.showInfo("Refactor complete, ${psiFiles.size} files are affected", "Remove ButterKnife")
        }
    }

    private fun doEachFile(virtualFile: VirtualFile, list: MutableList<VirtualFile>) {
        if (virtualFile.valid()) {
            if (virtualFile.isDirectory) {
                virtualFile.children.forEach {
                    doEachFile(it, list)
                }
            } else {
                list.add(virtualFile)
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)

        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)

        if (e.project == null || virtualFile == null) {
            e.presentation.isEnabled = false
            return
        }
        virtualFile.let {
            if (!it.valid() || !virtualFile.isDirectory || virtualFile.children.isEmpty()) {
                e.presentation.isEnabled = false
                return
            }
        }
        e.presentation.isEnabledAndVisible = true
    }

    private fun VirtualFile.valid(): Boolean {
        return isValid && isWritable && isInLocalFileSystem && exists()
    }
}