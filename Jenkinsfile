pipeline {
    agent any

    environment {
        GIT_CREDENTIALS_ID = '1'
        FRONTEND_REPO = 'github.com/xmr977/xmiipp-frontend-1.git'
        BACKEND_REPO = 'github.com/xmr977/xmiipp-backend-1.git'
    }

    stages {
        stage('Clone Repositories') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: "${GIT_CREDENTIALS_ID}", usernameVariable: 'GIT_USER', passwordVariable: 'GIT_PASS')]) {
                            // Clone frontend and backend repositories
                             sh 'git clone https://${GIT_USER}:${GIT_PASS}@${FRONTEND_REPO} frontend'
                             sh 'git clone https://${GIT_USER}:${GIT_PASS}@${BACKEND_REPO} backend'
                        }
                    }
                }

        stage('Build Docker Images') {
            steps {
                // Build Docker images for frontend and backend
                sh 'docker build -t xmiipp-backend:latest ./backend'
                sh 'docker build -t xmiipp-frontend:latest ./frontend'

            }
        }

        stage('Deploy with Docker Compose') {
            steps {
                // Deploy using Docker Compose
                sh 'docker-compose up -d --build'
            }
        }
    }

    post {
        always {
            // Clean up unused Docker resources
            sh 'docker system prune -f'
        }
    }
}
