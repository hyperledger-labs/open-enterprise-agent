"""Exercise publication routing without network access or real credentials."""

import json
import os
from pathlib import Path
import subprocess
import tempfile
import textwrap
import unittest


GENERATOR = Path(__file__).resolve().parent
WORKFLOW = GENERATOR.parents[2] / ".github/workflows/release-clients.yml"


class PublicationTests(unittest.TestCase):
  def setUp(self):
    self.temp = tempfile.TemporaryDirectory()
    self.addCleanup(self.temp.cleanup)
    self.root = Path(self.temp.name).resolve()
    for name in ("bin", "generator", "kotlin", "typescript"):
      (self.root / name).mkdir()
    self.log = self.root / "calls.jsonl"
    for name in ("npm", "yarn", "gradle"):
      command = self.root / "bin" / name
      command.write_text(
        "#!/usr/bin/env python3\n"
        "import json, os, sys\n"
        "with open(os.environ['CALL_LOG'], 'a') as log:\n"
        "  log.write(json.dumps([os.path.basename(sys.argv[0]), os.getcwd(), sys.argv[1:]]) + '\\n')\n"
      )
      command.chmod(0o755)

  def publish(self, version, kotlin="true", ts="true"):
    env = dict(os.environ, PATH=f"{self.root / 'bin'}:{os.environ['PATH']}",
               CALL_LOG=str(self.log), VERSION_TAG=version,
               PUBLISH_KOTLIN=kotlin, PUBLISH_TS=ts)
    result = subprocess.run(["bash", str(GENERATOR / "publish-clients.sh")],
                            cwd=self.root / "generator", env=env,
                            capture_output=True, text=True, check=False)
    calls = [json.loads(line) for line in self.log.read_text().splitlines()] if self.log.exists() else []
    return result, calls

  def test_release_and_snapshot_publish_the_client_package(self):
    for version, tag, maven_version in (("2.2.1", "latest", "2.2.1"),
                                        ("2.2.1-abc1234", "snapshot", "2.2.1-abc1234-SNAPSHOT")):
      with self.subTest(version=version):
        self.log.unlink(missing_ok=True)
        result, calls = self.publish(version)
        self.assertEqual(result.returncode, 0, result.stderr)
        npm = [call for call in calls if call[0] == "npm"]
        self.assertEqual(len(npm), 4)
        self.assertTrue(all(call[1] == str(self.root / "typescript") for call in npm))
        self.assertEqual(npm[1][2], ["run", "build"])
        self.assertEqual(npm[2][2], ["version", version, "--no-git-tag-version"])
        self.assertEqual(npm[3][2], ["publish", "--ignore-scripts", "--provenance", "--access", "public", "--tag", tag])
        gradle = [call for call in calls if call[0] == "gradle"]
        self.assertEqual(len(gradle), 1)
        self.assertEqual(gradle[0][2], ["-p", "../kotlin", f"-Pversion={maven_version}", "publishToMavenCentral"])

  def test_one_client_can_be_retried_without_republishing_the_other(self):
    for kotlin, ts, excluded in (("false", "true", "gradle"), ("true", "false", "npm")):
      with self.subTest(kotlin=kotlin, ts=ts):
        self.log.unlink(missing_ok=True)
        result, calls = self.publish("2.2.1", kotlin, ts)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertFalse(any(call[0] == excluded for call in calls))

  def test_invalid_input_fails_before_any_tool_is_called(self):
    for version, kotlin, ts in (("bad/version", "true", "true"),
                                ("", "true", "true"),
                                ("2.2.1", "yes", "true"),
                                ("2.2.1", "false", "false")):
      with self.subTest(version=version, kotlin=kotlin, ts=ts):
        result, calls = self.publish(version, kotlin, ts)
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(calls, [])

  def validate(self, **overrides):
    # Execute the workflow's actual shell validation, including the manual-build case.
    block = WORKFLOW.read_text().split("      - name: Validate publication inputs\n", 1)[1]
    shell = textwrap.dedent(block.split("        run: |\n", 1)[1].split("\n      - name:", 1)[0])
    env = dict(os.environ, REVISION="2.2.1-abc1234", RELEASE="false",
               CHECK_SUM="1234abcd", SOURCE_RUN_ID="12345", GITHUB_EVENT_NAME="workflow_dispatch")
    env.update(overrides)
    return subprocess.run(["bash", "-e", "-c", shell], env=env,
                          capture_output=True, text=True, check=False)

  def test_manually_dispatched_revision_build_accepts_its_own_run_id(self):
    result = self.validate()
    self.assertEqual(result.returncode, 0, result.stderr)

  def test_release_does_not_require_a_revision_artifact(self):
    result = self.validate(RELEASE="true", REVISION="2.2.1", CHECK_SUM="", SOURCE_RUN_ID="")
    self.assertEqual(result.returncode, 0, result.stderr)

  def test_revision_rejects_missing_artifact_or_stable_version(self):
    for overrides in ({"CHECK_SUM": ""}, {"SOURCE_RUN_ID": ""},
                      {"SOURCE_RUN_ID": "wrong-run"}, {"REVISION": "2.2.1"}):
      with self.subTest(overrides=overrides):
        self.assertNotEqual(self.validate(**overrides).returncode, 0)


if __name__ == "__main__":
  unittest.main()
