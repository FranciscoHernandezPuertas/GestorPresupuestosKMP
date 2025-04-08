package org.dam.tfg.styles

import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.rgba
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.dam.tfg.models.Theme
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

val DropdownItemStyle = CssStyle {
    base {
        Modifier
            .backgroundColor(Colors.Transparent)
            .padding(leftRight = 12.px, topBottom = 8.px)
            .borderRadius(4.px)
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 200.ms
                )
            )
            .color(Theme.Secondary.rgb)
    }

    hover {
        Modifier
            .backgroundColor(rgba(0, 0, 0, 0.05f))
            .color(Theme.Primary.rgb)
    }
}

val DropdownContainerStyle = CssStyle {
    base {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms
                )
            )
    }
}

// Para el botón que despliega el menú
val DropdownTriggerStyle = CssStyle {
    base {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 250.ms
                )
            )
    }

    hover {
        Modifier.backgroundColor(rgba(0, 0, 0, 0.03f))
    }
}