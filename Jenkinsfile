pipeline {
    agent any

    stages {
        stage('Maven Package') {
            steps {
                sh 'mvn package  -DskipTests=true'
            }
        }
        stage('Maven Tests') {
             steps {
                 sh 'mvn test'
             }
         }
        stage('Docker Build') {
            steps {
                sh """
                    docker build . -t fractalwoodstories/user-service:arm64-latest
                """
            }
        }
        stage('Docker push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'fractalwoodstories-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        shortGitCommit = env.GIT_COMMIT[0..7]
                        sh """
                            docker tag fractalwoodstories/user-service:arm64-latest fractalwoodstories/user-service:arm64-${shortGitCommit}
                            docker login -u ${USERNAME} -p ${PASSWORD}
                            docker push fractalwoodstories/user-service:arm64-latest
                            docker push fractalwoodstories/user-service:arm64-${shortGitCommit}
                            docker logout
                        """
                    }
                }
            }
        }
        stage('Docker push main') {
            when {
                expression { env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'origin/main' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: 'fractalwoodstories-docker-hub', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                    sh """
                        docker tag fractalwoodstories/user-service:arm64-latest fractalwoodstories/user-service:arm64-main
                        docker login -u ${USERNAME} -p ${PASSWORD}
                        docker push fractalwoodstories/user-service:arm64-master
                        docker logout
                    """
                }
            }
        }
    }
}