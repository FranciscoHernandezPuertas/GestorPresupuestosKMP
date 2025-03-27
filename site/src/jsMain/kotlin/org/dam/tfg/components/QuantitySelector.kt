package org.dam.tfg.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.icons.fa.FaMinus
import com.varabyte.kobweb.silk.components.icons.fa.FaPlus
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.css.px

@Composable
fun QuantitySelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 1,
    max: Int = 5,
    showText: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showText) {
            SpanText(
                modifier = Modifier
                    .fontFamily(FONT_FAMILY)
                    .fontSize(16.px)
                    .color(Theme.Secondary.rgb),
                text = "Cantidad: "
            )
        }

        Box(
            modifier = Modifier
                .size(30.px)
                .backgroundColor(if (value > min) Theme.Primary.rgb else Theme.LightGray.rgb)
                .borderRadius(4.px)
                .cursor(if (value > min) Cursor.Pointer else Cursor.NotAllowed)
                .onClick { if (value > min) onValueChange(value - 1) },
            contentAlignment = Alignment.Center
        ) {
            FaMinus(
                modifier = Modifier.color(Colors.White),
                size = IconSize.SM
            )
        }

        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .color(Theme.Secondary.rgb)
                .margin(leftRight = 15.px)
                .width(20.px)
                .textAlign(TextAlign.Center),
            text = value.toString()
        )

        Box(
            modifier = Modifier
                .size(30.px)
                .backgroundColor(if (value < max) Theme.Primary.rgb else Theme.LightGray.rgb)
                .borderRadius(4.px)
                .cursor(if (value < max) Cursor.Pointer else Cursor.NotAllowed)
                .onClick { if (value < max) onValueChange(value + 1) },
            contentAlignment = Alignment.Center
        ) {
            FaPlus(
                modifier = Modifier.color(Colors.White),
                size = IconSize.SM
            )
        }
    }
}