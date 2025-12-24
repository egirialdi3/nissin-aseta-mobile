package id.aseta.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

@Composable
fun RequiredLabel(
    text: String,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.SemiBold,
    requiredColor: Color = Color.Red
) {
    Text(
        buildAnnotatedString {
            append(text)
            withStyle(style = SpanStyle(color = requiredColor, fontWeight = fontWeight)) {
                append(" *")
            }
        },
        fontWeight = fontWeight,
        modifier = modifier
    )
}
