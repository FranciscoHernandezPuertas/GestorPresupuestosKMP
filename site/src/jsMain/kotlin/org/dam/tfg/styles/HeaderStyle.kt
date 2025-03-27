package org.dam.tfg.styles


import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.TransitionProperty
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.selectors.hover
import org.dam.tfg.models.Theme
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

// Estilo para el menú desplegable
val DropdownMenuStyle = CssStyle {
    base {
        Modifier
            .border(
                width = 1.px,
                style = LineStyle.Solid,
                color = Colors.Black
            )
    }
}

// Estilo para opciones del menú
val MenuItemStyle = CssStyle {
    base {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms,
                    timingFunction = null,
                    delay = null
                )
            )
            .color(Theme.Secondary.rgb)
    }
    hover {
        Modifier.color(Theme.Primary.rgb)
    }
}

val IconHoverStyle = CssStyle {
    base {
        Modifier
            .transition(
                Transition.of(
                    property = TransitionProperty.All,
                    duration = 300.ms
                )
            )
            .color(Theme.White.rgb)
    }
    hover {
        Modifier.color(Theme.Secondary.rgb)
    }
}