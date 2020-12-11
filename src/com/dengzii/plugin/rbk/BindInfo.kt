package com.dengzii.plugin.rbk

/**
 *
 * @author https://github.com/dengzii
 */
class BindInfo(
        val type: String,
        val idResExpr: String,
        var fileName: String? = null,
        var enable: Boolean = true,
        var optional: Boolean = false
) {

    fun genMappingField() {
        val builder = StringBuilder(Config.FIELD_NAME_PREFIX)
        if (idResExpr.contains("_")) {
            val split = idResExpr.toLowerCase().split("_".toRegex()).toTypedArray()
            for (s in split) {
                if (s.isNotEmpty()) {
                    val c = s.substring(0, 1).toUpperCase()
                    builder.append(c).append(s.substring(1))
                }
            }
        } else {
            val c = idResExpr.substring(0, 1).toUpperCase()
            builder.append(c).append(idResExpr.substring(1))
        }
        fileName = builder.toString()
    }
}