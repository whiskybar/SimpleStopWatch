# Custom Font Setup Instructions

## Required Font Files

To complete the visual design improvements, you need to add two custom font files to your project:

### 1. LED Matrix Font (ds_digital.otf)
- **Purpose**: Main time display font
- **Style**: LED matrix display (like old Casio digital wristwatches)
- **Download**: Search for "DS-Digital" or "LED Matrix" font
- **Location**: `app/src/main/res/font/ds_digital.otf`
- **Alternative**: "Digital-7", "LED Display", or "Matrix" fonts

### 2. Technical Font (orbitron.ttf)
- **Purpose**: Interval widget and technical elements
- **Style**: Futuristic, technical, monospace-like
- **Download**: Search for "Orbitron" font (Google Fonts has it)
- **Location**: `app/src/main/res/font/orbitron.ttf`

## How to Add Fonts

1. **Download the font files** from the internet (free fonts available)
2. **Place them in the font directory**: `app/src/main/res/font/`
3. **Rename them exactly**:
   - `ds_digital.otf` (for LED matrix clock display)
   - `orbitron.ttf` (for interval widget)
4. **Move extra files**: Place copyright notices, readme files, and additional font variants in `app/src/main/res/font/extra/` to avoid compilation issues

## Alternative: Use System Fonts

If you can't find the custom fonts, you can temporarily use system fonts by modifying the font definitions in MainActivity.kt:

```kotlin
// Current implementation - using actual font files
val digitalClockFont = FontFamily(Font(R.font.ds_digital, FontWeight.Normal)) // ds_digital.otf
val technicalFont = FontFamily(Font(R.font.orbitron, FontWeight.Normal)) // orbitron.ttf
```

## Font Sources

### LED Matrix Fonts:
- **DS-Digital**: https://www.dafont.com/ds-digital.font
- **LED Display**: https://www.fontspace.com/led-display-font-f2421
- **Digital-7**: https://www.dafont.com/digital-7.font
- **Matrix**: Search "LED matrix font" or "digital watch font" on Google
- **Alternative**: Search "Casio watch font" or "retro digital font"

### Orbitron Font:
- https://fonts.google.com/specimen/Orbitron
- Download from Google Fonts

## Testing

After adding the fonts:
1. Build and run the project
2. Check that the time display uses the LED matrix font (retro wristwatch style)
3. Verify the interval widget uses the technical font
4. Test all functionality remains intact
5. Ensure the time display stays on one line (no text wrapping)
