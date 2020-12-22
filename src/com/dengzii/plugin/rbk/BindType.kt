package com.dengzii.plugin.rbk

import com.intellij.psi.PsiAnnotation

enum class BindType {
    // Field bind
    Anim,
    Array,
    Bitmap,
    Bool,
    Color,
    Dimen,
    Drawable,
    Float,
    Int,
    String,
    View,
    Views,

    // Event bind
    OnCheckedChanged,
    OnEditorAction,
    OnFocusChange,
    OnItemSelected,
    OnItemClick,
    OnPageChange,
    OnTouch,
    OnLongClick,
    OnClick,

    Optional,
    Unknown;

    companion object {
        fun get(annotation: PsiAnnotation): BindType {
            return when (annotation.qualifiedName) {
                Constants.ButterKnifeBindAnim -> Anim
                Constants.ButterKnifeBindArray -> Array
                Constants.ButterKnifeBindBitmap -> Bitmap
                Constants.ButterKnifeBindBool -> Bool
                Constants.ButterKnifeBindColor -> Color
                Constants.ButterKnifeBindDimen -> Dimen
                Constants.ButterKnifeBindDrawable -> Drawable
                Constants.ButterKnifeBindFloat -> Float
                Constants.ButterKnifeBindInt -> Int
                Constants.ButterKnifeBindString -> String
                Constants.ButterKnifeBindView -> View
                Constants.ButterKnifeBindViews -> Views
                Constants.ButterKnifeOnCheckedChanged -> OnCheckedChanged
                Constants.ButterKnifeOnEditorAction -> OnEditorAction
                Constants.ButterKnifeOnFocusChange -> OnFocusChange
                Constants.ButterKnifeOnItemSelected -> OnItemSelected
                Constants.ButterKnifeOnItemClick -> OnItemClick
                Constants.ButterKnifeOnPageChange -> OnPageChange
                Constants.ButterKnifeOnTouch -> OnTouch
                Constants.ButterKnifeOnLongClick -> OnLongClick
                Constants.ButterKnifeOnClick -> OnClick
                Constants.ButterKnifeOptional -> Optional
                else -> Unknown
            }
        }
    }
}