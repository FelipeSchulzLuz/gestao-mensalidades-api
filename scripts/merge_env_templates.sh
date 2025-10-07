#!/usr/bin/env bash
set -euo pipefail

# scripts/merge_env_templates.sh
# Merge .env.docker.example into .env and append placeholders for keys present in .env.example but
# missing in .env. Safe defaults: dry-run mode, report, optional --apply to write file.
# NOTE: This now uses .env.docker.example (the safe template) as source.

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

DRY_RUN=true
OUTPUT_FILE=".env"
SOURCE_FILE=".env.docker.example"
EXAMPLE_FILE=".env.example"

usage() {
  cat <<EOF
Usage: merge_env_templates.sh [--apply] [--source FILE] [--example FILE] [--out FILE]

By default runs in dry-run and prints a report. Use --apply to actually write/modify ${OUTPUT_FILE}.

Options:
  --apply           Write changes to ${OUTPUT_FILE}
  --source FILE     Source env template (default: ${SOURCE_FILE})
  --example FILE    Example env with all keys (default: ${EXAMPLE_FILE})
  --out FILE        Output env file (default: ${OUTPUT_FILE})
  --help            Show this help
EOF
}

while [[ ${#} -gt 0 ]]; do
  case "$1" in
    --apply) DRY_RUN=false; shift ;;
    --source) SOURCE_FILE="$2"; shift 2 ;;
    --example) EXAMPLE_FILE="$2"; shift 2 ;;
    --out) OUTPUT_FILE="$2"; shift 2 ;;
    --help) usage; exit 0 ;;
    *) echo "Unknown arg: $1" >&2; usage; exit 1 ;;
  esac
done

if [[ ! -f "$SOURCE_FILE" ]]; then
  echo "Source file $SOURCE_FILE not found. Aborting." >&2
  exit 1
fi
if [[ ! -f "$EXAMPLE_FILE" ]]; then
  echo "Example file $EXAMPLE_FILE not found. Aborting." >&2
  exit 1
fi

# Load keys from files
extract_keys() {
  local file="$1"
  # Keep only lines like KEY=... and strip comments
  grep -E '^[A-Z0-9_]+=.*' "$file" | sed -E 's/=.*$//' | sort -u
}

source_keys=$(extract_keys "$SOURCE_FILE")
example_keys=$(extract_keys "$EXAMPLE_FILE")

# Load current .env if present (we'll preserve these values when merging)
env_keys=""
declare -A env_kv=()
if [[ -f "$OUTPUT_FILE" ]]; then
  env_keys=$(extract_keys "$OUTPUT_FILE")
  # populate env_kv map
  while IFS= read -r line; do
    k=$(printf "%s" "$line" | sed -E 's/=.*$//')
    v=$(printf "%s" "$line" | sed -E 's/^[^=]*=//')
    env_kv["$k"]="$v"
  done < <(grep -E '^[A-Z0-9_]+=.*' "$OUTPUT_FILE")
fi

# Compute missing keys: keys in example but not in env (or source)
missing_keys=()
while IFS= read -r key; do
  if [[ -z "$key" ]]; then continue; fi
  if ! grep -q -E "^${key}$" <(printf "%s\n" $env_keys); then
    if ! grep -q -E "^${key}$" <(printf "%s\n" $source_keys); then
      missing_keys+=("$key")
    fi
  fi
done < <(printf "%s\n" $example_keys)

# Detect keys present in source but with different canonical names in example (simple heuristic)
# e.g., STRIPE_API_KEY vs STRIPE_SECRET_KEY
declare -A map_ex_to_src=()
aliases=()
while IFS= read -r sk; do
  if [[ -z "$sk" ]]; then continue; fi
  # try replace API_KEY <-> SECRET_KEY, PROFILE <-> PROFILE(S)
  candidate1=$(echo "$sk" | sed -E 's/API_KEY/SECRET_KEY/')
  candidate2=$(echo "$sk" | sed -E 's/SECRET_KEY/API_KEY/')
  candidate3=$(echo "$sk" | sed -E 's/PROFILES?_?ACTIVE/PROFILE/')
  for cand in "$candidate1" "$candidate2" "$candidate3"; do
    if [[ -n "$cand" ]] && grep -q -E "^${cand}$" <(printf "%s\n" $example_keys) && ! grep -q -E "^${cand}$" <(printf "%s\n" $source_keys); then
      aliases+=("${sk} -> ${cand}")
      map_ex_to_src["$cand"]="$sk"
    fi
  done
done < <(printf "%s\n" $source_keys)

# Print report
cat <<EOF
Merge env templates report
=========================
Source file: $SOURCE_FILE
Example file: $EXAMPLE_FILE
Output file: $OUTPUT_FILE

Keys in source ($SOURCE_FILE):
$(printf "%s\n" $source_keys)

Keys in example ($EXAMPLE_FILE):
$(printf "%s\n" $example_keys)

Keys currently in $OUTPUT_FILE:
$(printf "%s\n" $env_keys)

Missing keys to add (from example, not present in source or output):
$(printf "%s\n" "${missing_keys[@]}")

Heuristic aliases detected (source_key -> example_key):
$(printf "%s\n" "${aliases[@]}")
EOF

if [[ "$DRY_RUN" == true ]]; then
  echo
  echo "Dry-run mode: no files modified. Rerun with --apply to write changes to $OUTPUT_FILE."
  exit 0
fi

# Apply changes: start from source (copy). Then append missing keys.
## Merge behavior: preserve existing .env values when present.
# Start from source to keep sensible dev values, but we'll build the merged content into a temp file.
cp /dev/null "$OUTPUT_FILE".tmp

# 1) Add/keep keys from the existing .env (highest priority)
if [[ -f "$OUTPUT_FILE" ]]; then
  # copy existing file lines as-is
  cat "$OUTPUT_FILE" >> "$OUTPUT_FILE".tmp
fi

# 2) For keys in source that are not yet in the tmp, append them
while IFS= read -r sk; do
  if [[ -z "$sk" ]]; then continue; fi
  if ! grep -q -E "^${sk}=" "$OUTPUT_FILE".tmp 2>/dev/null; then
    # get value from source
    val=$(grep -m1 -E "^${sk}=" "$SOURCE_FILE" | sed -E 's/^[^=]*=//') || val=""
    printf '%s=%s\n' "$sk" "$val" >> "$OUTPUT_FILE".tmp
  fi
done < <(printf "%s\n" $source_keys)

# 3) For missing example keys, try mapped source values, else placeholders; only add if not present
for k in "${missing_keys[@]}"; do
  if grep -q -E "^${k}=" "$OUTPUT_FILE".tmp 2>/dev/null; then
    continue
  fi
  if [[ -n "${map_ex_to_src[$k]:-}" ]]; then
    src_key="${map_ex_to_src[$k]}"
    src_val=$(grep -m1 -E "^${src_key}=" "$SOURCE_FILE" | sed -E 's/^[^=]*=//') || src_val=""
    printf '%s=%s # mapped from %s\n' "$k" "$src_val" "$src_key" >> "$OUTPUT_FILE".tmp
  else
    echo "${k}=" >> "$OUTPUT_FILE".tmp
  fi
done

# 4) Ensure keys from example that may not have been added yet are present as placeholders
while IFS= read -r ek; do
  if [[ -z "$ek" ]]; then continue; fi
  if ! grep -q -E "^${ek}=" "$OUTPUT_FILE".tmp 2>/dev/null; then
    echo "${ek}=" >> "$OUTPUT_FILE".tmp
  fi
done < <(printf "%s\n" $example_keys)

mv "$OUTPUT_FILE".tmp "$OUTPUT_FILE"

echo "Wrote merged $OUTPUT_FILE. Existing values preserved; mapped values and placeholders added. Review and fill secrets as needed."
# Mark todo completed
