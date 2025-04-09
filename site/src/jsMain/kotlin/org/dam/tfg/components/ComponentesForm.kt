package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.style.KobwebComposeStyleSheet.attr
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.silk.components.forms.Switch
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.forms.SwitchSize
import com.varabyte.kobweb.silk.components.layout.SimpleGrid
import com.varabyte.kobweb.silk.components.layout.numColumns
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
@Composable
fun ComponentesForm() {
    val breakpoint = rememberBreakpoint()
    var switch1 by remember { mutableStateOf(false) }
    var switch2 by remember { mutableStateOf(false) }
    var switch3 by remember { mutableStateOf(false) }

    SimpleGrid(
        numColumns = numColumns(base = 1, sm = 3)
    ) {
        Row(
            modifier = Modifier
                .margin(
                    right = if (breakpoint < Breakpoint.SM) 0.px else 24.px,
                    bottom = if (breakpoint < Breakpoint.SM) 24.px else 0.px
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch1,
                onCheckedChange = { switch1 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 1",
            )
        }

        Row(
            modifier = Modifier
                .margin(
                    right = if (breakpoint < Breakpoint.SM) 0.px else 24.px,
                    bottom = if (breakpoint < Breakpoint.SM) 24.px else 0.px
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch2,
                onCheckedChange = { switch2 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 2",
            )
        }

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                modifier = Modifier.margin(right = 8.px),
                checked = switch3,
                onCheckedChange = { switch3 = it },
                size = SwitchSize.LG
            )
            SpanText(
                modifier = Modifier
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .color(Theme.HalfBlack.rgb),
                text = "Switch 3",
            )
        }
    }
        Input(
            type = InputType.Text,
            attrs = Modifier
                .fillMaxWidth()
                .height(54.px)
                .margin(topBottom = 12.px)
                .backgroundColor(Theme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .border(
                    width = 0.px,
                    style = LineStyle.None,
                    color = Colors.Transparent
                )
                .styleModifier {
                    property("outline", "none")
                }
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .toAttrs{
                    attr("placeholder", "Placeholder 1")
                }
        )

        Input(
            type = InputType.Text,
            attrs = Modifier
                .fillMaxWidth()
                .height(54.px)
                .margin(bottom = 12.px)
                .backgroundColor(Theme.LightGray.rgb)
                .borderRadius(r = 4.px)
                .border(
                    width = 0.px,
                    style = LineStyle.None,
                    color = Colors.Transparent
                )
                .styleModifier {
                    property("outline", "none")
                }
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .toAttrs{
                    attr("placeholder", "Placeholder 2")
                }
        )
}

@Composable
fun CategoryDropdown() {

}