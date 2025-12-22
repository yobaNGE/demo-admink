pipeline {
    agent any

    environment {
        DOCKER_COMPOSE_FILE = 'docker-compose.yml'
        APP_NAME = 'demo-admink'
    }

    triggers {

        pollSCM('H/1 * * * *')

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
                        sh "docker compose down --remove-orphans || echo 'No containers to stop'"
                        sh "docker compose up -d --build ${APP_NAME}"
                    } else {
                        bat "docker rm -f ${APP_NAME} || echo Container not found"
                        bat "docker-compose down --remove-orphans || echo No containers to stop"
                        bat "docker-compose up -d --build ${APP_NAME}"
                    }
                }
            }
        }

        stage('Health Check') {
            steps {
                echo 'Проверка работоспособности приложения...'
                script {
                    def maxRetries = 10
                    def retryCount = 0
                    def healthy = false

                    def hostIP = ''
                    if (isUnix()) {
                        hostIP = sh(script: "ip route | grep default | awk '{print \$3}'", returnStdout: true).trim()
                        echo "Host IP: ${hostIP}"
                    }

                    if (hostIP == '') {
                        hostIP = 'demo-admink'
                    }

                    while (retryCount < maxRetries && !healthy) {
                        try {
                            if (isUnix()) {
                                sh "curl -f http://${hostIP}:8080/actuator/health"
                            } else {
                                bat 'curl -f http://host.docker.internal:8080/actuator/health'
                            }
                            healthy = true
                            echo 'Приложение успешно запущено!'
                        } catch (Exception e) {
                            retryCount++
                            echo "Попытка ${retryCount}/${maxRetries}. Ожидание запуска приложения..."
                            sleep(time: 10, unit: 'SECONDS')
                        }
                    }

                    if (!healthy) {
                        error 'Приложение не запустилось в отведенное время!'
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
