package id.aseta.app.ui.screen.composable


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier


@Composable
fun RfidRadarIndicator(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    val animationValue = remember { Animatable(0f) }
    
    LaunchedEffect(isScanning) {
        if (isScanning) {
            // Continuously animate while scanning
            while (isScanning) {
                animationValue.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1000, easing = LinearEasing)
                )
                animationValue.snapTo(0f)
            }
        } else {
            // Reset animation when not scanning
            animationValue.snapTo(0f)
        }
    }
    
    Box(
        modifier = modifier
            .size(48.dp)
            .background(
                color = if (isScanning) Color(0xFF1E3A8A).copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Static background circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isScanning) Color(0xFF1E3A8A).copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
        
        // Animated pulse ripple
        if (isScanning) {
            val animatedSize = 32.dp + (16.dp * animationValue.value)
            val animatedAlpha = 0.7f - (0.7f * animationValue.value)
            
            Box(
                modifier = Modifier
                    .size(animatedSize)
                    .background(
                        color = Color(0xFF1E3A8A).copy(alpha = animatedAlpha),
                        shape = CircleShape
                    )
            )
        }
        
        // Center dot
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    color = if (isScanning) Color(0xFF1E3A8A) else Color.Red,
                    shape = CircleShape
                )
        )
    }
}
