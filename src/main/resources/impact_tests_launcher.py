import json
import sys
import os
import subprocess


def main():
    if len(sys.argv) < 2:
        print("Usage: python impact_tests_launcher.py <result.json>")
        sys.exit(1)
    result_path = sys.argv[1]
    if not os.path.exists(result_path):
        print(f"File not found: {result_path}")
        sys.exit(2)

    with open(result_path, "r", encoding="utf-8") as f:
        result = json.load(f)

    tests_to_run = []
    if "testsToRun" in result:
        for tasks in result["testsToRun"].values():
            tests_to_run.extend(tasks)

    if not tests_to_run:
        print("No tests to run.")
        sys.exit(0)

    gradlew = "gradlew.bat" if os.name == "nt" else "./gradlew"
    cmd = [gradlew] + tests_to_run

    print("Running:", " ".join(cmd))
    # Важно: пробрасываем код возврата gradle
    completed = subprocess.run(cmd)
    sys.exit(completed.returncode)


if __name__ == "__main__":
    main()
