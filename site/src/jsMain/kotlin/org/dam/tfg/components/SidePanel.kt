package org.dam.tfg.components

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.dom.svg.Path
import com.varabyte.kobweb.compose.dom.svg.Svg
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import org.dam.tfg.models.Theme
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.font
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.dam.tfg.util.Res
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh


@Composable
fun SidePanel() {
    // Posicionar fijo en la esquina superior izquierda sin margen extra en pantallas grandes.
    Column(
        modifier = Modifier
            .position(Position.Fixed)
            .left(0.px)
            .top(0.px)
            .width(SIDE_PANEL_WIDTH.px)
            .height(100.vh)
            .backgroundColor(Theme.Secondary.rgb)
            .zIndex(9)
            .padding(top = 50.px, bottom = 50.px, left = 20.px, right = 20.px),
    ) {
        // Se reduce el tamaño del logo para ajustarlo al panel.
        Image(
            modifier = Modifier
                .margin(bottom = 30.px)
                .width(200.px),
            src = Res.Image.logo,
            alt = "Logo Imagen"
        )
        SpanText(
            modifier = Modifier
                .margin(bottom = 30.px)
                .fontFamily(FONT_FAMILY)
                .fontSize(14.px)
                .color(Theme.HalfWhite.rgb),
            text = "Panel de Administración"
        )
        NavigationItem(
            modifier = Modifier.margin(bottom = 24.px),
            selected = true,
            title = "Inicio",
            icon = Res.PathIcon.home,
            onClick = {}
        )
        NavigationItem(
            modifier = Modifier.margin(bottom = 24.px),
            title = "Editar",
            icon = Res.PathIcon.edit,
            onClick = {}
        )
        NavigationItem(
            modifier = Modifier.margin(bottom = 24.px),
            title = "Lista",
            icon = Res.PathIcon.list,
            onClick = {}
        )
        NavigationItem(
            modifier = Modifier.margin(bottom = 24.px),
            title = "Cerrar Sesión",
            icon = Res.PathIcon.logout,
            onClick = {}
        )
    }
}

@Composable
fun NavigationItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    title: String,
    icon: String,
    onClick: () -> Unit
    ) {
    Row(
        modifier = modifier
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        VectorIcon(
            modifier = Modifier.margin(right = 10.px),
            pathData = icon,
            color = if(selected) Theme.Primary.hex else Theme.White.hex
        )
        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .color(if(selected) Theme.Primary.rgb else Theme.White.rgb),
            text = title
        )
    }
}

@Composable
fun VectorIcon(
    modifier: Modifier = Modifier,
    pathData: String,
    color: String
) {
    Svg(
        attrs = modifier
            .width(24.px)
            .height(24.px)
            .toAttrs {
                attr("viewBox", "0 0 840 840")
                attr("fill", "none")
            }
    ) {
        Path(
            attrs = Modifier.toAttrs {
                attr("d", pathData)
                attr("stroke", "#${color}")
                attr("stroke-width", "2")
                attr("stroke-linecap", "round")
                attr("stroke-linejoin", "round")
                attr("fill", "#${color}")
            }
        )
    }
}