package org.dam.tfg.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.boxShadow
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxWidth
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.left
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.position
import com.varabyte.kobweb.compose.ui.modifiers.right
import com.varabyte.kobweb.compose.ui.modifiers.top
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.modifiers.zIndex
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.icons.fa.FaGear
import com.varabyte.kobweb.silk.components.icons.fa.IconSize
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.localStorage
import org.dam.tfg.models.Theme
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.MenuItemStyle
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.logout
import org.dam.tfg.util.validateToken
import org.jetbrains.compose.web.css.CSSColorValue
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.Position
import org.jetbrains.compose.web.css.px
import org.w3c.dom.get
import kotlin.math.exp

@Composable
fun AppHeader(
    title: String = "Generador de Presupuestos",
    showSettings: Boolean = true
) {
    val context = rememberPageContext()
    var userType by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userType = localStorage["userType"] ?: ""

        // Verificar token para posible autologin
        val token = localStorage["jwt_token"]
        val remember = localStorage["remember"]

        if (token != null && remember == "true") {
            validateToken()?.let {
                userType = it.type
            }
        }
    }

    // Header con título
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(leftRight = 20.px, topBottom = 15.px)
            .backgroundColor(Theme.Primary.rgb),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer()

        SpanText(
            modifier = Modifier
                .fontFamily(FONT_FAMILY)
                .fontSize(28.px)
                .fontWeight(FontWeight.Bold)
                .color(Theme.White.rgb),
            text = title
        )

        Spacer()

        if (showSettings) {
            // Mostrar el icono de engranaje para todos los usuarios
            Box {
                FaGear(
                    modifier = Modifier
                        .margin(left = 15.px)
                        .color(Theme.White.rgb)
                        .cursor(Cursor.Pointer)
                        .onClick { showDropdown = !showDropdown },
                    size = IconSize.XL
                )

                // Menú desplegable
                if (showDropdown) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .position(Position.Fixed)
                            .top(0.px)
                            .left(0.px)
                            .zIndex(5)
                            .onClick { showDropdown = false }
                    )

                    UserMenuDropdown(
                        modifier = Modifier.zIndex(10),
                        isAdmin = userType == "admin",
                        onClose = { showDropdown = false },
                        onAdminPanelClick = { context.router.navigateTo(Screen.AdminHome.route) },
                        onLogoutClick = {
                            logout()
                            context.router.navigateTo(Screen.Login.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UserMenuDropdown(
    modifier: Modifier,
    isAdmin: Boolean,
    onClose: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .onClick { onClose() }
            .fillMaxSize()
            .zIndex(5)
    ) {
        Column(
            modifier = Modifier
                .zIndex(6)
                .position(Position.Absolute)
                .right(20.px)
                .top(70.px)
                .width(200.px)
                .backgroundColor(Theme.White.rgb)
                .borderRadius(4.px)
                // Añadir el borde negro aquí:
                .border(width = 1.px, style = LineStyle.Solid, color = Theme.Black.rgb)
                .padding(10.px)
                .boxShadow(
                    offsetX = 0.px,
                    offsetY = 2.px,
                    blurRadius = 4.px,
                    color = Theme.LightGray.rgb
                )
                .onClick { it.stopPropagation() }
        ) {
            Column {
                if (isAdmin) {
                    Row(
                        modifier = MenuItemStyle.toModifier()
                            .padding(10.px)
                            .width(150.px)
                            .cursor(Cursor.Pointer)
                            .onClick {
                                onAdminPanelClick()
                                onClose()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SpanText(
                            modifier = MenuItemStyle.toModifier()
                                .fontFamily(FONT_FAMILY),
                                //.color(Theme.Secondary.rgb),
                            text = "Panel de Control"
                        )
                    }
                }

                Row(
                    modifier = MenuItemStyle.toModifier()
                        .padding(10.px)
                        .width(150.px)
                        .cursor(Cursor.Pointer)
                        .onClick {
                            expanded = !expanded
                            onLogoutClick()
                            onClose()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpanText(
                        modifier = Modifier
                            .fontFamily(FONT_FAMILY),
                            //.color(Theme.Secondary.rgb),
                        text = "Cerrar sesión"
                    )
                }
            }
        }
    }
}

@Composable
private fun Divider(color: CSSColorValue) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.px)
            .backgroundColor(color)
    )
}

fun returnUserType(): String {
    return localStorage["userType"] ?: ""
}