pipeline {
    agent {
        docker {
            image 'maven:3.8.1-adoptopenjdk-11' 
            args '-v /root/.m2:/root/.m2' 
        }
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
            archiveArtifacts artifacts: '*.jar', fingerprint: true
            junit 'build/reports/**/*.xml'
        }
    }
}
