import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_rounded_mask(size, radius):
    mask = Image.new('L', size, 0)
    draw = ImageDraw.Draw(mask)
    draw.rounded_rectangle([(0, 0), size], radius=radius, fill=255)
    return mask

def generate_app_icon(output_path, size=(512, 512)):
    w, h = size
    # Create base canvas with supersampling for crisp edges
    scale = 2
    sw, sh = w * scale, h * scale
    img = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Background squircle with rich gradient
    # Color 1: #0284c7 (Sky Blue), Color 2: #4f46e5 (Indigo), Color 3: #7c3aed (Violet)
    corner_radius = int(sw * 0.22)
    
    # Render gradient
    for y in range(sh):
        ratio = y / sh
        r = int(14 * (1 - ratio) + 99 * ratio)
        g = int(132 * (1 - ratio) + 102 * ratio)
        b = int(233 * (1 - ratio) + 241 * ratio)
        draw.line([(0, y), (sw, y)], fill=(r, g, b, 255))
    
    # Clip to rounded rectangle
    mask = create_rounded_mask((sw, sh), corner_radius)
    bg_img = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
    bg_img.paste(img, (0, 0), mask)
    
    draw = ImageDraw.Draw(bg_img)

    # Subtle inner bevel/highlight
    draw.rounded_rectangle([(8 * scale, 8 * scale), (sw - 8 * scale, sh - 8 * scale)],
                           radius=corner_radius - 8 * scale,
                           outline=(255, 255, 255, 45), width=3 * scale)

    # Draw stylized speech bubble geometry
    # Primary Speech Bubble (Discourse Forum dialogue)
    # Center position
    cx, cy = sw // 2, sh // 2 - 10 * scale
    bw, bh = int(sw * 0.58), int(sh * 0.46)
    bx1, by1 = cx - bw // 2, cy - bh // 2
    bx2, by2 = cx + bw // 2, cy + bh // 2
    b_radius = int(bw * 0.28)

    # Shadow for primary bubble
    shadow_img = Image.new("RGBA", (sw, sh), (0, 0, 0, 0))
    s_draw = ImageDraw.Draw(shadow_img)
    s_draw.rounded_rectangle([(bx1, by1 + 14 * scale), (bx2, by2 + 14 * scale)], radius=b_radius, fill=(0, 0, 0, 110))
    # Tail shadow
    tail_pts_s = [
        (bx1 + 32 * scale, by2 + 10 * scale),
        (bx1 + 12 * scale, by2 + 38 * scale),
        (bx1 + 68 * scale, by2 + 12 * scale)
    ]
    s_draw.polygon(tail_pts_s, fill=(0, 0, 0, 110))
    shadow_img = shadow_img.filter(ImageFilter.GaussianBlur(16 * scale))
    bg_img.alpha_composite(shadow_img)

    # Main bubble (crisp white)
    draw = ImageDraw.Draw(bg_img)
    draw.rounded_rectangle([(bx1, by1), (bx2, by2)], radius=b_radius, fill=(255, 255, 255, 255))
    # Speech bubble tail
    tail_pts = [
        (bx1 + 32 * scale, by2 - 4 * scale),
        (bx1 + 10 * scale, by2 + 24 * scale),
        (bx1 + 64 * scale, by2 - 4 * scale)
    ]
    draw.polygon(tail_pts, fill=(255, 255, 255, 255))

    # Inner dialogue elements / modern forum lines inside bubble
    line_color = (79, 70, 229, 230)      # Indigo
    accent_color = (14, 165, 233, 230)    # Sky blue
    alt_accent = (236, 72, 153, 230)      # Pink notification dot

    # Line 1: Forum Topic Title bar
    l1_y = by1 + int(bh * 0.28)
    draw.rounded_rectangle([(bx1 + int(bw * 0.12), l1_y), (bx1 + int(bw * 0.75), l1_y + max(2, int(bh * 0.12)))],
                           radius=max(1, int(bh * 0.06)), fill=line_color)
    
    # Line 2: Forum Post Body snippet
    l2_y = l1_y + int(bh * 0.22)
    draw.rounded_rectangle([(bx1 + int(bw * 0.12), l2_y), (bx1 + int(bw * 0.85), l2_y + max(2, int(bh * 0.10)))],
                           radius=max(1, int(bh * 0.05)), fill=accent_color)
    
    # Line 3: Forum Subline
    l3_y = l2_y + int(bh * 0.18)
    draw.rounded_rectangle([(bx1 + int(bw * 0.12), l3_y), (bx1 + int(bw * 0.55), l3_y + max(2, int(bh * 0.10)))],
                           radius=max(1, int(bh * 0.05)), fill=(165, 180, 252, 220))

    # Second overlapping subtle conversation bubble (Top right)
    s2_w, s2_h = int(bw * 0.44), int(bh * 0.40)
    s2_x1, s2_y1 = bx2 - s2_w + 38 * scale, by1 - 32 * scale
    s2_x2, s2_y2 = s2_x1 + s2_w, s2_y1 + s2_h
    s2_rad = int(s2_w * 0.3)

    # Floating notification bubble (emerald / cyan or ruby pink)
    draw.rounded_rectangle([(s2_x1, s2_y1), (s2_x2, s2_y2)], radius=s2_rad, fill=(244, 63, 94, 255),
                           outline=(255, 255, 255, 255), width=4 * scale)
    
    # Mini counter / icon in badge (stylized message count or check)
    badge_cx = (s2_x1 + s2_x2) // 2
    badge_cy = (s2_y1 + s2_y2) // 2
    draw.ellipse([(badge_cx - 8 * scale, badge_cy - 8 * scale), (badge_cx + 8 * scale, badge_cy + 8 * scale)],
                 fill=(255, 255, 255, 255))

    # Resize back with High Quality Lanczos
    final_img = bg_img.resize((w, h), Image.Resampling.LANCZOS)
    final_img.save(output_path, "PNG")
    print(f"Generated icon: {output_path} ({w}x{h})")

def generate_banner(output_path, size=(1280, 640)):
    w, h = size
    img = Image.new("RGBA", (w, h), (13, 17, 23, 255)) # Dark GitHub-friendly background
    draw = ImageDraw.Draw(img)

    # Ambient glows in background
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(glow)
    g_draw.ellipse([( -100, -100), (500, 500)], fill=(37, 99, 235, 45))
    g_draw.ellipse([(700, 100), (1400, 700)], fill=(124, 58, 237, 40))
    g_draw.ellipse([(400, 300), (900, 800)], fill=(14, 165, 233, 30))
    glow = glow.filter(ImageFilter.GaussianBlur(90))
    img.alpha_composite(glow)

    # Draw decorative grid lines
    draw = ImageDraw.Draw(img)
    for gx in range(0, w, 48):
        draw.line([(gx, 0), (gx, h)], fill=(255, 255, 255, 8))
    for gy in range(0, h, 48):
        draw.line([(0, gy), (w, gy)], fill=(255, 255, 255, 8))

    # Left Section: Branding & Features
    # Draw Mini Icon
    icon_temp = "C:/Users/Administrator/.gemini/antigravity/scratch/Discoursey/icon_temp.png"
    generate_app_icon(icon_temp, size=(110, 110))
    icon_p = Image.open(icon_temp).convert("RGBA")
    img.paste(icon_p, (70, 75), icon_p)

    # Try to load Arial or default font
    try:
        title_font = ImageFont.truetype("arialbd.ttf", 52)
        sub_font = ImageFont.truetype("arial.ttf", 20)
        tag_font = ImageFont.truetype("arialbd.ttf", 15)
        body_font = ImageFont.truetype("arial.ttf", 16)
        code_font = ImageFont.truetype("consola.ttf", 14)
    except:
        title_font = ImageFont.load_default()
        sub_font = ImageFont.load_default()
        tag_font = ImageFont.load_default()
        body_font = ImageFont.load_default()
        code_font = ImageFont.load_default()

    # Title & Subtitle
    draw.text((205, 80), "Discoursey", fill=(255, 255, 255), font=title_font)
    draw.text((207, 142), "Open-Source Discourse Forum Client for Android", fill=(148, 163, 184), font=sub_font)

    # Badges row
    badges = [
        ("v0.0.1 BETA", (239, 68, 68), (255, 255, 255)),
        ("GPL-3.0 LICENSE", (34, 197, 94), (10, 10, 10)),
        ("BUILT WITH AI", (139, 92, 246), (255, 255, 255)),
        ("ARMv7 & ARMv8", (14, 165, 233), (255, 255, 255)),
        ("F-DROID READY", (245, 158, 11), (10, 10, 10)),
    ]
    bx = 70
    by = 195
    for text, bg_col, fg_col in badges:
        bw_box = len(text) * 9 + 22
        draw.rounded_rectangle([(bx, by), (bx + bw_box, by + 28)], radius=6, fill=bg_col)
        draw.text((bx + 11, by + 6), text, fill=fg_col, font=tag_font)
        bx += bw_box + 12

    # Feature Bullet Points
    features = [
        " Connect to any Discourse forum instance worldwide",
        " Dual UI Engines: Sleek Modern UI & Clean Classic Old UI",
        " Full Topic Browser with rich markdown & avatar activity stream",
        " Private Messages & Notification Inbox with Read/Unread Sync",
        " Customizable Lower Navigation Bar (Pick your 5 favorites)",
        " Discourse Image Album & Adaptive Video Stream integrations",
        " Deep offline caching, font sizing, and theme customization"
    ]
    fy = 250
    for feat in features:
        draw.text((70, fy), feat, fill=(226, 232, 240), font=body_font)
        fy += 31

    # Right Section: Stylized Mockup Preview of Modern UI and Old UI
    # Frame 1: Modern UI Card (Right side)
    mx1, my1, mx2, my2 = 740, 65, 1210, 575
    draw.rounded_rectangle([(mx1, my1), (mx2, my2)], radius=24, fill=(18, 18, 18),
                           outline=(45, 55, 72), width=2)
    
    # Modern UI Top Bar
    draw.rounded_rectangle([(mx1, my1), (mx2, my1 + 54)], radius=24, fill=(24, 24, 27))
    draw.rectangle([(mx1, my1 + 30), (mx2, my1 + 54)], fill=(24, 24, 27)) # flatten bottom corners
    draw.text((mx1 + 22, my1 + 17), "<  Topics", fill=(255, 255, 255), font=tag_font)
    draw.text((mx2 - 40, my1 + 15), "+", fill=(255, 255, 255), font=title_font)

    # Modern Topic Card 1
    t1_y = my1 + 70
    draw.text((mx1 + 20, t1_y), "### ANNOUNCEMENTS & RELEASES ###", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 20, t1_y + 20), "49 posts, Topic pinned and closed", fill=(156, 163, 175), font=code_font)
    # Sub-card last poster
    draw.rounded_rectangle([(mx1 + 18, t1_y + 44), (mx2 - 18, t1_y + 110)], radius=12, fill=(28, 28, 30))
    draw.ellipse([(mx1 + 28, t1_y + 54), (mx1 + 68, t1_y + 94)], fill=(236, 72, 153))
    draw.text((mx1 + 38, t1_y + 64), "D", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 80, t1_y + 54), "Discourse Admin", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 80, t1_y + 75), "Official update on Discoursey Android build...", fill=(156, 163, 175), font=code_font)
    draw.text((mx2 - 120, t1_y + 54), "10m ago", fill=(107, 114, 128), font=code_font)

    # Modern Topic Card 2
    t2_y = t1_y + 125
    draw.line([(mx1 + 18, t2_y - 8), (mx2 - 18, t2_y - 8)], fill=(39, 39, 42))
    draw.text((mx1 + 20, t2_y), "Community Discussion - Mobile Client Features", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 20, t2_y + 20), "128 posts, Active", fill=(156, 163, 175), font=code_font)
    # Sub-card last poster
    draw.rounded_rectangle([(mx1 + 18, t2_y + 44), (mx2 - 18, t2_y + 110)], radius=12, fill=(28, 28, 30))
    draw.ellipse([(mx1 + 28, t2_y + 54), (mx1 + 68, t2_y + 94)], fill=(14, 165, 233))
    draw.text((mx1 + 38, t2_y + 64), "J", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 80, t2_y + 54), "John Developer", fill=(255, 255, 255), font=tag_font)
    draw.text((mx1 + 80, t2_y + 75), "Testing the customizable bottom bar slots!", fill=(156, 163, 175), font=code_font)
    draw.text((mx2 - 120, t2_y + 54), "1m ago", fill=(107, 114, 128), font=code_font)

    # Modern Topic Card 3 (Classic switch preview)
    t3_y = t2_y + 125
    draw.line([(mx1 + 18, t3_y - 8), (mx2 - 18, t3_y - 8)], fill=(39, 39, 42))
    draw.rounded_rectangle([(mx1 + 20, t3_y), (mx2 - 20, t3_y + 40)], radius=8, fill=(30, 41, 59))
    draw.text((mx1 + 32, t3_y + 12), "UI Mode Toggle: Modern Flow <-> Classic Clean", fill=(56, 189, 248), font=code_font)

    # Modern Bottom Navigation Bar
    draw.rounded_rectangle([(mx1, my2 - 58), (mx2, my2)], radius=24, fill=(24, 24, 27))
    draw.rectangle([(mx1, my2 - 58), (mx2, my2 - 30)], fill=(24, 24, 27))
    draw.line([(mx1, my2 - 58), (mx2, my2 - 58)], fill=(45, 55, 72))
    
    # 5 Nav Icons representation
    nav_labels = ["Home", "Search", "Messages", "Profile", "Menu"]
    spacing = (mx2 - mx1) // 5
    for i, label in enumerate(nav_labels):
        nx = mx1 + i * spacing + spacing // 2
        col = (255, 255, 255) if i == 2 else (148, 163, 184)
        draw.text((nx - 16, my2 - 38), label[:4], fill=col, font=code_font)
        if i == 2: # Notification red badge
            draw.ellipse([(nx + 10, my2 - 46), (nx + 26, my2 - 30)], fill=(239, 68, 68))
            draw.text((nx + 13, my2 - 44), "7", fill=(255, 255, 255), font=code_font)

    img.save(output_path, "PNG")
    print(f"Generated banner: {output_path} ({w}x{h})")

def generate_android_res_icons(res_dir):
    import os
    sizes = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    src_icon = Image.open("C:/Users/Administrator/.gemini/antigravity/scratch/Discoursey/app_icon.png")
    for folder, dim in sizes.items():
        target_dir = os.path.join(res_dir, folder)
        os.makedirs(target_dir, exist_ok=True)
        # Regular icon
        resized = src_icon.resize((dim, dim), Image.Resampling.LANCZOS)
        resized.save(os.path.join(target_dir, "ic_launcher.png"), "PNG")
        
        # Round icon (circle mask)
        circle_mask = Image.new("L", (dim, dim), 0)
        c_draw = ImageDraw.Draw(circle_mask)
        c_draw.ellipse([(0, 0), (dim, dim)], fill=255)
        round_icon = Image.new("RGBA", (dim, dim), (0, 0, 0, 0))
        round_icon.paste(resized, (0, 0), circle_mask)
        round_icon.save(os.path.join(target_dir, "ic_launcher_round.png"), "PNG")
        print(f"Saved {folder}/ic_launcher.png & ic_launcher_round.png ({dim}x{dim})")

if __name__ == "__main__":
    generate_app_icon("C:/Users/Administrator/.gemini/antigravity/scratch/Discoursey/app_icon.png")
    generate_banner("C:/Users/Administrator/.gemini/antigravity/scratch/Discoursey/banner.png")
    generate_android_res_icons("C:/Users/Administrator/.gemini/antigravity/scratch/Discoursey/app/src/main/res")
