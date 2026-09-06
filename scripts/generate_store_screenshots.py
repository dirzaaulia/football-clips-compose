import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_rounded_mask(size, radius):
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), size], radius=radius, fill=255)
    return mask

def generate_banner(
    raw_screenshot_path: str,
    output_path: str,
    badge_text: str,
    headline_text: str,
    subtext: str,
    badge_color: tuple = (56, 189, 248), # Sky blue
):
    CANVAS_WIDTH = 1080
    CANVAS_HEIGHT = 2400
    
    # 1. Base Canvas with smooth dark gradient
    base = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (11, 15, 23, 255))
    draw = ImageDraw.Draw(base)
    
    for y in range(CANVAS_HEIGHT):
        ratio = y / CANVAS_HEIGHT
        r = int(14 * (1 - ratio) + 8 * ratio)
        g = int(22 * (1 - ratio) + 12 * ratio)
        b = int(38 * (1 - ratio) + 20 * ratio)
        draw.line([(0, y), (CANVAS_WIDTH, y)], fill=(r, g, b, 255))
    
    # 2. Ambient top glow
    glow = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.ellipse(
        [(CANVAS_WIDTH // 2 - 380, -80), (CANVAS_WIDTH // 2 + 380, 520)],
        fill=(badge_color[0], badge_color[1], badge_color[2], 40)
    )
    glow = glow.filter(ImageFilter.GaussianBlur(100))
    base = Image.alpha_composite(base, glow)
    
    # 3. Typography setup
    font_bold = "C:/Windows/Fonts/segoeuib.ttf"
    font_regular = "C:/Windows/Fonts/segoeui.ttf"
    
    badge_font = ImageFont.truetype(font_bold, 26)
    title_font = ImageFont.truetype(font_bold, 62)
    sub_font = ImageFont.truetype(font_regular, 32)
    
    # Measure badge
    badge_bbox = badge_font.getbbox(badge_text)
    badge_w = badge_bbox[2] - badge_bbox[0]
    badge_h = badge_bbox[3] - badge_bbox[1]
    
    pad_x = 28
    pad_y = 12
    box_w = badge_w + pad_x * 2
    box_h = badge_h + pad_y * 2
    badge_x = (CANVAS_WIDTH - box_w) // 2
    badge_y = 110
    
    # Draw badge pill on separate RGBA layer for perfect alpha blending
    badge_layer = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    badge_draw = ImageDraw.Draw(badge_layer)
    badge_draw.rounded_rectangle(
        [(badge_x, badge_y), (badge_x + box_w, badge_y + box_h)],
        radius=box_h // 2,
        fill=(badge_color[0], badge_color[1], badge_color[2], 45),
        outline=(badge_color[0], badge_color[1], badge_color[2], 180),
        width=2
    )
    # Badge text
    badge_draw.text(
        (badge_x + pad_x, badge_y + pad_y - badge_bbox[1] - 1),
        badge_text,
        font=badge_font,
        fill=(badge_color[0], badge_color[1], badge_color[2], 255)
    )
    base = Image.alpha_composite(base, badge_layer)
    
    # 4. Draw Headline & Subtext
    text_layer = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    text_draw = ImageDraw.Draw(text_layer)
    
    title_bbox = title_font.getbbox(headline_text)
    title_w = title_bbox[2] - title_bbox[0]
    title_x = (CANVAS_WIDTH - title_w) // 2
    title_y = badge_y + box_h + 30
    text_draw.text((title_x, title_y), headline_text, font=title_font, fill=(255, 255, 255, 255))
    
    sub_bbox = sub_font.getbbox(subtext)
    sub_w = sub_bbox[2] - sub_bbox[0]
    sub_x = (CANVAS_WIDTH - sub_w) // 2
    sub_y = title_y + 82
    text_draw.text((sub_x, sub_y), subtext, font=sub_font, fill=(148, 163, 184, 255))
    base = Image.alpha_composite(base, text_layer)
    
    # 5. Process Phone Screenshot
    phone_ui = Image.open(raw_screenshot_path).convert('RGBA')
    
    TARGET_UI_WIDTH = 930
    aspect = phone_ui.height / phone_ui.width
    TARGET_UI_HEIGHT = int(TARGET_UI_WIDTH * aspect)
    
    phone_ui_resized = phone_ui.resize((TARGET_UI_WIDTH, TARGET_UI_HEIGHT), Image.Resampling.LANCZOS)
    
    CORNER_RADIUS = 52
    mask = create_rounded_mask((TARGET_UI_WIDTH, TARGET_UI_HEIGHT), CORNER_RADIUS)
    
    ui_x = (CANVAS_WIDTH - TARGET_UI_WIDTH) // 2
    ui_y = 520
    
    # Drop shadow
    shadow = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow)
    shadow_draw.rounded_rectangle(
        [(ui_x - 6, ui_y + 16), (ui_x + TARGET_UI_WIDTH + 6, ui_y + TARGET_UI_HEIGHT + 24)],
        radius=CORNER_RADIUS + 6,
        fill=(0, 0, 0, 210)
    )
    shadow = shadow.filter(ImageFilter.GaussianBlur(36))
    base = Image.alpha_composite(base, shadow)
    
    # Paste screenshot
    base.paste(phone_ui_resized, (ui_x, ui_y), mask)
    
    # Border stroke
    stroke_layer = Image.new('RGBA', (CANVAS_WIDTH, CANVAS_HEIGHT), (0, 0, 0, 0))
    stroke_draw = ImageDraw.Draw(stroke_layer)
    stroke_draw.rounded_rectangle(
        [(ui_x, ui_y), (ui_x + TARGET_UI_WIDTH, ui_y + TARGET_UI_HEIGHT)],
        radius=CORNER_RADIUS,
        outline=(255, 255, 255, 45),
        width=3
    )
    base = Image.alpha_composite(base, stroke_layer)
    
    # 6. Save final RGB PNG
    final_img = base.convert('RGB')
    final_img.save(output_path, 'PNG', quality=95)
    print(f"Generated: {output_path}")

def main():
    os.makedirs('screenshots/store_listing', exist_ok=True)
    
    slides = [
        {
            "raw": "screenshots/raw/01_highlights.png",
            "out": "screenshots/store_listing/01_highlights_banner.png",
            "badge": "OFFICIAL MATCH CLIPS",
            "title": "Watch Match Highlights",
            "sub": "High-quality video recaps & decisive goals",
            "color": (56, 189, 248) # Blue
        },
        {
            "raw": "screenshots/raw/02_fixtures.png",
            "out": "screenshots/store_listing/02_fixtures_banner.png",
            "badge": "SCORES & SCHEDULES",
            "title": "Live Scores & Fixtures",
            "sub": "Real-time updates across major competitions",
            "color": (16, 185, 129) # Emerald Green
        },
        {
            "raw": "screenshots/raw/03_filter.png",
            "out": "screenshots/store_listing/03_leagues_banner.png",
            "badge": "EXPLORE LEAGUES",
            "title": "Filter Top Competitions",
            "sub": "Premier League, La Liga, UCL, Serie A & more",
            "color": (168, 85, 247) # Purple
        },
        {
            "raw": "screenshots/raw/04_info.png",
            "out": "screenshots/store_listing/04_premium_banner.png",
            "badge": "AD-FREE EXPERIENCE",
            "title": "Pure Football Experience",
            "sub": "Cross-device account sync & lifetime ad-free upgrade",
            "color": (251, 191, 36) # Gold / Amber
        }
    ]
    
    for s in slides:
        generate_banner(
            raw_screenshot_path=s["raw"],
            output_path=s["out"],
            badge_text=s["badge"],
            headline_text=s["title"],
            subtext=s["sub"],
            badge_color=s["color"]
        )

if __name__ == "__main__":
    main()
