#!/usr/bin/env bash
set -euo pipefail

RESULTS_DIR="${1:-target/allure-results}"
OUTPUT_DIR="${2:-${RESULTS_DIR}/exported-png}"

if [[ ! -d "${RESULTS_DIR}" ]]; then
  echo "Error: results directory not found: ${RESULTS_DIR}" >&2
  echo "Usage: $0 [allure-results-dir] [output-dir]" >&2
  exit 1
fi

mkdir -p "${OUTPUT_DIR}"

count=0
while IFS= read -r -d '' attachment; do
  base_name="$(basename "${attachment}")"
  cp "${attachment}" "${OUTPUT_DIR}/${base_name}.png"
  count=$((count + 1))
done < <(find "${RESULTS_DIR}" -maxdepth 1 -type f -name '*-attachment' -print0)

echo "Done. Exported ${count} screenshots to: ${OUTPUT_DIR}"
