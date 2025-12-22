pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        APP_NAME = 'demo-admink'
        COMPOSE_PROJECT_NAME = 'demo-admink'
    }

    triggers {

        pollSCM('H/2 * * * *')

        // githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Получение кода из репозитория...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Сборка приложения...'
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw && ./mvnw clean package -DskipTests'
                    } else {
                        bat 'mvnw.cmd clean package -DskipTests'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Запуск тестов...'
                script {
                    if (isUnix()) {
                        sh 'chmod +x mvnw && ./mvnw test'
                    } else {
                        bat 'mvnw.cmd test'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'Сборка Docker образа...'
                script {
                    if (isUnix()) {
                        sh "docker build -t ${APP_NAME}:${BUILD_NUMBER} -t ${APP_NAME}:latest ."
                    } else {
                        bat "docker build -t ${APP_NAME}:${BUILD_NUMBER} -t ${APP_NAME}:latest ."
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                echo 'Развертывание приложения...'
                script {
                    if (isUnix()) {
                        sh "docker rm -f ${APP_NAME} || echo 'Container not found'"
                        sh "docker compose -p ${COMPOSE_PROJECT_NAME} down --remove-orphans || echo 'No containers to stop'"
                        sh "docker compose -p ${COMPOSE_PROJECT_NAME} up -d --build ${APP_NAME}"
                    } else {
                        bat "docker rm -f ${APP_NAME} || echo Container not found"
                        bat "docker-compose -p ${COMPOSE_PROJECT_NAME} down --remove-orphans || echo No containers to stop"
                        bat "docker-compose -p ${COMPOSE_PROJECT_NAME} up -d --build ${APP_NAME}"
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline выполнен успешно!'
        }
        failure {
            echo 'Pipeline завершился с ошибкой!'
        }
        always {
            cleanWs()
        }
    }
}
