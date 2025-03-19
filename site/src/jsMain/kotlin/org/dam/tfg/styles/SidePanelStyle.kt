package org.dam.tfg.styles

import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.cssRule
import com.varabyte.kobweb.silk.style.selectors.focus
import org.dam.tfg.models.Theme
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

val NavigationItemStyle = CssStyle {
    cssRule(" > #svgParent > #vectorIcon") {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms,
                    timingFunction = null,
                    delay = null
                )
            )
            .styleModifier {
                property("stroke", "#${Theme.White.hex}")
                property("fill", "#${Theme.White.hex}")
            }
    }

    cssRule(":hover > #svgParent > #vectorIcon") {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms,
                    timingFunction = null,
                    delay = null
                )
            )
            .styleModifier {
                property("stroke", "#${Theme.Primary.hex}")
                property("fill", "#${Theme.Primary.hex}")
        }
    }
    cssRule(" > #navigationText") {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms,
                    timingFunction = null,
                    delay = null
                )
            )
            .color(Theme.White.rgb)
    }

    cssRule(":hover > #navigationText") {
        Modifier.color(Theme.Primary.rgb)
    }
}