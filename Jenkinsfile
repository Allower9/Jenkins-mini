pipeline {
    agent any

    environment {
        DEPLOY_USER = 'user1'
        DEPLOY_HOST = '84.201.164.197'
        DEPLOY_PATH = '/var/www/myapp'
        JAR_NAME = 'app.jar'
    }

    stages {
        stage('Build') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: ['deploy2key']) {
                    sh '''
                        set -e
                        echo "1. Создаём директорию на сервере..."
                        ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "mkdir -p /var/www/myapp"

                        echo "2. Копируем JAR на сервер..."
                        scp -o StrictHostKeyChecking=no target/simple-app-1.0-SNAPSHOT.jar user1@84.201.164.197:/var/www/myapp/app.jar

                        echo "3. Останавливаем старое приложение..."
                        ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "pkill -f java.*app.jar || true"

                        echo "4. Запускаем новое приложение..."
                        ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "cd /var/www/myapp && nohup java -jar app.jar > app.log 2>&1 &"

                        echo "5. Проверяем что приложение запустилось..."
                        sleep 5
                        ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "ps aux | grep java | grep app.jar || echo Процесс не найден"
                    '''
                }
            }
        }
    }

    post {
        always {
            echo "Build завершен: ${currentBuild.result}"
        }
        success {
            echo "✅ Деплой успешно завершен!"
        }
        failure {
            echo "❌ Деплой завершился с ошибкой"
        }
    }
}
