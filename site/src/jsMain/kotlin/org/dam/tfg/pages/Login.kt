package org.dam.tfg.pages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.css.TextAlign
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.backgroundColor
import com.varabyte.kobweb.compose.ui.modifiers.border
import com.varabyte.kobweb.compose.ui.modifiers.borderRadius
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.cursor
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.fontWeight
import com.varabyte.kobweb.compose.ui.modifiers.height
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.outline
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.textAlign
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.dam.tfg.models.Theme
import org.dam.tfg.models.User
import org.dam.tfg.models.UserWithoutPassword
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.LoginInputStyle
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Id
import org.dam.tfg.util.Res
import org.dam.tfg.util.checkUserExistence
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Input
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.set


@Page
@Composable
fun LoginScreen() {
    val scope = rememberCoroutineScope()
    val context = rememberPageContext()
    var errorText by remember { mutableStateOf("") }

    fun attemptLogin() {
        scope.launch {
            val username = (document.getElementById(Id.usernameInput) as HTMLInputElement).value
            val password = (document.getElementById(Id.passwordInput) as HTMLInputElement).value
            if(username.isNotEmpty() && password.isNotEmpty()) {
                val user = checkUserExistence(
                    user = User(
                        username = username,
                        password = password
                    )
                )
                if(user != null) {
                    rememberLoggedIn(remember = true, user = user)
                    if(user.type == "admin") {
                        context.router.navigateTo(Screen.AdminHome.route)
                    } else {
                        context.router.navigateTo(Screen.Home.route)
                    }
                }
                else {
                    errorText = "Usuario o contraseña incorrectos"
                    delay(3000)
                    errorText = " "
                }
            }
            else {
                errorText = "Los campos de usuario y contraseña están vacíos"
                delay(3000)
                errorText = " "
            }
        }
    }

    // Manejador global de evento de teclado
    DisposableEffect(Unit) {
        val keyListener: (Event) -> Unit = { event ->
            if (event is KeyboardEvent && event.key == "Enter") {
                attemptLogin()
            }
        }

        // Agregar el listener al documento
        document.addEventListener("keydown", keyListener)

        // Eliminar el listener cuando el componente se desmonte
        onDispose {
            document.removeEventListener("keydown", keyListener)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(leftRight = 50.px, top = 80.px, bottom = 24.px)
                .backgroundColor(Theme.LightGray.rgb),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier
                    .margin(bottom = 50.px)
                    .width(500.px),
                src = Res.Image.logo,
                description = "Logo Imagen"
            )
            Input(
                type = InputType.Text,
                attrs = LoginInputStyle.toModifier()
                    .id(Id.usernameInput)
                    .margin(bottom = 12.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontSize(14.px)
                    .fontFamily(FONT_FAMILY)
                    .outline(
                        width = 0.px,
                        style = LineStyle.None,
                        color = Colors.Transparent
                    )
                    .toAttrs {
                        attr("placeholder", "Usuario")
                    }
            )
            Input(
                type = InputType.Password,
                attrs = LoginInputStyle.toModifier()
                    .id(Id.passwordInput)
                    .margin(bottom = 20.px)
                    .width(350.px)
                    .height(54.px)
                    .padding(leftRight = 20.px)
                    .backgroundColor(Colors.White)
                    .fontFamily(FONT_FAMILY)
                    .fontSize(14.px)
                    .outline(
                        width = 0.px,
                        style = LineStyle.None,
                        color = Colors.Transparent
                    )
                    .toAttrs {
                        attr("placeholder", "Contraseña")
                    }
            )
            Button(
                attrs = Modifier
                    .margin(bottom = 24.px)
                    .width(350.px)
                    .height(54.px)
                    .backgroundColor(Theme.Primary.rgb)
                    .color(Colors.White)
                    .borderRadius(r = 4.px)
                    .fontFamily(FONT_FAMILY)
                    .fontWeight(FontWeight.Medium)
                    .fontSize(14.px)
                    .border(
                        width = 0.px,
                        style = LineStyle.None,
                        color = Colors.Transparent
                    )
                    .outline(
                        width = 0.px,
                        style = LineStyle.None,
                        color = Colors.Transparent
                    )
                    .cursor(Cursor.Pointer)
                    .onClick { attemptLogin() }
                    .toAttrs(),
            ) {
                SpanText("Iniciar Sesión")
            }
            SpanText(
                modifier = Modifier
                    .width(350.px)
                    .color(Colors.Red)
                    .fontFamily(FONT_FAMILY)
                    .textAlign(TextAlign.Center),
                text = errorText
            )
        }
    }
}

private fun rememberLoggedIn(
    remember: Boolean,
    user: UserWithoutPassword? = null
) {
    localStorage["remember"] = remember.toString()
    if(user != null) {
        localStorage["userId"] = user.id
        localStorage["username"] = user.username
    }
}