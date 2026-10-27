import argparse
from pathlib import Path
import subprocess


def git(root, *arguments, data=None):
    return subprocess.run(
        ["git", "-C", str(root), *arguments],
        input=data,
        stdout=subprocess.PIPE,
        check=True,
    ).stdout


def normalize(data):
    return data.replace(b"\r\n", b"\n").replace(b"\r", b"\n")


def is_binary(data):
    sample = data[:8000]
    if b"\0" in sample:
        return True
    controls = sum(byte < 32 and byte not in b"\b\t\n\f\r\x1b" or byte == 127 for byte in sample)
    return controls > (len(sample) - controls) // 128


def read_content(stream, size, auto):
    sample = stream.read(min(size, 8000))
    remaining = size - len(sample)
    if auto and is_binary(sample):
        while remaining:
            chunk = stream.read(min(remaining, 65536))
            if not chunk:
                raise RuntimeError("Unexpected end of file")
            remaining -= len(chunk)
        return None
    content = sample + stream.read(remaining)
    if len(content) != size:
        raise RuntimeError("Unexpected end of file")
    return content


def iter_contents(root, entries, policies, from_index):
    text_entries = [
        (path, object_id) for path, object_id in entries
        if policies[path][b"text"] != b"unset"
    ]
    if not from_index:
        for path, _ in text_entries:
            file_path = root / path.decode("utf-8", errors="surrogateescape")
            with file_path.open("rb") as stream:
                content = read_content(stream, file_path.stat().st_size,
                                       policies[path][b"text"] in (b"auto", b"unspecified"))
            if content is not None:
                yield path, content
        return

    with subprocess.Popen(
        ["git", "-C", str(root), "cat-file", "--batch"],
        stdin=subprocess.PIPE,
        stdout=subprocess.PIPE,
    ) as batch:
        try:
            for path, object_id in text_entries:
                batch.stdin.write(object_id + b"\n")
                batch.stdin.flush()
                header = batch.stdout.readline().split()
                if len(header) != 3 or header[1] != b"blob":
                    raise RuntimeError("Expected a tracked file blob")
                content = read_content(batch.stdout, int(header[2]),
                                       policies[path][b"text"] in (b"auto", b"unspecified"))
                if batch.stdout.read(1) != b"\n":
                    raise RuntimeError("Invalid Git blob separator")
                if content is not None:
                    yield path, content
        finally:
            batch.stdin.close()
            batch.stdout.close()
        if batch.wait() != 0:
            raise RuntimeError("Git blob reader failed")


def normalize_repository(root, check=False, from_index=False):
    entries = []
    for entry in git(root, "ls-files", "--stage", "-z").split(b"\0"):
        if not entry:
            continue
        metadata, path = entry.split(b"\t", 1)
        mode, object_id, stage = metadata.split()
        if stage != b"0":
            raise RuntimeError("Resolve merge conflicts before normalizing line endings")
        if mode in (b"100644", b"100755"):
            entries.append((path, object_id))

    if not entries:
        print("0 file(s) need normalization" if check else "0 file(s) normalized")
        return 0

    paths = b"\0".join(path for path, _ in entries) + b"\0"
    attributes = git(root, "check-attr", "-z", "--stdin", "text", "eol", data=paths).split(b"\0")
    policies = {}
    for offset in range(0, len(attributes) - 1, 3):
        path, attribute, value = attributes[offset:offset + 3]
        policies.setdefault(path, {})[attribute] = value

    changed = 0
    for path, content in iter_contents(root, entries, policies, check or from_index):
        policy = policies[path]
        file_path = root / path.decode("utf-8", errors="surrogateescape")
        normalized = normalize(content)
        if not check and policy[b"eol"] == b"crlf":
            normalized = normalized.replace(b"\n", b"\r\n")
        if content != normalized:
            changed += 1
            print(file_path.relative_to(root).as_posix())
            if not check:
                file_path.write_bytes(normalized)
    print(f"{changed} file(s) {'need normalization' if check else 'normalized'}")
    return changed


def main():
    parser = argparse.ArgumentParser(description="Normalize tracked text line endings using Git attributes")
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--check", action="store_true", help="Check staged Git blobs for non-LF endings")
    parser.add_argument("--from-index", action="store_true", help="Normalize staged blobs instead of working files")
    arguments = parser.parse_args()
    changed = normalize_repository(arguments.root.resolve(), arguments.check, arguments.from_index)
    return 1 if arguments.check and changed else 0


if __name__ == "__main__":
    raise SystemExit(main())
