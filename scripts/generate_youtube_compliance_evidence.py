"""Generate YouTube API Compliance Evidence Screenshots and PDFs."""

import os

from PIL import Image, ImageDraw

from scripts.youtube_compliance_helpers import (
    EVIDENCE_DIR,
    generate_evidence_1_privacy_policy,
    generate_evidence_3_terms_of_service,
    get_font,
)


def generate_evidence_2_homepage_branding():
    print("Generating Evidence 2: Homepage & YouTube Branding Screenshot...")
    img_home = Image.open("screenshots/home_screen.png").convert("RGB")
    img_info = Image.open("screenshots/info_privacy_scrolled_up.png").convert("RGB")

    target_height = 1600
    w_h = int(img_home.width * (target_height / img_home.height))
    w_i = int(img_info.width * (target_height / img_info.height))

    home_resized = img_home.resize((w_h, target_height), Image.Resampling.LANCZOS)
    info_resized = img_info.resize((w_i, target_height), Image.Resampling.LANCZOS)

    canvas_width = w_h + w_i + 120
    header_height = 180
    footer_height = 140
    canvas_height = target_height + header_height + footer_height

    canvas = Image.new("RGB", (canvas_width, canvas_height), (15, 23, 42))
    draw = ImageDraw.Draw(canvas)

    title_font = get_font(38, bold=True)
    subtitle_font = get_font(22, bold=False)
    tag_font = get_font(20, bold=True)
    body_font = get_font(18, bold=False)

    draw.text((60, 45), "Football Highlights & Clips — Official YouTube Attribution & Privacy Policy Flow", fill=(248, 250, 252), font=title_font)
    draw.text((60, 105), "Required Evidence #2: Homepage showing YouTube Content & Direct Navigation Link to Privacy Policy", fill=(148, 163, 184), font=subtitle_font)

    x_home = 40
    y_screen = header_height + 10
    canvas.paste(home_resized, (x_home, y_screen))

    x_info = x_home + w_h + 40
    canvas.paste(info_resized, (x_info, y_screen))

    scale = target_height / img_home.height
    feat_y1, feat_y2 = int(y_screen + (350 * scale)), int(y_screen + (980 * scale))
    feat_x1, feat_x2 = x_home + int(36 * scale), x_home + w_h - int(36 * scale)
    draw.rectangle([(feat_x1, feat_y1), (feat_x2, feat_y2)], outline=(239, 68, 68), width=6)
    draw.rectangle([(feat_x1, feat_y1 - 38), (feat_x1 + 650, feat_y1)], fill=(239, 68, 68))
    draw.text((feat_x1 + 14, feat_y1 - 32), "1. Official YouTube Video Highlights (Curated Player Embeds)", fill=(255, 255, 255), font=tag_font)

    nav_y1, nav_y2 = int(y_screen + (2130 * scale)), int(y_screen + (2350 * scale))
    nav_x1, nav_x2 = x_home + int(670 * scale), x_home + int(890 * scale)
    draw.rectangle([(nav_x1, nav_y1), (nav_x2, nav_y2)], outline=(34, 197, 94), width=6)
    draw.rectangle([(nav_x1 - 130, nav_y1 - 38), (nav_x1 + 210, nav_y1)], fill=(34, 197, 94))
    draw.text((nav_x1 - 120, nav_y1 - 32), "2. Tap 'Info' for Privacy Policy", fill=(255, 255, 255), font=tag_font)

    info_card_y1, info_card_y2 = int(y_screen + (1160 * scale)), int(y_screen + (2050 * scale))
    info_card_x1, info_card_x2 = x_info + int(36 * scale), x_info + w_i - int(36 * scale)
    draw.rectangle([(info_card_x1, info_card_y1), (info_card_x2, info_card_y2)], outline=(59, 130, 246), width=6)
    draw.rectangle([(info_card_x1, info_card_y1 - 38), (info_card_x1 + 600, info_card_y1)], fill=(59, 130, 246))
    draw.text((info_card_x1 + 14, info_card_y1 - 32), "3. Official Privacy Policy & Terms Documentation Links", fill=(255, 255, 255), font=tag_font)

    footer_y = canvas_height - 100
    draw.text((60, footer_y), "Compliance Note: End users access video highlights from verified official league/club channels.", fill=(203, 213, 225), font=body_font)
    draw.text((60, footer_y + 35), "Live Web Application: https://fc.dirzaaulia.com  •  Privacy: https://fc.dirzaaulia.com/privacy  •  Terms: https://fc.dirzaaulia.com/tnc", fill=(56, 189, 248), font=body_font)

    canvas.save(os.path.join(EVIDENCE_DIR, "2_Homepage_YouTube_Branding_and_Privacy_Policy.png"), "PNG", quality=95)
    canvas.save(os.path.join(EVIDENCE_DIR, "2_Homepage_YouTube_Branding_and_Privacy_Policy.pdf"), "PDF", resolution=100.0)


def generate_evidence_4_player_embed():
    print("Generating Evidence 4: Player / Embed Screenshots...")
    player_raw_path = "youtube_quota_evidence/web_player_screen.png"
    if not os.path.exists(player_raw_path):
        return

    img_player = Image.open(player_raw_path).convert("RGB")
    target_width = 1920
    target_height = int(img_player.height * (target_width / img_player.width))
    player_resized = img_player.resize((target_width, target_height), Image.Resampling.LANCZOS)

    header_height = 160
    footer_height = 140
    canvas_height = target_height + header_height + footer_height

    canvas = Image.new("RGB", (target_width, canvas_height), (15, 23, 42))
    draw = ImageDraw.Draw(canvas)

    title_font = get_font(34, bold=True)
    subtitle_font = get_font(20, bold=False)
    tag_font = get_font(18, bold=True)
    body_font = get_font(18, bold=False)

    draw.text((60, 40), "Football Highlights & Clips — Official YouTube Player Embed & Compliance Showcase", fill=(248, 250, 252), font=title_font)
    draw.text((60, 95), "Conditional Evidence c: Player / Embed Screenshots (Websites & Mobile Apps)", fill=(148, 163, 184), font=subtitle_font)

    y_screen = header_height
    canvas.paste(player_resized, (0, y_screen))
    scale = target_width / img_player.width

    logo_x1, logo_y1 = int(1640 * scale), int(y_screen + (1490 * scale))
    logo_x2, logo_y2 = int(1860 * scale), int(y_screen + (1570 * scale))
    draw.rectangle([(logo_x1, logo_y1), (logo_x2, logo_y2)], outline=(239, 68, 68), width=5)
    draw.rectangle([(logo_x1 - 100, logo_y1 - 34), (logo_x1 + 250, logo_y1)], fill=(239, 68, 68))
    draw.text((logo_x1 - 90, logo_y1 - 28), "1. Official YouTube Watermark & Logo", fill=(255, 255, 255), font=tag_font)

    title_x1, title_y1 = int(80 * scale), int(y_screen + (130 * scale))
    title_x2, title_y2 = int(1520 * scale), int(y_screen + (270 * scale))
    draw.rectangle([(title_x1, title_y1), (title_x2, title_y2)], outline=(34, 197, 94), width=5)
    draw.rectangle([(title_x1 + 20, title_y1 - 34), (title_x1 + 680, title_y1)], fill=(34, 197, 94))
    draw.text((title_x1 + 30, title_y1 - 28), "2. Official Verified Club Channel Attribution (Direct Creator Promotion)", fill=(255, 255, 255), font=tag_font)

    ctrl_x1, ctrl_y1 = int(80 * scale), int(y_screen + (1420 * scale))
    ctrl_x2, ctrl_y2 = int(1860 * scale), int(y_screen + (1580 * scale))
    draw.rectangle([(ctrl_x1, ctrl_y1), (ctrl_x2, ctrl_y2)], outline=(59, 130, 246), width=5)
    draw.rectangle([(ctrl_x1 + 20, ctrl_y1 - 34), (ctrl_x1 + 860, ctrl_y1)], fill=(59, 130, 246))
    draw.text((ctrl_x1 + 30, ctrl_y1 - 28), "3. Standard YouTube IFrame Player Controls (Timeline, Play/Pause, Volume, Fullscreen, CC)", fill=(255, 255, 255), font=tag_font)

    footer_y = canvas_height - 95
    draw.text((60, footer_y), "Compliance Statement: Video highlights stream exclusively via the official YouTube IFrame Player API.", fill=(203, 213, 225), font=body_font)
    draw.text((60, footer_y + 35), "All creator views, monetization, and official branding are 100% preserved. Web Demo: https://fc.dirzaaulia.com", fill=(56, 189, 248), font=body_font)

    canvas.save(os.path.join(EVIDENCE_DIR, "4_Player_Embed_Screenshots.png"), "PNG", quality=95)
    canvas.save(os.path.join(EVIDENCE_DIR, "4_Player_Embed_Screenshots.pdf"), "PDF", resolution=100.0)


if __name__ == "__main__":
    generate_evidence_1_privacy_policy()
    generate_evidence_2_homepage_branding()
    generate_evidence_3_terms_of_service()
    generate_evidence_4_player_embed()
    print("\nALL EVIDENCE GENERATION COMPLETE!")
