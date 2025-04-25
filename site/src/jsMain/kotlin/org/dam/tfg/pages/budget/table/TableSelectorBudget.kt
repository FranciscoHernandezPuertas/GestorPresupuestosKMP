package org.dam.tfg.pages.budget.table

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.color
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.fontFamily
import com.varabyte.kobweb.compose.ui.modifiers.fontSize
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import org.dam.tfg.components.AppHeader
import org.dam.tfg.models.Theme
import org.dam.tfg.util.Constants.FONT_FAMILY
import org.dam.tfg.util.isUserLoggedInCheck
import org.jetbrains.compose.web.css.px

@Page
@Composable
fun TableSelectorBudgetPage() {
    isUserLoggedInCheck {
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()

            TableSelectorBudget()
        }
    }
}

@Composable
fun TableSelectorBudget() {
    SpanText(
        text = "Aquí iría el resultado del presupuesto, listando los elementos, junto al precio final de cada uno y un botón de exportar a PDF, que, a parte de exportarlo a PDF, también guardará el presupuesto en la base de datos de MongoDB.",
        modifier = Modifier
            .fillMaxSize()
            .padding(left = 20.px, right = 20.px, top = 20.px, bottom = 20.px)
            .color(Theme.Red.rgb)
            .fontFamily(FONT_FAMILY)
            .fontSize(40.px)
    )
}