pipeline {
     agent any
     environment {
        DISCORD_WEBHOOK = credentials('barista-webhook-url')
     }
     
     stages {
          stage('Build') { 
               steps {
                 sh 'mvn -B clean package --debug' 
               }
          }
     }
     
     post {
          always {
               archiveArtifacts artifacts: 'target/*original-BaristaGerald*.jar', fingerprint: true
               discordSend description: "Build: ${env.BUILD_NUMBER}\nResult: ${currentBuild.currentResult}\nChanges: ${getCommitList()}\n", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: DISCORD_WEBHOOK
          }
     }
}

def getCommitList() {
    checkout scm
    def lastSuccessfulCommit = getLastSuccessfulCommit()
    def currentCommit = commitHashForBuild( currentBuild.rawBuild )
    if (lastSuccessfulCommit) {
        commits = sh(
            script: "git rev-list $currentCommit \"^$lastSuccessfulCommit\"",
            returnStdout: true
        ).split('\n')
    }
    return commits
}

def getLastSuccessfulCommit() {
  def lastSuccessfulHash = null
  def lastSuccessfulBuild = currentBuild.rawBuild.getPreviousSuccessfulBuild()
  if ( lastSuccessfulBuild ) {
    lastSuccessfulHash = commitHashForBuild( lastSuccessfulBuild )
  }
  return lastSuccessfulHash
}

@NonCPS
def commitHashForBuild( build ) {
  def scmAction = build?.actions.find { action -> action instanceof jenkins.scm.api.SCMRevisionAction }
  return scmAction?.revision?.hash
}
