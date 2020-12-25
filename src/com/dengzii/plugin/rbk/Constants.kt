package com.dengzii.plugin.rbk

import com.intellij.lang.Language

@Suppress("SpellCheckingInspection")
object Constants {

    val langJava = Language.findLanguageByID("JAVA")
    val langKotlin = Language.findLanguageByID("kotlin")

    const val CLASS_BUTTTER_KNIFE = "butterknife.ButterKnife"

    const val ButterKnifeOptional = "butterknife.Optional"

    const val ButterKnifeOnClick = "butterknife.OnClick"
    const val ButterKnifeOnLongClick = "butterknife.OnLongClick"
    const val ButterKnifeOnCheckedChanged = "butterknife.OnCheckedChanged"
    const val ButterKnifeOnEditorAction = "butterknife.OnEditorAction"
    const val ButterKnifeOnFocusChange = "butterknife.OnFocusChange"
    const val ButterKnifeOnItemClick = "butterknife.OnItemClick"
    const val ButterKnifeOnItemSelected = "butterknife.OnItemSelected"
    const val ButterKnifeOnPageChange = "butterknife.OnPageChange"
    const val ButterKnifeOnTouch = "butterknife.OnTouch"

    const val ButterKnifeBindView = "butterknife.BindView"
    const val ButterKnifeBindArray = "butterknife.BindArray"
    const val ButterKnifeBindString = "butterknife.BindString"
    const val ButterKnifeBindDrawable = "butterknife.BindDrawable"
    const val ButterKnifeBindBitmap = "butterknife.BindBitmap"
    const val ButterKnifeBindInt = "butterknife.BindInt"
    const val ButterKnifeBindFloat = "butterknife.BindFloat"
    const val ButterKnifeBindViews = "butterknife.BindViews"
    const val ButterKnifeBindDimen = "butterknife.BindDimen"
    const val ButterKnifeBindColor = "butterknife.BindColor"
    const val ButterKnifeBindBool = "butterknife.BindBool"
    const val ButterKnifeBindAnim = "butterknife.BindAnim"

    val ButterKnifeBindMethodAnnotation = arrayOf(
            ButterKnifeOnClick,
            ButterKnifeOnLongClick,
            ButterKnifeOnCheckedChanged,
            ButterKnifeOnEditorAction,
            ButterKnifeOnFocusChange,
            ButterKnifeOnItemClick,
            ButterKnifeOnItemSelected,
            ButterKnifeOnPageChange,
            ButterKnifeOnTouch
    )

    val ButterKnifeBindFieldAnnotation = arrayOf(
            ButterKnifeBindView,
            ButterKnifeBindArray,
            ButterKnifeBindString,
            ButterKnifeBindDrawable,
            ButterKnifeBindBitmap,
            ButterKnifeBindInt,
            ButterKnifeBindFloat,
            ButterKnifeBindViews,
            ButterKnifeBindDimen,
            ButterKnifeBindColor,
            ButterKnifeBindBool,
            ButterKnifeBindAnim
    )
}