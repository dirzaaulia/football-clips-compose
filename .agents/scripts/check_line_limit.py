import sys, json, os

payload = json.load(sys.stdin)
args = payload.get("toolCall", {}).get("args", {})
target = args.get("TargetFile") or args.get("absolutePath") or ""
content = args.get("CodeContent") or args.get("text") or ""

# Normalize path
normalized_target = target.replace("\\", "/")

# Check Kotlin, TypeScript, and TSX files, ignoring build/dependency directories
is_watched_file = any(normalized_target.endswith(ext) for ext in [".kt", ".ts", ".tsx"])
is_ignored = any(ignored in normalized_target for ignored in ["node_modules/", ".next/", "build/", ".gradle/", "generated/"])

if is_watched_file and not is_ignored:
    line_count = len(content.splitlines()) if content else 0

    if not content and os.path.exists(target):
        try:
            with open(target, "r", encoding="utf-8") as f:
                line_count = len(f.readlines())
        except Exception:
            pass

    if line_count > 250:
        ext = os.path.splitext(target)[1]
        decompose_hint = "*Components.kt, *State.kt" if ext == ".kt" else "*Components.tsx, *SubComponent.tsx"
        print(json.dumps({
            "decision": "deny",
            "reason": f"BLOCKED BY LINE-GUARD: {os.path.basename(target)} has {line_count} lines (Max: 250). You MUST decompose this into multiple files ({decompose_hint}) before saving."
        }))
        sys.exit(0)

print(json.dumps({"decision": "allow"}))
