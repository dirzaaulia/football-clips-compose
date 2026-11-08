"""Playwright HTML compliance screenshot and PDF helpers for YouTube quota audit evidence."""

import os

from PIL import ImageFont
from playwright.sync_api import sync_playwright

EVIDENCE_DIR = os.path.abspath("youtube_quota_evidence")
os.makedirs(EVIDENCE_DIR, exist_ok=True)

PRIVACY_HTML = os.path.abspath("app/src/wasmJsMain/resources/privacy.html").replace("\\", "/")
TNC_HTML = os.path.abspath("app/src/wasmJsMain/resources/tnc.html").replace("\\", "/")


def get_font(size: int = 24, bold: bool = False):
    font_names = [
        "arialbd.ttf" if bold else "arial.ttf",
        "segoeuib.ttf" if bold else "segoeui.ttf",
        "calibrib.ttf" if bold else "calibri.ttf",
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
        page = browser.new_page(viewport={"width": 1280, "height": 1800}, device_scale_factor=2)
        page.goto(f"file:///{PRIVACY_HTML}", wait_until="networkidle")
        page.evaluate("""
            const cards = document.querySelectorAll('.info-card');
            cards.forEach(card => {
                if (card.innerText.includes('YouTube API Services')) {
                    card.style.border = '3px solid #ef4444';
                    card.style.backgroundColor = 'rgba(239, 68, 68, 0.08)';
                    card.style.position = 'relative';
                    const badge = document.createElement('div');
                    badge.innerText = 'AUDIT REQUIREMENT: YouTube API Services & ToS Agreement';
                    badge.style.cssText = 'position:absolute;top:-14px;right:16px;background:#ef4444;color:#fff;font-size:12px;font-weight:bold;padding:2px 10px;border-radius:4px;';
                    card.appendChild(badge);
                }
            });
            const headers = document.querySelectorAll('h2');
            headers.forEach(h2 => {
                if (h2.innerText.includes('Data Retention & Deletion Rights')) {
                    const pElements = [];
                    let sibling = h2.nextElementSibling;
                    while (sibling && sibling.tagName === 'P') {
                        pElements.push(sibling);
                        sibling = sibling.nextElementSibling;
                    }
                    if (pElements.length > 0) {
                        const wrapper = document.createElement('div');
                        wrapper.style.cssText = 'border:3px solid #3b82f6;background:rgba(59,130,246,0.08);border-radius:12px;padding:16px;margin:16px 0;position:relative;';
                        const badge = document.createElement('div');
                        badge.innerText = 'AUDIT REQUIREMENT: Data Deletion & Google Security Settings Link';
                        badge.style.cssText = 'position:absolute;top:-14px;right:16px;background:#3b82f6;color:#fff;font-size:12px;font-weight:bold;padding:2px 10px;border-radius:4px;';
                        wrapper.appendChild(badge);
                        h2.parentNode.insertBefore(wrapper, h2.nextSibling);
                        pElements.forEach(el => wrapper.appendChild(el));
                    }
                }
            });
        """)
        png_path = os.path.join(EVIDENCE_DIR, "1_Privacy_Policy_Compliance.png")
        page.screenshot(path=png_path, full_page=True)
        pdf_path = os.path.join(EVIDENCE_DIR, "1_Privacy_Policy_Compliance.pdf")
        page.pdf(path=pdf_path, format="A4", print_background=True, margin={"top": "20mm", "bottom": "20mm", "left": "15mm", "right": "15mm"})
        browser.close()


def generate_evidence_3_terms_of_service():
    print("Generating Evidence 3: Terms of Service Documentation...")
    with sync_playwright() as p:
        browser = p.chromium.launch()
        page = browser.new_page(viewport={"width": 1280, "height": 1800}, device_scale_factor=2)
        page.goto(f"file:///{TNC_HTML}", wait_until="networkidle")
        page.evaluate("""
            const lis = document.querySelectorAll('li');
            lis.forEach(li => {
                if (li.innerText.includes('Official Video Embeds') || li.innerText.includes('Third-Party Content Rights')) {
                    li.style.cssText = 'border:2px solid #ef4444;background:rgba(239,68,68,0.08);padding:10px 14px;border-radius:8px;margin:8px 0;';
                }
            });
        """)
        png_path = os.path.join(EVIDENCE_DIR, "3_Terms_of_Service_Documentation.png")
        page.screenshot(path=png_path, full_page=True)
        pdf_path = os.path.join(EVIDENCE_DIR, "3_Terms_of_Service_Documentation.pdf")
        page.pdf(path=pdf_path, format="A4", print_background=True, margin={"top": "20mm", "bottom": "20mm", "left": "15mm", "right": "15mm"})
        browser.close()
