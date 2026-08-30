#!/usr/bin/env python3

import sys


def is_ident_char(ch):
    return ch.isalnum() or ch == "_"


def occurrences(text, symbol):
    occs = []
    pos = 0

    while True:
        idx = text.find(symbol, pos)
        if idx == -1:
            break

        before_ok = (idx == 0) or (not is_ident_char(text[idx - 1]))
        end = idx + len(symbol)
        after_ok = (end >= len(text)) or (not is_ident_char(text[end]))

        if before_ok and after_ok:
            line_start = text.rfind("\n", 0, idx) + 1
            prefix = text[line_start:idx]

            # Very simple comment avoidance.
            if "//" not in prefix:
                occs.append(idx)

        pos = idx + 1

    return occs


def find_body_start(text, start):
    """
    Scan forward from 'start' until we find '{' or ';'.
    If we find ';', this is probably only a prototype.
    """
    in_block = False
    in_line = False
    in_str = False
    in_char = False

    i = start
    while i < len(text):
        c = text[i]

        if in_line:
            if c == "\n":
                in_line = False

        elif in_block:
            if c == "*" and i + 1 < len(text) and text[i + 1] == "/":
                in_block = False
                i += 1

        elif in_str:
            if c == "\\":
                i += 1
            elif c == '"':
                in_str = False

        elif in_char:
            if c == "\\":
                i += 1
            elif c == "'":
                in_char = False

        else:
            if c == "/" and i + 1 < len(text):
                if text[i + 1] == "/":
                    in_line = True
                    i += 1
                elif text[i + 1] == "*":
                    in_block = True
                    i += 1

            elif c == '"':
                in_str = True

            elif c == "'":
                in_char = True

            elif c == "{":
                return i

            elif c == ";":
                return None

        i += 1

    return None


def find_matching_brace(text, brace_pos):
    depth = 0

    in_block = False
    in_line = False
    in_str = False
    in_char = False

    i = brace_pos
    while i < len(text):
        c = text[i]

        if in_line:
            if c == "\n":
                in_line = False

        elif in_block:
            if c == "*" and i + 1 < len(text) and text[i + 1] == "/":
                in_block = False
                i += 1

        elif in_str:
            if c == "\\":
                i += 1
            elif c == '"':
                in_str = False

        elif in_char:
            if c == "\\":
                i += 1
            elif c == "'":
                in_char = False

        else:
            if c == "/" and i + 1 < len(text):
                if text[i + 1] == "/":
                    in_line = True
                    i += 1
                elif text[i + 1] == "*":
                    in_block = True
                    i += 1

            elif c == '"':
                in_str = True

            elif c == "'":
                in_char = True

            elif c == "{":
                depth += 1

            elif c == "}":
                depth -= 1
                if depth == 0:
                    return i

        i += 1

    return None


def extract_function(text, symbol):
    lines = text.splitlines(True)

    offsets = [0]
    for line in lines:
        offsets.append(offsets[-1] + len(line))

    for occ in occurrences(text, symbol):
        body_start = find_body_start(text, occ + len(symbol))
        if body_start is None:
            continue

        body_end = find_matching_brace(text, body_start)
        if body_end is None:
            continue

        occ_line = text.count("\n", 0, occ)

        # Try to include the return type / JNIEXPORT line(s) before the symbol.
        #
        # We walk backwards a limited number of lines until we hit something
        # that looks like a separator: blank line, preprocessor line, ';' or '}'.
        start_line = 0
        for i in range(occ_line - 1, max(-1, occ_line - 201), -1):
            if i < 0:
                break

            s = lines[i].strip()

            if s == "" or s.startswith("#") or s.endswith("}") or s.endswith(";"):
                start_line = i + 1
                break

        # Skip leading blank lines.
        while start_line < occ_line and lines[start_line].strip() == "":
            start_line += 1

        end_line = text.count("\n", 0, body_end)

        return text[offsets[start_line]:offsets[end_line + 1]]

    return None


def main():
    if len(sys.argv) != 3:
        sys.stderr.write("usage: extract_function.py <file> <symbol>\n")
        return 2

    filename = sys.argv[1]
    symbol = sys.argv[2]

    try:
        with open(filename, "r", encoding="utf-8", errors="replace") as f:
            text = f.read()
    except OSError as e:
        sys.stderr.write(f"error: cannot read {filename}: {e}\n")
        return 1

    func = extract_function(text, symbol)

    if func is None:
        sys.stderr.write(
            f"error: could not find definition for {symbol} in {filename}\n"
        )
        return 1

    sys.stdout.write(f"/* Extracted from {filename}: {symbol} */\n")
    sys.stdout.write(func)

    if not func.endswith("\n"):
        sys.stdout.write("\n")

    return 0


if __name__ == "__main__":
    sys.exit(main())
