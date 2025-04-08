package org.dam.tfg.styles

import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.focus
import com.varabyte.kobweb.silk.style.selectors.hover
import org.dam.tfg.models.Theme
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent

val TableSelectorStyle = CssStyle {
    base {
        Modifier.border(
            width = 2.px,
            style = LineStyle.Solid,
            color = Theme.HalfBlack.rgb
        ).then(
            Modifier
                .borderRadius(4.px)
                .transition(Transition.of(property = "border", duration = 300.ms))
        )
    }

    hover {
        Modifier
            .borderRadius(4.px)
            .border(
                width = 2.px,
                style = LineStyle.Solid,
                color = Theme.Primary.rgb
            )
    }

    focus {
        Modifier
            .borderRadius(4.px)
            .border(
                width = 2.px,
                style = LineStyle.Solid,
                color = Theme.Primary.rgb
            )
    }
}

val RadioButtonStyle = CssStyle {
    base {
        Modifier
            .border(
                width = 2.px,
                style = LineStyle.Solid,
                color = Theme.HalfBlack.rgb
            )
            .borderRadius(50.percent)
            .backgroundColor(Colors.White)
            .transition(Transition.of(property = "all", duration = 300.ms))
    }
    cssRule(":checked + label::before") {
        Modifier
            .backgroundColor(Theme.Primary.rgb)
            .border(width = 2.px, style = LineStyle.Solid, color = Theme.Primary.rgb)
    }
    hover {
        Modifier
            .border(width = 2.px, style = LineStyle.Solid, color = Theme.Primary.rgb)
    }
}