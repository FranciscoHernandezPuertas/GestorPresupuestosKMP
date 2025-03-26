package org.dam.tfg.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.css.px

@Composable
fun ValidationError(error: String) {
    if (error.isNotEmpty()) {
        SpanText(
            modifier = Modifier
                .margin(topBottom = 8.px)
                .color(Colors.Red)
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .textAlign(TextAlign.Center),
            text = error
        )
    }
}