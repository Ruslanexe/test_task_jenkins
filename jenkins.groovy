pipeline {
    agent any
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/Ruslanexe/test_task_jenkins', branch: 'main'
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build('python-app:latest')
                }
            }
        }
        stage('Run Container') {
            steps {
                script {
                    sh 'docker run -d -p 5000:5000 --name python_app python-app:latest'
                    // Перевірка чи контейнер запущений
                    sh 'docker ps'
                    sh "docker logs python_app"
                    sh 'docker exec python_app ls -la /app'
                    sh "curl -f http://localhost:5000/health || exit 1"
                }
            }
        }
        stage('Test') {
            steps {
                script {
                    // Додаткова перевірка після запуску
                    sh "curl -f http://localhost:5000/health || exit 1"
                }
            }
        }
    }
    post {
        always {
            script {
                // Видалення контейнера після завершення
                sh 'docker rm -f python_app || true'
            }
        }
    }
}
