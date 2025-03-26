package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.FontWeight
import com.varabyte.kobweb.compose.foundation.layout.Arrangement
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.foundation.layout.Spacer
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
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
import com.varabyte.kobweb.compose.ui.modifiers.id
import com.varabyte.kobweb.compose.ui.modifiers.margin
import com.varabyte.kobweb.compose.ui.modifiers.maxWidth
import com.varabyte.kobweb.compose.ui.modifiers.onClick
import com.varabyte.kobweb.compose.ui.modifiers.outline
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.compose.ui.modifiers.size
import com.varabyte.kobweb.compose.ui.modifiers.width
import com.varabyte.kobweb.compose.ui.toAttrs
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.graphics.Image
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.breakpoint.Breakpoint
import com.varabyte.kobweb.silk.style.toModifier
import com.varabyte.kobweb.silk.theme.breakpoint.rememberBreakpoint
import org.dam.tfg.components.AppHeader
import org.dam.tfg.components.BudgetFooter
import org.dam.tfg.models.Theme
import org.dam.tfg.navigation.Screen
import org.dam.tfg.styles.LoginInputStyle
import org.dam.tfg.util.BudgetManager
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.Res
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.percent
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Input
import org.jetbrains.compose.web.dom.Label

@Page
@Composable
fun TableElementsPage() {
    isUserLoggedInCheck {
        TableElementsPageContent()
    }
}

@Composable
fun TableElementsPageContent() {
    val context = rememberPageContext()
    val breakpoint = rememberBreakpoint()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 0.px),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Contenido específico para usuarios
            TableElementsContent()
        }
    }
}

@Composable
fun TableElementsContent() {
    var mesa by remember { mutableStateOf(BudgetManager.loadMesa()) }
    val breakpoint = rememberBreakpoint()
    var selectedTable by remember { mutableStateOf(1) }

    fun validateData(): Boolean {
        return mesa.tramos.all { it.isValid() }
    }

    fun saveData() {
        BudgetManager.updateMesa(mesa)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(title = "Selección de Elementos en Encimera")

        SpanText(
            modifier = Modifier
                .margin(top = 40.px, bottom = 30.px)
                .fontFamily(FONT_FAMILY)
                .fontSize(24.px)
                .fontWeight(FontWeight.Medium)
                .color(Theme.Secondary.rgb),
            text = "Seleccione los elementos en encimera"
        )

        // Contenedor principal
        if (breakpoint > Breakpoint.MD) {

        } else {
            // Vista móvil
        }

        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            BudgetFooter(
                previousScreen = Screen.TableSelector,
                nextScreen = Screen.TableElements,
                validateData = { validateData() },
                saveData = { saveData() }
            )
        }
    }
}
