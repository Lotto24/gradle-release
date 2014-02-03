package release

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

//@Mixin(PluginHelper)
class ReleasePluginSemanticVersionTests extends Specification {

	Project project
	static boolean promptedForVersion

	def testDir = new File("build/tmp/test/${getClass().simpleName}")

	def setup() {
		project = ProjectBuilder.builder().withName(getClass().simpleName).withProjectDir(testDir).build()
		project.apply plugin: MockedTestReleasePlugin
		promptedForVersion = false
	}

	def 'when a patch release is executed'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseType', "patch")
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '1.2.2'
		promptedForVersion == true
	}

	def 'when a patch release for a snapshot version is executed'() {
		given:
		project.version = '1.2.1-SNAPSHOT'
		project.setProperty('releaseType', "patch")
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '1.2.1'
	}

	def 'when a minor release is executed'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseType', "minor")
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '1.3.0'
	}

	def 'when a major release is executed'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseType', "major")
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '2.0.0'
	}

	def 'when a patch release with automatic versioning is executed'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseType', "patch")
		project.setProperty('gradle.release.useAutomaticVersion', 'true')
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '1.2.2'
		promptedForVersion == false
	}

	def 'when an explicit release version is given'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseVersion', '1.5.0')
		project.setProperty('gradle.release.useAutomaticVersion', 'true')
		when:
		project.unSnapshotVersion.execute()
		project.confirmReleaseVersion.execute()
		then:
		project.version == '1.5.0'
		promptedForVersion == false
	}

	def 'when an explicit release version and a release type are given'() {
		given:
		project.version = '1.2.1'
		project.setProperty('releaseVersion', '1.5.0')
		project.setProperty('releaseType', "patch")
		when:
		project.confirmReleaseVersion.execute()
		then:
		GradleException ex = thrown()
		ex.cause.message.contains "1.5.0"
		ex.cause.message.contains "patch"
	}

	static class MockedTestReleasePlugin extends TestReleasePlugin {
		void updateVersionProperty(String newVersion) {
			project.version = newVersion
		}

		String readLine(String message, String defaultValue = null) {
			if (message.startsWith("This release version")) {
				promptedForVersion = true
				return defaultValue
			} else {
				return super.readLine(message, defaultValue)
			}
		}
	}
}
