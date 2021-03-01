package com.khoben.autotitle.common

import android.content.res.Resources
import android.graphics.Typeface
import android.text.TextUtils
import java.util.*

class FontProvider(private val resources: Resources) {
    private val typefaces: MutableMap<String?, Typeface?>
    private val fontNameToTypefaceFile: MutableMap<String?, String>
    private val fontNames: List<String?>

    /**
     * @param typefaceName must be one of the font names provided from [FontProvider.getFontNames]
     * @return the Typeface associated with `typefaceName`, or [Typeface.DEFAULT] otherwise
     */
    fun getTypeface(typefaceName: String?): Typeface? {
        return if (TextUtils.isEmpty(typefaceName)) {
            Typeface.DEFAULT
        } else {
            if (typefaces[typefaceName] == null) {
                typefaces[typefaceName] = Typeface.createFromAsset(
                    resources.assets,
                    "fonts/" + fontNameToTypefaceFile[typefaceName]
                )
            }
            typefaces[typefaceName]
        }
    }

    /**
     * use [to get Typeface for the font name][FontProvider.getTypeface]
     *
     * @return list of available font names
     */
    fun getFontNames(): List<String?> {
        return fontNames
    }

    /**
     * @return Default Font Name - **Helvetica**
     */
    fun getDefaultFontName(): String {
        return DEFAULT_FONT_NAME
    }

    companion object {
        private const val DEFAULT_FONT_NAME = "Helvetica"
    }

    init {
        typefaces = HashMap()

        // populate fonts
        fontNameToTypefaceFile = HashMap()
        fontNameToTypefaceFile["Arial"] = "arial.ttf"
        fontNameToTypefaceFile["Eutemia"] = "eutemia.ttf"
        fontNameToTypefaceFile["GREENPIL"] = "greenpil.ttf"
        fontNameToTypefaceFile["Grinched"] = "grinched.ttf"
        fontNameToTypefaceFile["Helvetica"] = "helvetica.ttf"
        fontNameToTypefaceFile["Libertango"] = "libertango.ttf"
        fontNameToTypefaceFile["Metal Macabre"] = "metalmacabre.ttf"
        fontNameToTypefaceFile["Parry Hotter"] = "parryhotter.ttf"
        fontNameToTypefaceFile["SCRIPTIN"] = "scriptin.ttf"
        fontNameToTypefaceFile["The Godfather v2"] = "thegodfather_v2.ttf"
        fontNameToTypefaceFile["Aka Dora"] = "akadora.ttf"
        fontNameToTypefaceFile["Waltograph"] = "waltograph42.ttf"
        fontNames = ArrayList(fontNameToTypefaceFile.keys)
    }
}