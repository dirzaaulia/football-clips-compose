import os
import sys
from playwright.sync_api import sync_playwright
from PIL import Image, ImageDraw, ImageFont

EVIDENCE_DIR = os.path.abspath("youtube_quota_evidence")
os.makedirs(EVIDENCE_DIR, exist_ok=True)

PRIVACY_HTML = os.path.abspath("app/src/wasmJsMain/resources/privacy.html").replace("\\", "/")
TNC_HTML = os.path.abspath("app/src/wasmJsMain/resources/tnc.html").replace("\\", "/")

def get_font(size=24, bold=False):
    # Try Windows fonts
    font_names = [
        "arialbd.ttf" if bold else "arial.ttf",
        "segoeuib.ttf" if bold else "segoeui.ttf",
        "calibrib.ttf" if bold else "calibri.ttf"
    ]
    for fn in font_names:
        p = os.path.join("C:/Windows/Fonts", fn)
        if os.path.exists(p):
            try:
                return ImageFont.truetype(p, size)
            except Exception:
                pass
    return ImageFont.load_default()

def generate_evidence_1_privacy_policy():
    print("Generating Evidence 1: Privacy Policy...")
    with sync_playwright() as p:
        browser = p.chromium.launch()
        # Create page with desktop viewport for clean document rendering
        page = browser.new_page(viewport={"width": 1280, "height": 1800}, device_scale_factor=2)
        page.goto(f"file:///{PRIVACY_HTML}", wait_until="networkidle")

        # Inject visual highlight styling for audit boxes before screenshot
        page.evaluate("""
            // Add custom red borders around YouTube section and Data Deletion section
            const cards = document.querySelectorAll('.info-card');
            cards.forEach(card => {
                if (card.innerText.includes('YouTube API Services')) {
                    card.style.border = '3px solid #ef4444';
                    card.style.backgroundColor = 'rgba(239, 68, 68, 0.08)';
                    card.style.position = 'relative';
                    const badge = document.createElement('div');
                    badge.innerText = 'AUDIT REQUIREMENT: YouTube API Services & ToS Agreement';
                    badge.style.position = 'absolute';
                    badge.style.top = '-14px';
                    badge.style.right = '16px';
                    badge.style.background = '#ef4444';
                    badge.style.color = '#ffffff';
                    badge.style.fontSize = '12px';
                    badge.style.fontWeight = 'bold';
                    badge.style.padding = '2px 10px';
                    badge.style.borderRadius = '4px';
                    badge.style.boxShadow = '0 2px 6px rgba(0,0,0,0.2)';
                    card.appendChild(badge);
                }
            });

            // Find Section 4 Data Retention
            const headers = document.querySelectorAll('h2');
            headers.forEach(h2 => {
                if (h2.innerText.includes('Data Retention & Deletion Rights')) {
                    const parent = h2.parentElement;
                    const pElements = [];
                    let sibling = h2.nextElementSibling;
                    while (sibling && sibling.tagName === 'P') {
                        pElements.push(sibling);
                        sibling = sibling.nextElementSibling;
                    }
                    if (pElements.length > 0) {
                        const wrapper = document.createElement('div');
                        wrapper.style.border = '3px solid #3b82f6';
                        wrapper.style.backgroundColor = 'rgba(59, 130, 246, 0.08)';
                        wrapper.style.borderRadius = '12px';
                        wrapper.style.padding = '16px';
                        wrapper.style.margin = '16px 0';
                        wrapper.style.position = 'relative';
                        
                        const badge = document.createElement('div');
                        badge.innerText = 'AUDIT REQUIREMENT: Data Deletion & Google Security Settings Link';
                        badge.style.position = 'absolute';
                        badge.style.top = '-14px';
                        badge.style.right = '16px';
                        badge.style.background = '#3b82f6';
                        badge.style.color = '#ffffff';
                        badge.style.fontSize = '12px';
                        badge.style.fontWeight = 'bold';
                        badge.style.padding = '2px 10px';
                        badge.style.borderRadius = '4px';
                        badge.style.boxShadow = '0 2px 6px rgba(0,0,0,0.2)';
                        wrapper.appendChild(badge);

                        h2.parentNode.insertBefore(wrapper, h2.nextSibling);
                        pElements.forEach(p => wrapper.appendChild(p));
                    }
                }
            });
        """)

        # 1. Full Page Screenshot
        png_path = os.path.join(EVIDENCE_DIR, "1_Privacy_Policy_Compliance.png")
        page.screenshot(path=png_path, full_page=True)
        print(f"Saved: {png_path}")

        # 2. PDF Document
        pdf_path = os.path.join(EVIDENCE_DIR, "1_Privacy_Policy_Compliance.pdf")
        page.pdf(
            path=pdf_path,
            format="A4",
            print_background=True,
            margin={"top": "20mm", "bottom": "20mm", "left": "15mm", "right": "15mm"}
        )
        print(f"Saved: {pdf_path}")
        browser.close()

def generate_evidence_2_homepage_branding():
    print("Generating Evidence 2: Homepage & YouTube Branding Screenshot...")
    
    # Load Android screenshots
    home_raw_path = "screenshots/home_screen.png"
    info_raw_path = "screenshots/info_privacy_scrolled_up.png"

    img_home = Image.open(home_raw_path).convert("RGB")
    img_info = Image.open(info_raw_path).convert("RGB")

    # Combine into side-by-side presentation canvas
    target_height = 1600
    w_h = int(img_home.width * (target_height / img_home.height))
    w_i = int(img_info.width * (target_height / img_info.height))

    home_resized = img_home.resize((w_h, target_height), Image.Resampling.LANCZOS)
    info_resized = img_info.resize((w_i, target_height), Image.Resampling.LANCZOS)

    canvas_width = w_h + w_i + 120
    header_height = 180
    footer_height = 140
    canvas_height = target_height + header_height + footer_height

    canvas = Image.new("RGB", (canvas_width, canvas_height), (15, 23, 42)) # Slate 900
    draw = ImageDraw.Draw(canvas)

    title_font = get_font(38, bold=True)
    subtitle_font = get_font(22, bold=False)
    tag_font = get_font(20, bold=True)
    body_font = get_font(18, bold=False)

    # Header
    draw.text((60, 45), "Football Highlights & Clips — Official YouTube Attribution & Privacy Policy Flow", fill=(248, 250, 252), font=title_font)
    draw.text((60, 105), "Required Evidence #2: Homepage showing YouTube Content & Direct Navigation Link to Privacy Policy", fill=(148, 163, 184), font=subtitle_font)

    # Paste screens
    x_home = 40
    y_screen = header_height + 10
    canvas.paste(home_resized, (x_home, y_screen))

    x_info = x_home + w_h + 40
    canvas.paste(info_resized, (x_info, y_screen))

    # Annotate Screen 1 (Homepage)
    # Original image: (1080, 2424), target_height = 1600 -> scale = 1600/2424 = 0.660066
    scale = target_height / img_home.height

    # 1. Featured card box (from y=350 to y=980 on 2424)
    feat_y1 = int(y_screen + (350 * scale))
    feat_y2 = int(y_screen + (980 * scale))
    feat_x1 = x_home + int(36 * scale)
    feat_x2 = x_home + w_h - int(36 * scale)
    draw.rectangle([(feat_x1, feat_y1), (feat_x2, feat_y2)], outline=(239, 68, 68), width=6)

    # Tag for Box 1
    tag_text_1 = "1. Official YouTube Video Highlights (Curated Player Embeds)"
    draw.rectangle([(feat_x1, feat_y1 - 38), (feat_x1 + 650, feat_y1)], fill=(239, 68, 68))
    draw.text((feat_x1 + 14, feat_y1 - 32), tag_text_1, fill=(255, 255, 255), font=tag_font)

    # 2. Bottom Nav Info button box (from y=2130 to y=2350 on 2424, x from 670 to 890 on 1080)
    nav_y1 = int(y_screen + (2130 * scale))
    nav_y2 = int(y_screen + (2350 * scale))
    nav_x1 = x_home + int(670 * scale)
    nav_x2 = x_home + int(890 * scale)
    draw.rectangle([(nav_x1, nav_y1), (nav_x2, nav_y2)], outline=(34, 197, 94), width=6)

    tag_text_2 = "2. Tap 'Info' for Privacy Policy"
    draw.rectangle([(nav_x1 - 130, nav_y1 - 38), (nav_x1 + 210, nav_y1)], fill=(34, 197, 94))
    draw.text((nav_x1 - 120, nav_y1 - 32), tag_text_2, fill=(255, 255, 255), font=tag_font)

    # Annotate Screen 2 (Info Screen)
    # The Privacy Policy & Terms card starts around y=1160 to y=2050 on 2424
    info_card_y1 = int(y_screen + (1160 * scale))
    info_card_y2 = int(y_screen + (2050 * scale))
    info_card_x1 = x_info + int(36 * scale)
    info_card_x2 = x_info + w_i - int(36 * scale)
    draw.rectangle([(info_card_x1, info_card_y1), (info_card_x2, info_card_y2)], outline=(59, 130, 246), width=6)

    tag_text_3 = "3. Official Privacy Policy & Terms Documentation Links"
    draw.rectangle([(info_card_x1, info_card_y1 - 38), (info_card_x1 + 600, info_card_y1)], fill=(59, 130, 246))
    draw.text((info_card_x1 + 14, info_card_y1 - 32), tag_text_3, fill=(255, 255, 255), font=tag_font)

    # Footer note
    footer_y = canvas_height - 100
    draw.text((60, footer_y), "Compliance Note: End users access video highlights from verified official league/club channels. Privacy Policy and Terms are prominent and 1-tap accessible.", fill=(203, 213, 225), font=body_font)
    draw.text((60, footer_y + 35), "Live Web Application: https://fc.dirzaaulia.com  •  Privacy: https://fc.dirzaaulia.com/privacy  •  Terms: https://fc.dirzaaulia.com/tnc", fill=(56, 189, 248), font=body_font)

    # Save PNG
    out_png = os.path.join(EVIDENCE_DIR, "2_Homepage_YouTube_Branding_and_Privacy_Policy.png")
    canvas.save(out_png, "PNG", quality=95)
    print(f"Saved: {out_png}")

    # Save PDF
    out_pdf = os.path.join(EVIDENCE_DIR, "2_Homepage_YouTube_Branding_and_Privacy_Policy.pdf")
    canvas.save(out_pdf, "PDF", resolution=100.0)
    print(f"Saved: {out_pdf}")

def generate_evidence_3_terms_of_service():
    print("Generating Evidence 3: Terms of Service Documentation...")
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page(viewport={"width": 1280, "height": 1800}, device_scale_factor=2)
        page.goto(f"file:///{TNC_HTML}", wait_until="networkidle")

        # Inject visual highlight styling for audit boxes before screenshot
        page.evaluate("""
            const lis = document.querySelectorAll('li');
            lis.forEach(li => {
                if (li.innerText.includes('Official Video Embeds') || li.innerText.includes('Third-Party Content Rights')) {
                    li.style.border = '2px solid #ef4444';
                    li.style.backgroundColor = 'rgba(239, 68, 68, 0.08)';
                    li.style.padding = '10px 14px';
                    li.style.borderRadius = '8px';
                    li.style.margin = '8px 0';
                }
            });
        """)

        # 1. Full Page Screenshot
        png_path = os.path.join(EVIDENCE_DIR, "3_Terms_of_Service_Documentation.png")
        page.screenshot(path=png_path, full_page=True)
        print(f"Saved: {png_path}")

        # 2. PDF Document
        pdf_path = os.path.join(EVIDENCE_DIR, "3_Terms_of_Service_Documentation.pdf")
        page.pdf(
            path=pdf_path,
            format="A4",
            print_background=True,
            margin={"top": "20mm", "bottom": "20mm", "left": "15mm", "right": "15mm"}
        )
        print(f"Saved: {pdf_path}")
        browser.close()

def generate_evidence_4_player_embed():
    print("Generating Evidence 4: Player / Embed Screenshots (Conditional Evidence c)...")
    player_raw_path = "youtube_quota_evidence/web_player_screen.png"
    if not os.path.exists(player_raw_path):
        print(f"Error: {player_raw_path} not found.")
        return

    img_player = Image.open(player_raw_path).convert("RGB")
    
    # Create canvas with top banner and footer notes
    target_width = 1920
    target_height = int(img_player.height * (target_width / img_player.width))
    player_resized = img_player.resize((target_width, target_height), Image.Resampling.LANCZOS)

    header_height = 160
    footer_height = 140
    canvas_height = target_height + header_height + footer_height

    canvas = Image.new("RGB", (target_width, canvas_height), (15, 23, 42)) # Slate 900
    draw = ImageDraw.Draw(canvas)

    title_font = get_font(34, bold=True)
    subtitle_font = get_font(20, bold=False)
    tag_font = get_font(18, bold=True)
    body_font = get_font(18, bold=False)

    # Header
    draw.text((60, 40), "Football Highlights & Clips — Official YouTube Player Embed & Compliance Showcase", fill=(248, 250, 252), font=title_font)
    draw.text((60, 95), "Conditional Evidence c: Player / Embed Screenshots (Websites & Mobile Apps)", fill=(148, 163, 184), font=subtitle_font)

    # Paste player screenshot
    y_screen = header_height
    canvas.paste(player_resized, (0, y_screen))

    # Calculate scale from original (2880 x 1800) to target (1920 x 1200)
    scale = target_width / img_player.width

    # Box 1: YouTube Logo (Watermark / Attribution)
    # Original coordinates around x=1650 to 1860, y=1480 to 1580
    logo_x1 = int(1640 * scale)
    logo_y1 = int(y_screen + (1490 * scale))
    logo_x2 = int(1860 * scale)
    logo_y2 = int(y_screen + (1570 * scale))
    draw.rectangle([(logo_x1, logo_y1), (logo_x2, logo_y2)], outline=(239, 68, 68), width=5)

    tag_text_1 = "1. Official YouTube Watermark & Logo"
    draw.rectangle([(logo_x1 - 100, logo_y1 - 34), (logo_x1 + 250, logo_y1)], fill=(239, 68, 68))
    draw.text((logo_x1 - 90, logo_y1 - 28), tag_text_1, fill=(255, 255, 255), font=tag_font)

    # Box 2: Official Channel & Video Title
    title_x1 = int(80 * scale)
    title_y1 = int(y_screen + (130 * scale))
    title_x2 = int(1520 * scale)
    title_y2 = int(y_screen + (270 * scale))
    draw.rectangle([(title_x1, title_y1), (title_x2, title_y2)], outline=(34, 197, 94), width=5)

    tag_text_2 = "2. Official Verified Club Channel Attribution (Direct Creator Promotion)"
    draw.rectangle([(title_x1 + 20, title_y1 - 34), (title_x1 + 680, title_y1)], fill=(34, 197, 94))
    draw.text((title_x1 + 30, title_y1 - 28), tag_text_2, fill=(255, 255, 255), font=tag_font)

    # Box 3: Standard Unmodified Controls
    ctrl_x1 = int(80 * scale)
    ctrl_y1 = int(y_screen + (1420 * scale))
    ctrl_x2 = int(1860 * scale)
    ctrl_y2 = int(y_screen + (1580 * scale))
    draw.rectangle([(ctrl_x1, ctrl_y1), (ctrl_x2, ctrl_y2)], outline=(59, 130, 246), width=5)

    tag_text_3 = "3. Standard YouTube IFrame Player Controls (Timeline, Play/Pause, Volume, Fullscreen, CC)"
    draw.rectangle([(ctrl_x1 + 20, ctrl_y1 - 34), (ctrl_x1 + 860, ctrl_y1)], fill=(59, 130, 246))
    draw.text((ctrl_x1 + 30, ctrl_y1 - 28), tag_text_3, fill=(255, 255, 255), font=tag_font)

    # Footer
    footer_y = canvas_height - 95
    draw.text((60, footer_y), "Compliance Statement: Video highlights stream exclusively via the official YouTube IFrame Player API. No video media is downloaded, cached, or altered.", fill=(203, 213, 225), font=body_font)
    draw.text((60, footer_y + 35), "All creator views, monetization, and official branding are 100% preserved. Web Demo: https://fc.dirzaaulia.com", fill=(56, 189, 248), font=body_font)

    # Save PNG
    out_png = os.path.join(EVIDENCE_DIR, "4_Player_Embed_Screenshots.png")
    canvas.save(out_png, "PNG", quality=95)
    print(f"Saved: {out_png}")

    # Save PDF
    out_pdf = os.path.join(EVIDENCE_DIR, "4_Player_Embed_Screenshots.pdf")
    canvas.save(out_pdf, "PDF", resolution=100.0)
    print(f"Saved: {out_pdf}")

if __name__ == "__main__":
    generate_evidence_1_privacy_policy()
    generate_evidence_2_homepage_branding()
    generate_evidence_3_terms_of_service()
    generate_evidence_4_player_embed()
    print("\nALL EVIDENCE GENERATION COMPLETE! Files located in 'youtube_quota_evidence/':")
    for f in sorted(os.listdir(EVIDENCE_DIR)):
        print(f" - {f}")
