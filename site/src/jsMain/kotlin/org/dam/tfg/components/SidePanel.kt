package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.ScrollBehavior
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.dom.svg.Path
import com.varabyte.kobweb.compose.dom.svg.Svg
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import org.dam.tfg.models.Theme
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.compose.ui.thenIf
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.icons.fa.FaBars
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.overlay.OpenClose
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxHeight
import com.varabyte.kobweb.compose.ui.modifiers.opacity
import com.varabyte.kobweb.compose.ui.modifiers.overflow
import com.varabyte.kobweb.compose.ui.modifiers.scrollBehavior
import com.varabyte.kobweb.compose.ui.modifiers.transition
import com.varabyte.kobweb.compose.ui.modifiers.translateX
import com.varabyte.kobweb.silk.components.icons.fa.FaXmark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.SideNavigationItemStyle
import org.dam.tfg.util.Constants.COLLAPSED_PANEL_HEIGHT
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Constants.SIDE_PANEL_WIDTH
import org.dam.tfg.util.Id
import org.dam.tfg.util.Res
import org.dam.tfg.util.logout
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.css.vh

@Composable
fun SidePanel(onMenuClick: () -> Unit) {
    val breakpoint = rememberBreakpoint()
    if (breakpoint > Breakpoint.MD) {
        SidePanelInternal()
    } else {
        CollapsedSidePanel(onMenuClick = onMenuClick)
    }
}

@Composable
private fun SidePanelInternal() {
    val breakpoint = rememberBreakpoint()
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
        NavigationItems()
    }
}

@Composable
private fun NavigationItems() {
    val context = rememberPageContext()
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
        selected = context.route.path == Screen.AdminHome.route,
        title = "Inicio",
        icon = Res.PathIcon.home,
        onClick = {
            context.router.navigateTo(Screen.AdminHome.route)
        }
    )
    NavigationItem(
        modifier = Modifier.margin(bottom = 24.px),
        selected = context.route.path == Screen.AdminEdit.route,
        title = "Editar",
        icon = Res.PathIcon.edit,
        onClick = {
            context.router.navigateTo(Screen.AdminEdit.route)
        }
    )
    NavigationItem(
        modifier = Modifier.margin(bottom = 24.px),
        selected = context.route.path == Screen.AdminList.route,
        title = "Lista",
        icon = Res.PathIcon.list,
        onClick = {
            context.router.navigateTo(Screen.AdminList.route)
        }
    )
    NavigationItem(
        modifier = Modifier.margin(bottom = 24.px),
        title = "Pantalla Principal",
        selected = context.route.path == Screen.Home.route,
        icon = Res.PathIcon.index,
        onClick = {
            context.router.navigateTo(Screen.Home.route)
        }
    )
    NavigationItem(
        modifier = Modifier.margin(bottom = 24.px),
        title = "Cerrar Sesión",
        icon = Res.PathIcon.logout,
        onClick = {
            logout()
            context.router.navigateTo(Screen.Login.route)
        }
    )
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
        modifier = SideNavigationItemStyle.toModifier()
            .then(modifier)
            .cursor(Cursor.Pointer)
            .onClick { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        VectorIcon(
            modifier = Modifier.margin(right = 10.px),
            selected = selected,
            pathData = icon,
            //color = if(selected) Theme.Primary.hex else Theme.White.hex
        )
        SpanText(
            modifier = Modifier
                .id(Id.navigationText)
                .fontFamily(FONT_FAMILY)
                .fontSize(16.px)
                .thenIf(
                    condition = selected,
                    other = Modifier.color(Theme.Primary.rgb)
                ),
            text = title
        )
    }
}

@Composable
private fun VectorIcon(
    modifier: Modifier = Modifier,
    pathData: String,
    selected: Boolean
) {
    Svg(
        attrs = modifier
            .id(Id.svgParent)
            .width(24.px)
            .height(24.px)
            .toAttrs {
                attr("viewBox", "0 0 840 840")
            }
    ) {
        Path(
            attrs = Modifier
                .id(Id.vectorIcon)
                .thenIf(
                    condition = selected,
                    other = Modifier.styleModifier {
                        property("stroke", "#${Theme.Primary.hex}")
                        property("fill", "#${Theme.Primary.hex}")
                    }
                )
                .toAttrs {
                    attr("d", pathData)
                    attr("stroke-width", "2")
                    attr("stroke-linecap", "round")
                    attr("stroke-linejoin", "round")
                }
        )
    }
}

@Composable
private fun CollapsedSidePanel(onMenuClick: () -> Unit ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(COLLAPSED_PANEL_HEIGHT.px)
            .padding(leftRight = 24.px)
            .backgroundColor(Theme.Secondary.rgb),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FaBars(
            modifier = Modifier
                .margin(right = 24.px)
                .color(Colors.White)
                .cursor(Cursor.Pointer)
                .onClick { onMenuClick() },
            size = IconSize.XL
        )
        Image(
            modifier = Modifier.width(80.px),
            src = Res.Image.logo,
            alt = "Logo Image"
        )
    }
}

@Composable
fun OverflowSidePanel(onMenuClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val breakpoint = rememberBreakpoint()
    var translateX by remember {
        mutableStateOf((-100).percent)
    }
    var opacity by remember {
        mutableStateOf(0.percent)
    }

    LaunchedEffect(breakpoint) {
        translateX = 0.percent
        opacity = 100.percent
        if (breakpoint > Breakpoint.MD) {
            scope.launch {
                translateX = (-100).percent
                opacity = 0.percent
                delay(500)
                onMenuClose()
            }
        }
    }

    // Este Box exterior captura los clics en cualquier parte de la pantalla
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.vh)
            .position(Position.Fixed)
            .zIndex(9)
            .opacity(opacity)
            .transition(
                Transition.of(
                    property = "opacity",
                    duration = 300.ms,
                    timingFunction = null,
                    delay = null
                )
            )
            .backgroundColor(Theme.HalfBlack.rgb)
            .onClick {
                // Cerrar el panel al hacer clic en el fondo
                scope.launch {
                    translateX = (-100).percent
                    opacity = 0.percent
                    delay(500)
                    onMenuClose()
                }
            }
    ) {
        // El panel lateral con mayor zIndex para evitar que se cierre al hacer clic dentro
        Column(
            modifier = Modifier
                .padding(all = 24.px)
                .fillMaxWidth()
                .fillMaxHeight()
                .width(if(breakpoint < Breakpoint.MD) 50.percent else 25.percent)
                .translateX(translateX)
                .transition(
                    Transition.of(
                        property = "translate",
                        duration = 300.ms,
                        timingFunction = null,
                        delay = null
                    )
                )
                .overflow(Overflow.Auto)
                .scrollBehavior(ScrollBehavior.Smooth)
                .backgroundColor(Theme.Secondary.rgb)
                .onClick {
                    // Detener la propagación del clic para que no llegue al Box exterior
                    it.stopPropagation()
                }
        ) {
            Row(
                modifier = Modifier.margin(bottom = 60.px, top = 24.px),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FaXmark(
                    modifier = Modifier
                        .margin(right = 20.px)
                        .color(Colors.White)
                        .onClick {
                            scope.launch {
                                translateX = (-100).percent
                                opacity = 0.percent
                                delay(500)
                                onMenuClose()
                            }
                        },
                    size = IconSize.LG
                )
                Image(
                    modifier = Modifier
                        .width(80.px),
                    src = Res.Image.logo,
                    alt = "Logo Image"
                )
            }
            NavigationItems()
        }
    }
}