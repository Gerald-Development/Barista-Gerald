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
               discordSend description: "Build: ${env.BUILD_NUMBER}\nResult: ${currentBuild.currentResult}", link: env.BUILD_URL, result: currentBuild.currentResult, title: JOB_NAME, webhookURL: DISCORD_WEBHOOK
          }
     }
}
