#!/bin/bash
# memory.sh — Inject live git context into Claude Code at session start
# Run this before starting a Claude Code session: bash memory.sh

echo "=== CLAUDE CODE SESSION CONTEXT ==="
echo ""

# 1. Show current git branch
echo "Current branch:"
git branch --show-current 2>/dev/null || echo "(not a git repo)"
echo ""

# 2. Show last 5 commits with short messages
echo "Recent commits (last 5):"
git log --oneline -5 2>/dev/null || echo "(no commits)"
echo ""

# 3. Show files changed since last commit
echo "Files modified (not yet committed):"
git status --short 2>/dev/null || echo "(not a git repo)"
echo ""

# 4. Show files changed in the last 24 hours
echo "Files changed in last 24 hours:"
git diff --name-only HEAD@{1.day.ago} HEAD 2>/dev/null || echo "No changes or not enough history"
echo ""

# 5. Check for recent error logs
echo "Recent errors (last 10 lines of error.log, if it exists):"
if [ -f "error.log" ]; then
  tail -10 error.log
else
  echo "No error.log found"
fi
echo ""

echo "=== END OF SESSION CONTEXT ==="
echo "Paste the above output into Claude Code when starting your session."
