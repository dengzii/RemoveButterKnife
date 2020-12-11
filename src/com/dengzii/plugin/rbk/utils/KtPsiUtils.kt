package com.dengzii.plugin.rbk.utils

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction
import java.util.*

/**
 *
 * @author https://github.com/dengzii
 */
object KtPsiUtils {

    fun getFirstFun(ktClass: KtClass): KtFunction? {
        val functions = getFunList(ktClass)
        return if (functions.isNotEmpty()) {
            functions[0]
        } else null
    }

    private fun getFunList(ktClass: KtClass): List<KtFunction> {
        val result: MutableList<KtFunction> = ArrayList()
        if (Objects.isNull(ktClass.body)) {
            return result
        }
        val elements = ktClass.body!!.children
        for (element in elements) {
            if (element is KtFunction) {
                result.add(element)
            }
        }
        return result
    }
}