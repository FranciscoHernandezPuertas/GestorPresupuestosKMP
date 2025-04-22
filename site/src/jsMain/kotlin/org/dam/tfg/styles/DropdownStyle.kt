package org.dam.tfg.styles

import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Color.Companion.rgba
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.dam.tfg.models.Theme
import org.jetbrains.compose.web.css.LineStyle
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
            .backgroundColor(Theme.LightGray.rgb)
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

// Estilo mejorado para el botón de desplegable (Seleccionar...)
val DropdownSelectorStyle = CssStyle {
    base {
        Modifier
            .border(
                width = 1.px,
                style = LineStyle.Solid,
                color = rgba(0, 0, 0, 0.1f),
            )
            .borderRadius(4.px)
            .backgroundColor(Colors.White)
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 250.ms
                )
            )
    }

    hover {
        Modifier
            .backgroundColor(rgba(0, 0, 0, 0.03f))
            .border(
                width = 1.px,
                style = LineStyle.Solid,
                color = rgba(0, 0, 0, 0.2f),
            )
    }
}

// Estilo para animar la apertura y cierre del dropdown
val DropdownAnimationStyle = CssStyle {
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