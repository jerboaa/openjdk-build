import groovy.json.JsonOutput

if (!binding.hasVariable('TEST')) TEST = true

triggerSchedule = "@daily"
gitRefSpec = ""

if (TEST) {
    triggerSchedule = "@yearly"
    gitRefSpec = "+refs/pull/*/head:refs/remotes/pull/* +refs/heads/master:refs/remotes/origin/master"
}

folder("${BUILD_FOLDER}")
folder("${BUILD_FOLDER}/jobs")

pipelineJob("${BUILD_FOLDER}/${JOB_NAME}") {
    description('<h1>THIS IS AN AUTOMATICALLY GENERATED JOB DO NOT MODIFY, IT WILL BE OVERWRITTEN.</h1><p>This job is defined in pipeline_job_template.groovy in the openjdk-build repo, if you wish to change it modify that.</p>')
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url("${GIT_URL}")
                        refspec(gitRefSpec)
                    }
                    branch("${BRANCH}")
                }
            }
            scriptPath(SCRIPT)
        }
    }
    triggers {
        cron(triggerSchedule)
    }
    logRotator {
        numToKeep(10)
        artifactNumToKeep(2)
    }

    properties {
        copyArtifactPermissionProperty {
            projectNames('*')
        }
    }

    parameters {
        textParam('targetConfigurations', JsonOutput.prettyPrint(JsonOutput.toJson(targetConfigurations)))
        choiceParam('releaseType', ['Nightly', 'Nightly Without Publish', 'Release' ], 'Nightly - release a standard nightly build.<br/>Nightly Without Publish - run a nightly but do not publish.<br/>Release - this is a release, this will need to be manually promoted.')
        stringParam('overridePublishName', "", 'Name that determines the publish name (and is used by the meta-data file), defaults to scmReference. Nightly builds: Leave blank (defaults to a date_time stamp. OpenJ9 Release build Java 8 example <code>jdk8u192-b12_openj9-0.12.1</code> and for OpenJ9 Java 11 example <code>jdk-11.0.2+9_openj9-0.12.1</code>.')
        stringParam('scmReference', "", 'Tag name or Branch name from which to build. Nightly builds: Defaults to master. Release builds: For hotspot this would be the OpenJDK tag and for OpenJ9 this will be the branch.')
        booleanParam('enableTests', !TEST, 'If set to true the test pipeline will be executed')
        stringParam('additionalConfigureArgs', "", "Additional arguments that will be ultimately passed to OpenJDK's <code>./configure</code>")
        stringParam('additionalBuildArgs', "", "Additional arguments to be passed to <code>makejdk-any-platform.sh</code>")
        stringParam('overrideFileNameVersion', "", "When forming the filename, ignore the build version and override it. For instance if you set this to 'FOO' the final file name will be of the form: <code>OpenJDK8U-jre_ppc64le_linux_openj9_FOO.tar.gz</code>")
        booleanParam('cleanWorkspaceBeforeBuild', false, "Clean out the workspace before the build")
        stringParam('adoptBuildNumber', "", "Starts at 1. If you ever need to re-release then bump this number. Currently this is only added to the build metadata file.")
    }
}