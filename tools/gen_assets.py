#!/usr/bin/env python3
"""Genera TODAS las texturas, modelos, blockstates, loot tables, recetas y traducciones del mod.
Uso: python3 tools/gen_assets.py   (requiere Pillow)"""
import json, os, random, math
from PIL import Image, ImageDraw

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "src", "main", "resources")
NS = "fnafnights"
A = os.path.join(ROOT, "assets", NS)
D = os.path.join(ROOT, "data", NS)
random.seed(1987)

def wj(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f: json.dump(data, f, indent=2, ensure_ascii=False)

def save(img, path):
    os.makedirs(os.path.dirname(path), exist_ok=True); img.save(path)

def jit(c, n=6):
    d = random.randint(-n, n)
    return tuple(max(0, min(255, v + d)) for v in c[:3]) + ((c[3],) if len(c) > 3 else (255,))

def tex16(fn):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 255)); px = im.load()
    for y in range(16):
        for x in range(16):
            px[x, y] = fn(x, y)
    return im

# ------------------------------------------------------------------ Texturas de bloque
B = lambda n: os.path.join(A, "textures", "block", n + ".png")
def blk(name, fn): save(tex16(fn), B(name))

def checker(x, y):
    base = (232, 232, 226) if ((x // 8) + (y // 8)) % 2 == 0 else (26, 26, 30)
    if x % 8 == 0 or y % 8 == 0: base = tuple(int(v * 0.8) for v in base)
    return jit(base, 4)
blk("checker_tile", checker)
blk("office_floor", lambda x, y: jit((70, 72, 80) if (x % 8 and y % 8) else (48, 50, 56), 5))
def wall(x, y):
    off = 4 if (y // 4) % 2 else 0
    return jit((38, 34, 58) if ((x + off) % 8 == 0 or y % 4 == 0) else (60, 54, 88), 4)
blk("pizzeria_wall", wall)
def striped(x, y):
    if y >= 13 and y <= 14: return (236, 190, 40, 255)
    return jit((108, 62, 160) if (x // 4) % 2 == 0 else (58, 34, 92), 4)
blk("pizzeria_wall_striped", striped)
def stage(x, y):
    if y % 4 == 0: return (60, 36, 24, 255)
    return jit((118, 76, 48) if (x + (y // 4) * 5) % 16 != 0 else (80, 50, 34), 6)
blk("stage_floor", stage)
def curtain(x, y):
    if y >= 14: return (236, 190, 50, 255)
    f = 0.75 + 0.25 * math.sin(x * math.pi / 2)
    return tuple(int(v * f) for v in (128, 34, 136)) + (255,)
blk("stage_curtain", curtain)
stars = {(3, 3), (10, 5), (5, 10), (12, 11), (8, 1)}
def pcurtain(x, y):
    f = 0.7 + 0.3 * math.sin(x * math.pi / 2)
    c = tuple(int(v * f) for v in (156, 28, 28))
    if (x, y) in stars or (x - 1, y) in stars or (x, y - 1) in stars: c = (240, 210, 60)
    if y >= 15: c = (90, 14, 14)
    return c + (255,)
blk("pirate_cove_curtain", pcurtain)
def dim(x, y):
    if x in (0, 15) or y in (0, 15): return (90, 90, 96, 255)
    if x in (1, 14) or y in (1, 14): return (150, 150, 130, 255)
    return (255, 244, 190, 255)
blk("dim_light", dim)
def vent(x, y):
    if x in (0, 15) or y in (0, 15): return (110, 112, 120, 255)
    return (14, 14, 18, 255) if y % 3 == 1 else (52, 54, 60, 255)
blk("vent_grate", vent)
def cloth(x, y):
    return jit((200, 38, 62) if ((x // 4) + (y // 4)) % 2 == 0 else (244, 244, 244), 5)
blk("party_table", cloth)
def door(x, y):
    if y <= 2 or y >= 13: return (236, 190, 30, 255) if ((x + y) // 2) % 2 == 0 else (24, 24, 24, 255)
    if (x in (2, 13)) and (y in (4, 11)): return (60, 60, 64, 255)
    return jit((118, 124, 132) if x % 8 else (96, 100, 108), 4)
blk("security_door", door)
blk("button_panel", lambda x, y: (40, 40, 44, 255) if x in (0, 15) or y in (0, 15) else jit((76, 78, 86), 4))
def flat(c, hi):
    return lambda x, y: hi + (255,) if (2 <= x <= 5 and 2 <= y <= 4) else c + (255,)
blk("btn_red", flat((205, 28, 28), (255, 140, 140)))
blk("btn_green", flat((40, 210, 70), (170, 255, 190)))
blk("btn_light_off", flat((62, 70, 112), (110, 120, 170)))
blk("btn_light_on", flat((255, 248, 180), (255, 255, 255)))
def hall(on):
    def f(x, y):
        if x in (0, 15) or y in (0, 15): return (44, 44, 50, 255)
        return (255, 248, 186, 255) if on else (84, 88, 104, 255)
    return f
blk("hall_light_off", hall(False)); blk("hall_light_on", hall(True))
def mon_front(x, y):
    if x in (0, 15) or y in (0, 15) or x == 1 or x == 14 or y == 1 or y == 14: return jit((196, 190, 172), 3)
    if (x, y) == (13, 13): return (230, 30, 30, 255)
    g = random.randint(8, 70)
    return (g // 3, g, g // 3, 255) if y % 2 else (g // 4, int(g * .8), g // 4, 255)
blk("monitor_front", mon_front)
blk("monitor_side", lambda x, y: jit((190, 186, 170), 5))
blk("desk_top", lambda x, y: jit((98, 102, 110), 4) if x % 5 else (80, 84, 92, 255))
def desk_side(x, y):
    if y in (0, 15) or x in (0, 15): return (60, 62, 70, 255)
    if y == 7: return (50, 52, 58, 255)
    if y in (3, 11) and 5 <= x <= 10: return (200, 200, 205, 255)
    return jit((84, 86, 96), 4)
blk("desk_side", desk_side)
def cam_body(x, y):
    if (x, y) == (3, 3): return (240, 30, 30, 255)
    return jit((44, 44, 52), 5) if x not in (0, 15) and y not in (0, 15) else (26, 26, 30, 255)
blk("camera_body", cam_body)
def cam_lens(x, y):
    d = math.hypot(x - 7.5, y - 7.5)
    return (20, 30, 60, 255) if d < 3 else (90, 120, 200, 255) if d < 4.5 else (30, 30, 36, 255)
blk("camera_lens", cam_lens)
def poster1(x, y):
    if x in (0, 15) or y in (0, 15): return (236, 190, 40, 255)
    if 6 <= y <= 9: return (245, 245, 245, 255) if (x + y) % 5 else (230, 60, 90, 255)
    if (x * 7 + y * 3) % 11 == 0: return random.choice([(255, 230, 60, 255), (80, 220, 240, 255), (255, 120, 190, 255)])
    return (92, 44, 136, 255)
blk("poster_celebrate", poster1)
def poster2(x, y):
    d = math.hypot(x - 7.5, y - 8)
    if (x, y) in ((6, 7), (9, 7), (5, 7), (10, 7)): return (0, 0, 0, 255)
    if (x, y) in ((7, 11), (8, 11)): return (0, 0, 0, 255)
    if d < 5: return (230, 180, 40, 255)
    if (x, y) in ((4, 3), (11, 3), (3, 4), (12, 4)): return (230, 180, 40, 255)
    return (26, 24, 28, 255)
blk("poster_its_me", poster2)

PLUSH = {"freddy": ((120, 76, 40), (170, 120, 70)), "bonnie": ((100, 90, 200), (150, 140, 240)),
         "chica": ((240, 200, 40), (252, 230, 110)), "foxy": ((160, 44, 30), (205, 90, 60))}
for k, (c1, c2) in PLUSH.items():
    im = Image.new("RGBA", (16, 16)); d = ImageDraw.Draw(im)
    d.rectangle([0, 0, 15, 15], fill=c1 + (255,))
    d.rectangle([0, 8, 7, 15], fill=c1 + (255,)); d.rectangle([3, 11, 4, 14], fill=c2 + (255,))
    d.rectangle([2, 5, 5, 7], fill=c2 + (255,))                       # hocico
    d.point([(1, 2), (2, 2), (5, 2), (6, 2)], fill=(255, 255, 255, 255)); d.point([(2, 3), (5, 3)], fill=(0, 0, 0, 255))
    d.point([(3, 5), (4, 5)], fill=(0, 0, 0, 255))
    if k == "chica": d.rectangle([2, 5, 5, 6], fill=(240, 140, 30, 255))
    if k == "foxy": d.rectangle([5, 1, 6, 3], fill=(0, 0, 0, 255))   # parche
    save(im, B("plush_" + k))
    icon = im.crop((0, 0, 8, 8)).resize((16, 16), Image.NEAREST)
    save(icon, os.path.join(A, "textures", "item", "plush_" + k + ".png"))

# ------------------------------------------------------------------ Modelos de bloque
def cube_all(n): return {"parent": "minecraft:block/cube_all", "textures": {"all": f"{NS}:block/{n}"}}
def el(f, t, tex="#t", faces=None):
    e = {"from": f, "to": t, "faces": {}}
    for s in ("north", "south", "east", "west", "up", "down"):
        e["faces"][s] = {"texture": tex, "cullface": None} if False else {"texture": tex}
    if faces:
        for s, v in faces.items(): e["faces"][s] = v
    return e
def custom(textures, elements):
    textures = dict(textures); textures.setdefault("particle", list(textures.values())[0])
    return {"parent": "minecraft:block/block", "textures": textures, "elements": elements}
def M(name, data): wj(os.path.join(A, "models", "block", name + ".json"), data)
def item_model(name, parent=None, layer=None):
    wj(os.path.join(A, "models", "item", name + ".json"),
       {"parent": parent} if parent else {"parent": "minecraft:item/generated", "textures": {"layer0": layer}})
def state_simple(name, model=None):
    wj(os.path.join(A, "blockstates", name + ".json"), {"variants": {"": {"model": f"{NS}:block/{model or name}"}}})
ROT = {"north": 0, "east": 90, "south": 180, "west": 270}
def state_facing(name, models=None):
    """models: función (props)->nombre de modelo; props es dict extra"""
    v = {}
    for f, r in ROT.items():
        v[f"facing={f}"] = {"model": f"{NS}:block/{name}"} | ({"y": r} if r else {})
    wj(os.path.join(A, "blockstates", name + ".json"), {"variants": v})
def state_facing_bool(name, prop, mfalse, mtrue):
    v = {}
    for f, r in ROT.items():
        for b, m in (("false", mfalse), ("true", mtrue)):
            v[f"facing={f},{prop}={b}"] = {"model": f"{NS}:block/{m}"} | ({"y": r} if r else {})
    wj(os.path.join(A, "blockstates", name + ".json"), {"variants": v})

for n in ["checker_tile", "office_floor", "pizzeria_wall", "pizzeria_wall_striped", "stage_floor", "stage_curtain",
          "pirate_cove_curtain", "dim_light", "vent_grate"]:
    M(n, cube_all(n)); state_simple(n); item_model(n, f"{NS}:block/{n}")
# mesa
M("party_table", custom({"t": f"{NS}:block/party_table"}, [el([0, 12, 0], [16, 16, 16]), el([6, 0, 6], [10, 12, 10])]))
state_simple("party_table"); item_model("party_table", f"{NS}:block/party_table")
# escritorio
M("office_desk", {"parent": "minecraft:block/cube_bottom_top", "textures": {
    "top": f"{NS}:block/desk_top", "bottom": f"{NS}:block/desk_top", "side": f"{NS}:block/desk_side"}})
state_simple("office_desk"); item_model("office_desk", f"{NS}:block/office_desk")
# puertas
for side in ("left", "right"):
    n = f"security_door_{side}"
    M(n + "_closed", custom({"t": f"{NS}:block/security_door"}, [el([0, 0, 6], [16, 16, 10])]))
    M(n + "_open", custom({"t": f"{NS}:block/security_door"}, [el([0, 0, 6], [1, 16, 10]), el([15, 0, 6], [16, 16, 10]), el([0, 15, 6], [16, 16, 10])]))
    state_facing_bool(n, "open", n + "_closed", n + "_open")
    item_model(n, layer=f"{NS}:block/security_door")
# botones
for fn, tf, tn in (("door", "btn_red", "btn_green"), ("light", "btn_light_off", "btn_light_on")):
    for side in ("left", "right"):
        n = f"{fn}_button_{side}"
        for st, tx in (("off", tf), ("on", tn)):
            M(f"{n}_{st}", custom({"p": f"{NS}:block/button_panel", "b": f"{NS}:block/{tx}"},
                                  [el([3, 3, 13], [13, 13, 16], "#p"), el([6, 6, 11], [10, 10, 13], "#b")]))
        state_facing_bool(n, "on", n + "_off", n + "_on")
        item_model(n, layer=f"{NS}:block/{tn if fn == 'light' else tf}")
# focos de pasillo
for side in ("left", "right"):
    n = f"hall_light_{side}"
    for st in ("off", "on"):
        M(f"{n}_{st}", custom({"t": f"{NS}:block/hall_light_{st}"}, [el([4, 4, 12], [12, 12, 16])]))
    state_facing_bool(n, "lit", n + "_off", n + "_on"); item_model(n, layer=f"{NS}:block/hall_light_on")
# monitor
M("security_monitor", custom({"f": f"{NS}:block/monitor_front", "s": f"{NS}:block/monitor_side"}, [
    el([2, 2, 3], [14, 13, 13], "#s", {"north": {"texture": "#f"}}), el([6, 0, 6], [10, 2, 10], "#s")]))
state_facing("security_monitor"); item_model("security_monitor", layer=f"{NS}:block/monitor_front")
# cámara
M("security_camera", custom({"b": f"{NS}:block/camera_body", "l": f"{NS}:block/camera_lens"}, [
    el([5, 5, 6], [11, 11, 14], "#b"), el([6, 6, 3], [10, 10, 6], "#b", {"north": {"texture": "#l"}}), el([7, 10, 13], [9, 14, 15], "#b"),
    el([5, 14, 12], [11, 16, 16], "#b")]))
state_facing("security_camera"); item_model("security_camera", layer=f"{NS}:block/camera_body")
# pósters
for n in ("poster_celebrate", "poster_its_me"):
    M(n, custom({"t": f"{NS}:block/{n}"}, [el([1, 1, 15], [15, 15, 16], "#t")]))
    state_facing(n); item_model(n, layer=f"{NS}:block/{n}")
# peluches
for k in PLUSH:
    n = f"plush_{k}"
    head_front = {"north": {"uv": [0, 0, 8, 8], "texture": "#t"}}
    plain_h = {s: {"uv": [8, 0, 16, 8], "texture": "#t"} for s in ("south", "east", "west", "up", "down")}
    plain_b = {s: {"uv": [8, 8, 16, 16], "texture": "#t"} for s in ("south", "east", "west", "up", "down")}
    ears = []
    if k in ("freddy", "bonnie"): ears = [el([3, 13, 6], [5, 15, 8], "#t", {s: {"uv": [8, 0, 10, 2], "texture": "#t"} for s in ("north", "south", "east", "west", "up", "down")}),
                                          el([11, 13, 6], [13, 15, 8], "#t", {s: {"uv": [8, 0, 10, 2], "texture": "#t"} for s in ("north", "south", "east", "west", "up", "down")})]
    M(n, custom({"t": f"{NS}:block/{n}"}, [
        {"from": [4, 0, 5], "to": [12, 6, 11], "faces": {"north": {"uv": [0, 8, 8, 14], "texture": "#t"}, **{s: {"uv": [8, 8, 16, 14], "texture": "#t"} for s in ("south", "east", "west", "up", "down")}}},
        {"from": [3, 6, 4], "to": [13, 13, 12], "faces": {**head_front, **plain_h}}] + ears))
    state_facing(n); item_model(n, layer=f"{NS}:item/{n}")

# ------------------------------------------------------------------ Items
I = lambda n: os.path.join(A, "textures", "item", n + ".png")
def icon(name, draw):
    im = Image.new("RGBA", (16, 16), (0, 0, 0, 0)); draw(ImageDraw.Draw(im)); save(im, I(name)); item_model(name, layer=f"{NS}:item/{name}")
def coin(d):
    d.ellipse([2, 2, 13, 13], fill=(230, 180, 30, 255), outline=(150, 100, 10, 255)); d.ellipse([4, 4, 11, 11], outline=(255, 225, 100, 255))
    d.rectangle([6, 5, 9, 6], fill=(150, 100, 10, 255)); d.rectangle([6, 5, 7, 10], fill=(150, 100, 10, 255)); d.rectangle([6, 7, 8, 7], fill=(150, 100, 10, 255))
icon("faz_coin", coin)
def paycheck(d):
    d.rectangle([1, 3, 14, 12], fill=(240, 240, 228, 255), outline=(120, 120, 110, 255))
    d.rectangle([3, 5, 9, 5], fill=(60, 60, 60, 255)); d.rectangle([3, 7, 12, 7], fill=(160, 160, 150, 255)); d.rectangle([9, 9, 12, 10], fill=(40, 150, 60, 255))
icon("paycheck", paycheck)
def flash(d):
    d.rectangle([5, 1, 10, 4], fill=(240, 220, 90, 255)); d.rectangle([6, 4, 9, 14], fill=(60, 60, 68, 255)); d.rectangle([7, 5, 8, 13], fill=(90, 90, 100, 255))
    d.point([(7, 0), (8, 0)], fill=(255, 255, 200, 255))
icon("flashlight", flash)
def pizza(d):
    d.polygon([(1, 3), (14, 3), (8, 15)], fill=(236, 170, 50, 255), outline=(150, 90, 20, 255)); d.polygon([(2, 4), (13, 4), (8, 13)], fill=(240, 200, 80, 255))
    for p in ((5, 6), (9, 6), (7, 9)): d.ellipse([p[0] - 1, p[1] - 1, p[0] + 1, p[1] + 1], fill=(190, 40, 40, 255))
icon("pizza_slice", pizza)
def cupcake(d):
    d.polygon([(3, 8), (12, 8), (11, 14), (4, 14)], fill=(150, 90, 200, 255)); d.ellipse([2, 3, 13, 9], fill=(250, 150, 190, 255), outline=(200, 90, 130, 255))
    d.ellipse([6, 1, 9, 4], fill=(220, 30, 40, 255)); d.point([(5, 6), (10, 5), (8, 7)], fill=(255, 255, 255, 255))
    d.point([(5, 6), (9, 6)], fill=(255, 255, 255, 255)); d.point([(6, 7), (7, 7)], fill=(0, 0, 0, 255))
icon("cupcake", cupcake)
def maskicon(d):
    d.ellipse([1, 0, 5, 4], fill=(120, 76, 40, 255)); d.ellipse([10, 0, 14, 4], fill=(120, 76, 40, 255))
    d.rectangle([2, 2, 13, 13], fill=(120, 76, 40, 255)); d.rectangle([5, 8, 10, 12], fill=(170, 120, 70, 255))
    d.rectangle([4, 5, 5, 6], fill=(255, 255, 255, 255)); d.rectangle([10, 5, 11, 6], fill=(255, 255, 255, 255)); d.point([(5, 6), (10, 6)], fill=(0, 0, 0, 255))
    d.rectangle([7, 9, 8, 9], fill=(0, 0, 0, 255))
icon("freddy_mask", maskicon)
for k in ("freddy", "bonnie", "chica", "foxy", "golden_freddy", "springtrap"):
    item_model(k + "_spawn_egg", "minecraft:item/template_spawn_egg")

# ------------------------------------------------------------------ Entidades (128x128)
PAL = {
    "freddy": dict(fur=(112, 70, 36), light=(164, 116, 68), bow=(18, 18, 22), hat=(20, 20, 24), eye=(150, 200, 255)),
    "bonnie": dict(fur=(96, 84, 196), light=(140, 130, 235), bow=(190, 30, 40), hat=None, eye=(255, 255, 255)),
    "chica": dict(fur=(238, 198, 36), light=(250, 226, 104), bow=(246, 246, 246), hat=None, eye=(255, 150, 255)),
    "foxy": dict(fur=(150, 38, 28), light=(196, 76, 54), bow=None, hat=None, eye=(255, 232, 60)),
    "golden_freddy": dict(fur=(224, 178, 40), light=(248, 218, 96), bow=(190, 140, 30), hat=(196, 148, 34), eye=(255, 255, 255)),
    "springtrap": dict(fur=(146, 148, 66), light=(172, 174, 96), bow=(90, 94, 40), hat=None, eye=(255, 40, 40)),
}
def faces(u, v, w, h, d):
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + 2 * d + w, v + d, w, h)}
def paint(im, box, color, n=7, per=None):
    px = im.load()
    for name, (x, y, w, h) in faces(*box).items():
        c = (per or {}).get(name, color)
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                px[xx, yy] = jit(c, n) + ()
def rect(im, x1, y1, x2, y2, c):
    ImageDraw.Draw(im).rectangle([x1, y1, x2, y2], fill=c + (255,))

HEAD, MUZ, EAR, HAT, BRIM, BODY, BOW, BIB, ARM, HOOK, LEG = (0, 0, 10, 9, 9), None, (64, 0), (0, 20, 8, 6, 8), (34, 20, 12, 1, 12), (0, 40, 10, 14, 6), (34, 40, 6, 2, 2), (34, 46, 8, 6, 1), (60, 40, 4, 13, 4), (80, 40, 2, 6, 2), (0, 64, 5, 12, 5)
def make_entity(k):
    p = PAL[k]; im = Image.new("RGBA", (128, 128), (0, 0, 0, 0)); eyes = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    fur, light = p["fur"], p["light"]
    paint(im, HEAD, fur); paint(im, BODY, fur); paint(im, ARM, fur); paint(im, LEG, fur)
    # barriga / pecho claro
    rect(im, 8, 63 - 0, 8, 63 - 0, fur) if False else None
    bf = faces(*BODY)["front"]; rect(im, bf[0] + 2, bf[1] + 4, bf[0] + 7, bf[1] + 11, light)
    # hocico / pico / orejas
    if k in ("freddy", "golden_freddy"):
        paint(im, (40, 0, 6, 4, 3), light); paint(im, (64, 0, 4, 4, 2), fur)
        mf = faces(40, 0, 6, 4, 3)["front"]; rect(im, mf[0] + 2, mf[1], mf[0] + 3, mf[1], (10, 10, 10)); rect(im, mf[0] + 1, mf[1] + 3, mf[0] + 4, mf[1] + 3, (30, 20, 16))
        paint(im, HAT, p["hat"], 3); paint(im, BRIM, p["hat"], 3); rect(im, 9, 29, 22, 29, (120, 30, 30)) if False else None
    elif k in ("bonnie", "springtrap"):
        paint(im, (40, 0, 6, 3, 3), light); paint(im, (64, 0, 3, 9, 2), fur)
        mf = faces(40, 0, 6, 3, 3)["front"]; rect(im, mf[0] + 2, mf[1], mf[0] + 3, mf[1], (10, 10, 10))
    elif k == "chica":
        paint(im, (40, 0, 6, 2, 3), (240, 140, 30)); paint(im, (64, 0, 2, 3, 1), light)
        paint(im, BIB, (246, 246, 246), 2); bb = faces(*BIB)["front"]
        for i in range(3): rect(im, bb[0] + 1 + i * 2, bb[1] + 2, bb[0] + 1 + i * 2, bb[1] + 3, (230, 70, 150))
    elif k == "foxy":
        paint(im, (40, 0, 4, 3, 6), light); paint(im, (64, 0, 3, 5, 2), fur); paint(im, HOOK, (200, 200, 210), 4)
        mf = faces(40, 0, 4, 3, 6)["front"]; rect(im, mf[0] + 1, mf[1], mf[0] + 2, mf[1], (10, 10, 10))
        for part in (LEG, ARM):   # extremidades al descubierto (endoesqueleto)
            fc = faces(*part)
            for nm in ("front", "back", "left", "right"):
                x, y, w, h = fc[nm]; rect(im, x, y + h // 2 + 1, x + w - 1, y + h - 1, (118, 120, 130))
    if p["bow"] and k != "chica": paint(im, BOW, p["bow"], 3)
    if k == "springtrap":
        for _ in range(70):   # podredumbre y parches
            x, y = random.randint(0, 100), random.randint(0, 80)
            if im.getpixel((x, y))[3]: im.putpixel((x, y), random.choice([(40, 40, 22, 255), (22, 22, 14, 255), (110, 112, 120, 255)]))
        for part in (BODY, ARM):
            fc = faces(*part)["front"]; rect(im, fc[0] + 1, fc[1] + 2, fc[0] + 3, fc[1] + 4, (100, 102, 112))
    # cara (frente de la cabeza: x 9..18, y 9..17)
    fx, fy = 9, 9
    for ex in (1, 6):
        rect(im, fx + ex, fy + 2, fx + ex + 2, fy + 4, (6, 6, 8)); rect(eyes, fx + ex, fy + 2, fx + ex + 2, fy + 4, p["eye"])
    if k in ("bonnie", "foxy", "springtrap"): rect(im, fx + 1, fy + 1, fx + 3, fy + 1, (20, 14, 30)); rect(im, fx + 6, fy + 1, fx + 8, fy + 1, (20, 14, 30))
    if k == "foxy":   # parche en el ojo derecho (visto desde el frente: izquierda de la imagen)
        rect(im, fx + 1, fy + 2, fx + 3, fy + 4, (12, 12, 14)); ImageDraw.Draw(eyes).rectangle([fx + 1, fy + 2, fx + 3, fy + 4], fill=(0, 0, 0, 0))
        ImageDraw.Draw(im).line([fx, fy, fx + 9, fy + 3], fill=(12, 12, 14, 255))
    if k == "golden_freddy":
        ImageDraw.Draw(eyes).rectangle([fx + 1, fy + 2, fx + 3, fy + 4], fill=(0, 0, 0, 0)); rect(eyes, fx + 2, fy + 3, fx + 2, fy + 3, (255, 255, 255))
        ImageDraw.Draw(eyes).rectangle([fx + 6, fy + 2, fx + 8, fy + 4], fill=(0, 0, 0, 0)); rect(eyes, fx + 7, fy + 3, fx + 7, fy + 3, (255, 255, 255))
    if k == "springtrap":   # dientes rotos
        for x in range(fx + 1, fx + 9, 2): im.putpixel((x, fy + 8), (230, 226, 190, 255))
    # sombra suave en cuello / dobleces
    save(im, os.path.join(A, "textures", "entity", k + ".png")); save(eyes, os.path.join(A, "textures", "entity", k + "_eyes.png"))
for k in PAL: make_entity(k)

# Armadura (máscara)
arm = Image.new("RGBA", (64, 32), (0, 0, 0, 0)); paint(arm, (0, 0, 8, 8, 8), (120, 76, 40))
hf = faces(0, 0, 8, 8, 8)["front"]
rect(arm, hf[0] + 1, hf[1] + 3, hf[0] + 2, hf[1] + 4, (255, 255, 255)); rect(arm, hf[0] + 5, hf[1] + 3, hf[0] + 6, hf[1] + 4, (255, 255, 255))
arm.putpixel((hf[0] + 2, hf[1] + 4), (0, 0, 0, 255)); arm.putpixel((hf[0] + 5, hf[1] + 4), (0, 0, 0, 255))
rect(arm, hf[0] + 2, hf[1] + 5, hf[0] + 5, hf[1] + 7, (170, 120, 70)); rect(arm, hf[0] + 3, hf[1] + 5, hf[0] + 4, hf[1] + 5, (0, 0, 0))
save(arm, os.path.join(ROOT, "assets", NS, "textures", "models", "armor", "fazbear_layer_1.png"))
save(Image.new("RGBA", (64, 32), (0, 0, 0, 0)), os.path.join(ROOT, "assets", NS, "textures", "models", "armor", "fazbear_layer_2.png"))

# ------------------------------------------------------------------ Jumpscares (64x64)
def scare(k):
    p = PAL[k]; im = Image.new("RGBA", (64, 64), (0, 0, 0, 255)); d = ImageDraw.Draw(im)
    fur, light = p["fur"], p["light"]
    if k in ("bonnie", "springtrap"):
        d.polygon([(14, 24), (18, 0), (26, 0), (28, 22)], fill=fur + (255,)); d.polygon([(36, 22), (38, 0), (46, 0), (50, 24)], fill=fur + (255,))
        if k == "springtrap": d.polygon([(36, 22), (38, 8), (44, 12), (50, 24)], fill=(22, 22, 14, 255))
    if k in ("freddy", "golden_freddy"):
        d.ellipse([6, 8, 20, 22], fill=fur + (255,)); d.ellipse([44, 8, 58, 22], fill=fur + (255,)); d.ellipse([9, 11, 17, 19], fill=light + (255,)); d.ellipse([47, 11, 55, 19], fill=light + (255,))
        d.rectangle([20, 0, 44, 12], fill=p["hat"] + (255,)); d.rectangle([14, 11, 50, 14], fill=p["hat"] + (255,))
    if k == "chica": d.polygon([(28, 6), (30, 14), (34, 14), (36, 6), (32, 12)], fill=fur + (255,))
    if k == "foxy": d.polygon([(10, 22), (12, 4), (24, 18)], fill=fur + (255,)); d.polygon([(54, 22), (52, 4), (40, 18)], fill=fur + (255,))
    d.ellipse([8, 12, 56, 62], fill=fur + (255,))
    # ojos
    for cx in (22, 42):
        d.ellipse([cx - 7, 24, cx + 7, 38], fill=(0, 0, 0, 255)); d.ellipse([cx - 5, 26, cx + 5, 36], fill=p["eye"] + (255,))
        d.ellipse([cx - 1, 29, cx + 1, 33], fill=(0, 0, 0, 255))
    if k in ("bonnie", "foxy", "springtrap"):
        d.polygon([(12, 20), (30, 26), (30, 22), (14, 16)], fill=(14, 10, 20, 255)); d.polygon([(52, 20), (34, 26), (34, 22), (50, 16)], fill=(14, 10, 20, 255))
    if k == "foxy": d.rectangle([14, 23, 30, 39], fill=(10, 10, 12, 255)); d.line([8, 14, 56, 44], fill=(10, 10, 12, 255), width=2)
    # hocico y boca abierta con dientes
    if k == "chica": d.polygon([(20, 40), (44, 40), (40, 46), (24, 46)], fill=(240, 140, 30, 255))
    else: d.ellipse([18, 38, 46, 54], fill=light + (255,))
    d.rectangle([20, 46, 44, 58], fill=(110, 8, 8, 255))
    for x in range(20, 44, 4): d.polygon([(x, 46), (x + 4, 46), (x + 2, 52)], fill=(240, 236, 220, 255)); d.polygon([(x, 58), (x + 4, 58), (x + 2, 53)], fill=(240, 236, 220, 255))
    d.rectangle([29, 40, 35, 42], fill=(10, 10, 10, 255))
    save(im, os.path.join(A, "textures", "gui", f"jumpscare_{k}.png"))
for k in PAL: scare(k)

# Icono del mod
ic = Image.new("RGBA", (128, 128), (20, 10, 40, 255)); sc = Image.open(os.path.join(A, "textures", "gui", "jumpscare_freddy.png")).resize((128, 128), Image.NEAREST); save(sc, os.path.join(A, "icon.png"))

# ------------------------------------------------------------------ Loot, tags, recetas
BLOCKS = ["checker_tile", "office_floor", "pizzeria_wall", "pizzeria_wall_striped", "stage_floor", "stage_curtain", "pirate_cove_curtain",
          "party_table", "dim_light", "vent_grate", "security_door_left", "security_door_right", "door_button_left", "door_button_right",
          "light_button_left", "light_button_right", "hall_light_left", "hall_light_right", "security_monitor", "office_desk", "security_camera",
          "poster_celebrate", "poster_its_me", "plush_freddy", "plush_bonnie", "plush_chica", "plush_foxy"]
for b in BLOCKS:
    wj(os.path.join(D, "loot_tables", "blocks", b + ".json"), {"type": "minecraft:block", "pools": [{"rolls": 1, "entries": [
        {"type": "minecraft:item", "name": f"{NS}:{b}"}], "conditions": [{"condition": "minecraft:survives_explosion"}]}]})
wj(os.path.join(ROOT, "data", "minecraft", "tags", "blocks", "mineable", "pickaxe.json"), {"replace": False, "values": [f"{NS}:{b}" for b in BLOCKS]})

def shaped(name, pattern, key, result, count=1):
    wj(os.path.join(D, "recipes", name + ".json"), {"type": "minecraft:crafting_shaped", "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()}, "result": {"item": result if ":" in result else f"{NS}:{result}", "count": count}})
def shapeless(name, ing, result, count=1):
    wj(os.path.join(D, "recipes", name + ".json"), {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": i} for i in ing],
        "result": {"item": f"{NS}:{result}", "count": count}})
shaped("faz_coin", ["NN", "NN"], {"N": "minecraft:gold_nugget"}, "faz_coin", 4)
shaped("freddy_mask", ["LLL", "LGL", "L L"], {"L": "minecraft:leather", "G": "minecraft:gold_ingot"}, "freddy_mask")
shaped("flashlight", ["D", "I", "R"], {"D": "minecraft:glowstone_dust", "I": "minecraft:iron_ingot", "R": "minecraft:redstone"}, "flashlight")
shapeless("pizza_slice", ["minecraft:bread", "minecraft:cooked_porkchop", "minecraft:tomato" if False else "minecraft:dried_kelp"], "pizza_slice", 2)
shapeless("cupcake", ["minecraft:sugar", "minecraft:egg", "minecraft:wheat"], "cupcake", 2)
shapeless("checker_tile", ["minecraft:black_concrete", "minecraft:white_concrete"], "checker_tile", 2)
shapeless("pizzeria_wall_striped", ["minecraft:purple_concrete", "minecraft:yellow_concrete"], "pizzeria_wall_striped", 2)
shapeless("pizzeria_wall", ["minecraft:purple_concrete", "minecraft:black_concrete"], "pizzeria_wall", 2)
shaped("security_monitor", ["III", "IGI", "IRI"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass_pane", "R": "minecraft:redstone"}, "security_monitor")
shaped("security_camera", ["III", "IGR", "  I"], {"I": "minecraft:iron_ingot", "G": "minecraft:glass_pane", "R": "minecraft:redstone"}, "security_camera")
shaped("office_desk", ["III", "I I", "I I"], {"I": "minecraft:iron_ingot"}, "office_desk")
shaped("security_door_left", ["II", "II", "II"], {"I": "minecraft:iron_block"}, "security_door_left", 3)
shapeless("security_door_right", [f"{NS}:security_door_left"], "security_door_right")
shapeless("security_door_left_back", [f"{NS}:security_door_right"], "security_door_left")
for fn, mat in (("door", "minecraft:redstone"), ("light", "minecraft:glowstone_dust")):
    shaped(f"{fn}_button_left", [" M ", "IBI"], {"M": mat, "I": "minecraft:iron_ingot", "B": "minecraft:stone_button"}, f"{fn}_button_left")
    shapeless(f"{fn}_button_right", [f"{NS}:{fn}_button_left"], f"{fn}_button_right")
shaped("hall_light_left", ["G", "I"], {"G": "minecraft:glowstone", "I": "minecraft:iron_ingot"}, "hall_light_left")
shapeless("hall_light_right", [f"{NS}:hall_light_left"], "hall_light_right")
shaped("dim_light", ["G", "I"], {"G": "minecraft:glowstone_dust", "I": "minecraft:iron_nugget"}, "dim_light", 2)
shaped("party_table", ["WWW", " S "], {"W": "minecraft:red_wool", "S": "minecraft:stick"}, "party_table")
shaped("stage_curtain", ["PP", "PP"], {"P": "minecraft:purple_wool"}, "stage_curtain", 2)
shaped("pirate_cove_curtain", ["RR", "RY"], {"R": "minecraft:red_wool", "Y": "minecraft:yellow_dye"}, "pirate_cove_curtain", 2)
shaped("stage_floor", ["PP", "PP"], {"P": "minecraft:dark_oak_planks"}, "stage_floor", 4)
shaped("office_floor", ["SS", "SS"], {"S": "minecraft:stone_bricks"}, "office_floor", 4)
shaped("vent_grate", ["I I", " I ", "I I"], {"I": "minecraft:iron_bars"}, "vent_grate", 2)
shaped("poster_celebrate", ["PP", "PY"], {"P": "minecraft:paper", "Y": "minecraft:yellow_dye"}, "poster_celebrate", 2)
shaped("poster_its_me", ["PP", "PB"], {"P": "minecraft:paper", "B": "minecraft:black_dye"}, "poster_its_me")
for k, wool in (("freddy", "brown_wool"), ("bonnie", "blue_wool"), ("chica", "yellow_wool"), ("foxy", "red_wool")):
    shaped(f"plush_{k}", ["WW", "WW"], {"W": f"minecraft:{wool}"}, f"plush_{k}")

# ------------------------------------------------------------------ Idiomas
ES = {"itemGroup.fnafnights.main": "FNaF: Nights at Fazbear's",
      "block.checker_tile": "Baldosa ajedrezada", "block.office_floor": "Suelo de oficina", "block.pizzeria_wall": "Pared de pizzería",
      "block.pizzeria_wall_striped": "Pared rayada de pizzería", "block.stage_floor": "Suelo de escenario", "block.stage_curtain": "Cortina del escenario",
      "block.pirate_cove_curtain": "Cortina de la Cala del Pirata", "block.party_table": "Mesa de fiesta", "block.dim_light": "Luz tenue de techo",
      "block.vent_grate": "Rejilla de ventilación", "block.security_door_left": "Puerta de seguridad (izquierda)",
      "block.security_door_right": "Puerta de seguridad (derecha)", "block.door_button_left": "Botón de puerta (izquierda)",
      "block.door_button_right": "Botón de puerta (derecha)", "block.light_button_left": "Botón de luz (izquierda)",
      "block.light_button_right": "Botón de luz (derecha)", "block.hall_light_left": "Foco de pasillo (izquierdo)",
      "block.hall_light_right": "Foco de pasillo (derecho)", "block.security_monitor": "Monitor de seguridad", "block.office_desk": "Escritorio del guardia",
      "block.security_camera": "Cámara de seguridad", "block.poster_celebrate": "Póster «¡Celebra!»", "block.poster_its_me": "Póster misterioso",
      "block.plush_freddy": "Peluche de Freddy", "block.plush_bonnie": "Peluche de Bonnie", "block.plush_chica": "Peluche de Chica", "block.plush_foxy": "Peluche de Foxy",
      "item.faz_coin": "Faz-Coin", "item.paycheck": "Cheque de Fazbear Entertainment", "item.freddy_mask": "Máscara de Freddy", "item.flashlight": "Linterna",
      "item.pizza_slice": "Porción de pizza Fazbear", "item.cupcake": "Cupcake de Chica",
      "entity.freddy": "Freddy Fazbear", "entity.bonnie": "Bonnie", "entity.chica": "Chica", "entity.foxy": "Foxy",
      "entity.golden_freddy": "Golden Freddy", "entity.springtrap": "Springtrap"}
EN = dict(ES)
EN.update({"block.checker_tile": "Checkered Tile", "block.office_floor": "Office Floor", "block.pizzeria_wall": "Pizzeria Wall", "block.pizzeria_wall_striped": "Striped Pizzeria Wall",
           "block.stage_floor": "Stage Floor", "block.stage_curtain": "Stage Curtain", "block.pirate_cove_curtain": "Pirate Cove Curtain", "block.party_table": "Party Table",
           "block.dim_light": "Dim Ceiling Light", "block.vent_grate": "Vent Grate", "block.security_door_left": "Security Door (Left)", "block.security_door_right": "Security Door (Right)",
           "block.door_button_left": "Door Button (Left)", "block.door_button_right": "Door Button (Right)", "block.light_button_left": "Light Button (Left)",
           "block.light_button_right": "Light Button (Right)", "block.hall_light_left": "Hall Light (Left)", "block.hall_light_right": "Hall Light (Right)",
           "block.security_monitor": "Security Monitor", "block.office_desk": "Night Guard Desk", "block.security_camera": "Security Camera",
           "block.poster_celebrate": "“Celebrate!” Poster", "block.poster_its_me": "Mysterious Poster", "block.plush_freddy": "Freddy Plush", "block.plush_bonnie": "Bonnie Plush",
           "block.plush_chica": "Chica Plush", "block.plush_foxy": "Foxy Plush", "item.paycheck": "Fazbear Entertainment Paycheck", "item.freddy_mask": "Freddy Mask",
           "item.flashlight": "Flashlight", "item.pizza_slice": "Fazbear Pizza Slice", "item.cupcake": "Chica's Cupcake"})
for k in PAL:
    for pre in ("entity", "item"):
        pass
    ES[f"item.{k}_spawn_egg"] = f"Huevo de {ES['entity.' + k]}"; EN[f"item.{k}_spawn_egg"] = f"{EN['entity.' + k]} Spawn Egg"
def lang(d):
    out = {}
    for key, v in d.items():
        if key.startswith("itemGroup"): out[key] = v
        else:
            t, n = key.split(".", 1); out[f"{t}.{NS}.{n}"] = v
    return out
wj(os.path.join(A, "lang", "es_es.json"), lang(ES)); wj(os.path.join(A, "lang", "es_mx.json"), lang(ES)); wj(os.path.join(A, "lang", "es_cl.json"), lang(ES))
wj(os.path.join(A, "lang", "en_us.json"), lang(EN))
print("Assets generados.")
