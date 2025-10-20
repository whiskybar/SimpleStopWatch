#!/usr/bin/env python3
"""
Android Stopwatch App Icon Generator
====================================

This script generates a 512x512 PNG app icon for an Android timer app using the Pillow library.
The icon features a digital stopwatch with a circular bezel, LCD display, and control buttons.

Features:
- Circular black bezel/outline
- Digital display showing "00:00" in LCD font style
- Start/stop buttons with play/pause icons
- High contrast colors for app icon visibility
- Transparent background with drop shadow effects
- Fallback drawing when icon files are missing

Author: AI Assistant
Date: 2024
"""

from PIL import Image, ImageDraw, ImageFont, ImageFilter
import os
import math

def create_circular_bezel(draw, center, radius, thickness=8):
    """Create a circular bezel for the stopwatch."""
    # Outer circle (black)
    draw.ellipse([center[0] - radius, center[1] - radius,
                  center[0] + radius, center[1] + radius],
                 fill='black', outline='#333333', width=2)

    # Inner circle (white/gray)
    inner_radius = radius - thickness
    draw.ellipse([center[0] - inner_radius, center[1] - inner_radius,
                  center[0] + inner_radius, center[1] + inner_radius],
                 fill='#f0f0f0', outline='#cccccc', width=1)

def create_digital_display(draw, center, size):
    """Create a rectangular digital display with LCD-style text."""
    # Display background (dark green/black)
    display_width, display_height = size
    x1 = center[0] - display_width // 2
    y1 = center[1] - display_height // 2
    x2 = center[0] + display_width // 2
    y2 = center[1] + display_height // 2

    # Main display background
    draw.rectangle([x1, y1, x2, y2], fill='#001100', outline='#00aa00', width=2)

    # Inner display area (slightly smaller)
    inner_margin = 4
    draw.rectangle([x1 + inner_margin, y1 + inner_margin,
                    x2 - inner_margin, y2 - inner_margin],
                   fill='#002200', outline='#00aa00', width=1)

def draw_seven_segment_digit(draw, digit, position, size, color='#00ff00'):
    """Draw a seven-segment digit (0-9) at the given position."""
    x, y = position
    width, height = size

    # Seven-segment positions (relative to digit position)
    segments = {
        'a': [(x, y), (x + width, y), (x + width - 2, y + 2), (x + 2, y + 2)],  # Top
        'b': [(x + width - 2, y), (x + width, y + height//2), (x + width - 2, y + height//2 - 2), (x + width - 4, y + 2)],  # Top right
        'c': [(x + width - 2, y + height//2), (x + width, y + height), (x + width - 2, y + height - 2), (x + width - 4, y + height//2 + 2)],  # Bottom right
        'd': [(x, y + height - 2), (x + width, y + height - 2), (x + width - 2, y + height), (x + 2, y + height)],  # Bottom
        'e': [(x, y + height//2), (x + 2, y + height), (x + 2, y + height - 2), (x, y + height - 2)],  # Bottom left
        'f': [(x, y), (x + 2, y + height//2), (x + 2, y + height//2 - 2), (x, y + 2)],  # Top left
        'g': [(x, y + height//2 - 1), (x + width, y + height//2 - 1), (x + width - 2, y + height//2 + 1), (x + 2, y + height//2 + 1)]  # Middle
    }

    # Digit patterns (which segments to light up for each digit)
    digit_patterns = {
        '0': ['a', 'b', 'c', 'd', 'e', 'f'],
        '1': ['b', 'c'],
        '2': ['a', 'b', 'g', 'e', 'd'],
        '3': ['a', 'b', 'g', 'c', 'd'],
        '4': ['f', 'g', 'b', 'c'],
        '5': ['a', 'f', 'g', 'c', 'd'],
        '6': ['a', 'f', 'g', 'e', 'd', 'c'],
        '7': ['a', 'b', 'c'],
        '8': ['a', 'b', 'c', 'd', 'e', 'f', 'g'],
        '9': ['a', 'b', 'c', 'd', 'f', 'g'],
        ':': []  # Colon - we'll handle this separately
    }

    if digit in digit_patterns:
        for segment in digit_patterns[digit]:
            if segment in segments:
                points = segments[segment]
                draw.polygon(points, fill=color)

def create_digital_time_display(draw, center, size):
    """Create the digital time display showing 00:00."""
    display_width, display_height = size
    digit_width = display_width // 5  # Space for 4 digits + colon
    digit_height = display_height - 8

    # Calculate positions for each digit
    start_x = center[0] - display_width // 2 + 4
    y = center[1] - digit_height // 2

    # Draw each digit
    digits = ['0', '0', ':', '0', '0']
    positions = [
        (start_x, y),
        (start_x + digit_width, y),
        (start_x + digit_width * 2, y),
        (start_x + digit_width * 3, y),
        (start_x + digit_width * 4, y)
    ]

    for i, (digit, pos) in enumerate(zip(digits, positions)):
        if digit == ':':
            # Draw colon (two dots)
            dot_size = 3
            draw.ellipse([pos[0] + digit_width//2 - dot_size, pos[1] + digit_height//3 - dot_size,
                         pos[0] + digit_width//2 + dot_size, pos[1] + digit_height//3 + dot_size],
                        fill='#00ff00')
            draw.ellipse([pos[0] + digit_width//2 - dot_size, pos[1] + digit_height*2//3 - dot_size,
                         pos[0] + digit_width//2 + dot_size, pos[1] + digit_height*2//3 + dot_size],
                        fill='#00ff00')
        else:
            draw_seven_segment_digit(draw, digit, pos, (digit_width - 4, digit_height))

def create_control_buttons(draw, center, radius):
    """Create start/stop control buttons at the bottom of the stopwatch."""
    button_radius = 12
    button_y = center[1] + radius - 30

    # Left button (play/start)
    left_x = center[0] - 25
    draw.ellipse([left_x - button_radius, button_y - button_radius,
                  left_x + button_radius, button_y + button_radius],
                 fill='#ff4444', outline='#cc0000', width=2)

    # Play triangle
    triangle_size = 6
    play_points = [
        (left_x - triangle_size, button_y - triangle_size),
        (left_x - triangle_size, button_y + triangle_size),
        (left_x + triangle_size, button_y)
    ]
    draw.polygon(play_points, fill='white')

    # Right button (pause/stop)
    right_x = center[0] + 25
    draw.ellipse([right_x - button_radius, button_y - button_radius,
                  right_x + button_radius, button_y + button_radius],
                 fill='#ff4444', outline='#cc0000', width=2)

    # Pause bars
    bar_width = 2
    bar_height = 8
    bar_spacing = 2
    bar1_x = right_x - bar_spacing - bar_width
    bar2_x = right_x + bar_spacing
    bar_y = button_y - bar_height // 2

    draw.rectangle([bar1_x, bar_y, bar1_x + bar_width, bar_y + bar_height], fill='white')
    draw.rectangle([bar2_x, bar_y, bar2_x + bar_width, bar_y + bar_height], fill='white')

def add_drop_shadow(image):
    """Add a subtle drop shadow to the image."""
    # Create shadow
    shadow = image.copy()
    shadow = shadow.filter(ImageFilter.GaussianBlur(radius=3))

    # Create new image with shadow
    result = Image.new('RGBA', (image.width + 4, image.height + 4), (0, 0, 0, 0))
    result.paste(shadow, (2, 2))
    result.paste(image, (0, 0), image)

    return result

def load_icon_if_exists(filename):
    """Try to load an icon file if it exists, return None if not found."""
    if os.path.exists(filename):
        try:
            return Image.open(filename)
        except Exception as e:
            print(f"Warning: Could not load {filename}: {e}")
    return None

def generate_stopwatch_icon():
    """Generate the main stopwatch app icon."""
    # Create a 512x512 transparent image
    size = (512, 512)
    image = Image.new('RGBA', size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(image)

    center = (256, 256)  # Center of the 512x512 image
    bezel_radius = 200

    # Try to load external icon files, fall back to drawing if not available
    stopwatch_bezel = load_icon_if_exists('stopwatch_bezel.png')
    digital_display_bg = load_icon_if_exists('digital_display_bg.png')
    play_icon = load_icon_if_exists('play_icon.png')
    pause_icon = load_icon_if_exists('pause_icon.png')

    # Create the circular bezel
    if stopwatch_bezel:
        # Resize and paste the loaded bezel
        stopwatch_bezel = stopwatch_bezel.resize((bezel_radius * 2, bezel_radius * 2))
        bezel_pos = (center[0] - bezel_radius, center[1] - bezel_radius)
        image.paste(stopwatch_bezel, bezel_pos, stopwatch_bezel)
    else:
        # Draw the bezel programmatically
        create_circular_bezel(draw, center, bezel_radius)

    # Create the digital display
    display_size = (120, 40)
    if digital_display_bg:
        # Resize and paste the loaded display background
        digital_display_bg = digital_display_bg.resize(display_size)
        display_pos = (center[0] - display_size[0] // 2, center[1] - display_size[1] // 2)
        image.paste(digital_display_bg, display_pos, digital_display_bg)
    else:
        # Draw the display programmatically
        create_digital_display(draw, center, display_size)

    # Add the digital time display
    create_digital_time_display(draw, center, display_size)

    # Create control buttons
    create_control_buttons(draw, center, bezel_radius)

    # Add some additional details
    # Crown at the top (stopwatch winder)
    crown_y = center[1] - bezel_radius - 15
    draw.ellipse([center[0] - 8, crown_y - 8, center[0] + 8, crown_y + 8],
                 fill='#666666', outline='#333333', width=1)

    # Add subtle highlights to the bezel
    highlight_radius = bezel_radius - 5
    draw.arc([center[0] - highlight_radius, center[1] - highlight_radius,
              center[0] + highlight_radius, center[1] + highlight_radius],
             start=45, end=135, fill='white', width=2)

    # Add drop shadow
    image = add_drop_shadow(image)

    return image

def main():
    """Main function to generate and save the stopwatch icon."""
    print("Generating stopwatch app icon...")

    try:
        # Generate the icon
        icon = generate_stopwatch_icon()

        # Save the icon
        output_filename = 'stopwatch_app_icon.png'
        icon.save(output_filename, 'PNG')

        print(f"✅ Successfully generated {output_filename}")
        print(f"📏 Icon size: {icon.size[0]}x{icon.size[1]} pixels")
        print(f"🎨 Format: PNG with transparency")

        # Display some info about the generated icon
        print("\nIcon features:")
        print("- Circular black bezel with white/gray accents")
        print("- Green LCD-style digital display showing '00:00'")
        print("- Red start/stop buttons with play/pause icons")
        print("- Transparent background with drop shadow")
        print("- High contrast design for app icon visibility")

    except Exception as e:
        print(f"❌ Error generating icon: {e}")
        return False

    return True

if __name__ == "__main__":
    # Check if Pillow is available
    try:
        from PIL import Image, ImageDraw, ImageFont, ImageFilter
        print("✅ Pillow library is available")
    except ImportError:
        print("❌ Pillow library not found. Please install it with: pip install Pillow")
        exit(1)

    # Run the main function
    success = main()

    if success:
        print("\n🎉 Stopwatch app icon generation completed successfully!")
        print("The icon is ready to use in your Android app.")
    else:
        print("\n💥 Icon generation failed. Please check the error messages above.")
