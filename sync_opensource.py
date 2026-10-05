"""
把 01-源码项目（唯一真相源）同步到 04-开源源码/Block_ID（公开发布副本）。

用法：
    python sync_opensource.py           # 同步 + 本地 git commit
    python sync_opensource.py --dry    # 只预览将变更的文件，不写入
"""
import os
import sys
import shutil
import filecmp
import subprocess

SRC = r"D:\Mods\01-源码项目"
DST = r"D:\Mods\04-开源源码\Block_ID"
VERS = ["Forge-1.20.1", "Forge-1.19.2", "Fabric-1.20.1", "Fabric-1.19.2"]

IGNORE_DIRS = {".gradle", "build", "run", ".idea", "out", "classes", "bin",
               ".settings", ".vscode", ".git", ".github"}
# 根级 .github 的 CI 属于主线私有配置，公开副本不放（避免 workflow scope 问题）

ROOT_FILES = ["README.md", "LICENSE", ".gitignore", ".gitattributes"]


def collect(base, ignore_github=False):
    out = {}
    for v in VERS:
        vdir = os.path.join(base, v)
        if not os.path.isdir(vdir):
            continue
        for root, dirs, fs in os.walk(vdir):
            dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
            for f in fs:
                p = os.path.join(root, f)
                rel = os.path.relpath(p, base).replace("\\", "/")
                out[rel] = p
    for f in ROOT_FILES:
        p = os.path.join(base, f)
        if os.path.exists(p):
            out[f] = p
    return out


def main():
    dry = "--dry" in sys.argv
    src_files = collect(SRC)

    # 目标现状
    dst_files = {}
    for root, dirs, fs in os.walk(DST):
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        for f in fs:
            p = os.path.join(root, f)
            rel = os.path.relpath(p, DST).replace("\\", "/")
            dst_files[rel] = p

    add, mod, same = [], [], 0
    for rel, sp in src_files.items():
        dp = os.path.join(DST, rel.replace("/", os.sep))
        if rel not in dst_files:
            add.append(rel)
        elif not filecmp.cmp(sp, dp, shallow=False):
            mod.append(rel)
        else:
            same += 1
    dele = [rel for rel in dst_files if rel not in src_files]

    print(f"新增 {len(add)} / 修改 {len(mod)} / 相同 {same} / 删除 {len(dele)}")
    for r in add[:8]:
        print(f"  + {r}")
    for r in mod[:8]:
        print(f"  ~ {r}")
    for r in dele[:8]:
        print(f"  - {r}")
    if len(add) > 8 or len(mod) > 8 or len(dele) > 8:
        print("  ...(仅显示前8)")

    if dry:
        print("\n[dry-run] 未写入")
        return

    for rel, sp in src_files.items():
        dp = os.path.join(DST, rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dp), exist_ok=True)
        shutil.copy2(sp, dp)
    for rel in dele:
        # 保护：主线本地不存在的根级文件（远端独有）不允许被删除
        if "/" not in rel and rel in ROOT_FILES:
            print(f"  [保护] 跳过删除根级文件 {rel}")
            continue
        os.remove(os.path.join(DST, rel.replace("/", os.sep)))
    print(f"\n已同步 {len(src_files)} 个文件到公开副本")

    # 公开副本本地 git 提交（只做本地操作，不推送）
    try:
        def g(*a):
            return subprocess.run(["git", "-C", DST] + list(a),
                                  capture_output=True, text=True, encoding="utf-8")
        g("add", "-A")
        st = g("status", "--porcelain")
        if st.stdout.strip():
            c = g("commit", "-m",
                  "sync from 01-源码项目: 统一 mojmap 映射, v2.1.2.1-Beta")
            print("公开副本已本地提交:", (c.stdout or c.stderr).strip().splitlines()[-1][:80])
        else:
            print("公开副本无变更，跳过提交")
    except FileNotFoundError:
        print("!! 本机 git 不可用，公开副本已同步文件但未提交")


if __name__ == "__main__":
    main()
