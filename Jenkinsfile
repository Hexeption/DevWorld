pipeline {
    agent {
        docker { image "openjdk:8-jdk" }
    }
    stages {
        stage('Build') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew build --console=plain'
            }
        }
        stage('Release') {
            when {
                not {
                    changeRequest()
                }
            }
            steps {
                withCredentials([string(credentialsId: 'CURSE_API_KEY', variable: 'CURSE_API_KEY'),string(credentialsId: 'REPO_PASSWORD', variable: 'REPO_PASSWORD')]) {
                    sh "chmod +x gradlew && ./gradlew publish curseforge -PmavenPass=$REPO_USERNAME -Pcurse_api_key=$CURSE_API_KEY --console=plain"
                }
            }
        }
    }
    post {
        always {
            script {
                archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true, onlyIfSuccessful: true, allowEmptyArchive: true
            }
        }
    }
}
