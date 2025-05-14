package ch.wuerth.tobias.timeflow.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val predefinedColors = listOf(
    0xFFF44336.toInt(), // Red (Material Design 500)
    0xFFE57373.toInt(), // Red (Material Design 300)
    0xFFFF9800.toInt(), // Orange (Material Design 500)
    0xFFFFB74D.toInt(), // Orange (Material Design 300)
    0xFFFFEB3B.toInt(), // Yellow (Material Design 500)
    0xFFFFF176.toInt(), // Yellow (Material Design 300)
    0xFFAED581.toInt(), // Light Green (Material Design 300)
    0xFF4CAF50.toInt(), // Green (Material Design 500)
    0xFF81C784.toInt(), // Green (Material Design 300)
    0xFF00BCD4.toInt(), // Cyan (Material Design 500)
    0xFF4DD0E1.toInt(), // Cyan (Material Design 300)
    0xFF2196F3.toInt(), // Blue (Material Design 500)
    0xFF64B5F6.toInt(), // Blue (Material Design 300)
    0xFF3F51B5.toInt(), // Indigo (Material Design 500)
    0xFF7986CB.toInt(), // Indigo (Material Design 300)
    0xFF9C27B0.toInt(), // Purple (Material Design 500)
    0xFFBA68C8.toInt(), // Purple (Material Design 300)
    0xFFFFFFFF.toInt(), // White
    0xFFBDBDBD.toInt(), // Grey 400
    0xFF666666.toInt(), // Dark Grey
)

@Composable
fun ColorPicker(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Select color:", Modifier.padding(bottom = 8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(4.dp)
        ) {
            items(predefinedColors) { color ->
                ColorItem(
                    color = color,
                    isSelected = color == selectedColor,
                    onColorSelected = { onColorSelected(color) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Preview of selected color
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(selectedColor))
                    .border(2.dp, Color.LightGray, CircleShape)
            )
            
            Text(
                text = "Selected color",
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}

@Composable
private fun ColorItem(
    color: Int,
    isSelected: Boolean,
    onColorSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(50.dp)
            .clip(CircleShape)
            .background(Color(color))
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color.White else Color.LightGray,
                shape = CircleShape
            )
            .clickable { onColorSelected() }
    )
}
