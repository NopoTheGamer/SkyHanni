package at.hannibal2.skyhanni.utils

import at.hannibal2.skyhanni.utils.compat.toChatFormatting
import net.minecraft.text.OrderedText
import net.minecraft.text.Style
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting

object OrderedTextUtils {
    private val CHROMA_COLOR = TextColor(0xFFFFFF, "chroma")
    @JvmStatic
    fun orderedTextToLegacyString(orderedText: OrderedText?): String {
        orderedText ?: return ""
        val sb = StringBuilder()
        var lastStyle = Style.EMPTY
        orderedText.accept { index, style, codePoint ->
            if (lastStyle != style) {
                sb.append(requiredStyleChangeString(lastStyle, style))
                lastStyle = style
            }
            sb.append(codePoint.toChar())
            true
        }
        return sb.toString().removeSuffix("§r").removePrefix("§r")
    }
    @JvmStatic
    fun legacyTextToOrderedText(legacyString: String?): OrderedText {
        return OrderedText { visitor ->
            var lastStyle = Style.EMPTY
            var wasLastStyle = false
            for (char in legacyString ?: "") {
                if (char == '§') {
                    wasLastStyle = true
                } else if (wasLastStyle) {
                    val formatting = Formatting.byCode(char)
                    if (formatting != null) {
                        lastStyle = lastStyle.withFormatting(formatting)
                    } else if (char == 'z') {
                        lastStyle = lastStyle.withColor(CHROMA_COLOR)
                    }
                    wasLastStyle = false
                } else {
                    visitor.accept(0, lastStyle, char.code)
                }
            }
            true
        }
    }
    private fun requiredStyleChangeString(from: Style, to: Style): String {
        val reset = (
            from.isBold && !to.isBold ||
            from.isItalic && !to.isItalic ||
            from.isObfuscated && !to.isObfuscated ||
            from.isUnderlined && !to.isUnderlined ||
            from.isStrikethrough && !to.isStrikethrough ||
            from.color != null && to.color == null
        )

        val sb = StringBuilder()

        if (reset) sb.append(Formatting.RESET.toString())

        if ((to.isBold && reset) || (to.isBold && !from.isBold)) {
            sb.append(Formatting.BOLD.toString())
        }
        if ((to.isItalic && reset) || (to.isItalic && !from.isItalic)) {
            sb.append(Formatting.ITALIC.toString())
        }
        if ((to.isObfuscated && reset) || (to.isObfuscated && !from.isObfuscated)) {
            sb.append(Formatting.OBFUSCATED.toString())
        }
        if ((to.isUnderlined && reset) || (to.isUnderlined && !from.isUnderlined)) {
            sb.append(Formatting.UNDERLINE.toString())
        }
        if ((to.isStrikethrough && reset) || (to.isStrikethrough && !from.isStrikethrough)) {
            sb.append(Formatting.STRIKETHROUGH.toString())
        }
        if (from.color != to.color && to.color != null) {
            if (to.color?.name == "chroma") {
                sb.append("§z")
            } else {
                to.color?.toChatFormatting() ?.let {
                    sb.append(it.toString())
                }
            }

        }
        return sb.toString()
    }
}
