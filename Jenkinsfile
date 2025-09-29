pipeline {
    agent any

    environment {
        DEPLOY_USER = 'user1'
        DEPLOY_HOST = '84.201.164.197'
        DEPLOY_PATH = '/var/www/myapp'
        JAR_NAME = 'app.jar'
        LOG_FILE = 'app.log'
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
                    sh """
                        set -e  # прекращаем выполнение при любой ошибке

                        echo "Создаём директорию на сервере..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "mkdir -p $DEPLOY_PATH"

                        echo "Копируем jar-файл на сервер..."
                        scp -o StrictHostKeyChecking=no target/*.jar $DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/$JAR_NAME

                        echo "Завершаем старое приложение (если есть)..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            if pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                pkill -f 'java -jar $DEPLOY_PATH/$JAR_NAME'
                                echo 'Старый процесс завершён.'
                            else
                                echo 'Процесс не найден, пропускаем.'
                            fi
                        "

                        echo "Запускаем новое приложение..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            nohup java -jar $DEPLOY_PATH/$JAR_NAME > $DEPLOY_PATH/$LOG_FILE 2>&1 &
                            sleep 5  # ждём 5 секунд для проверки запуска
                            if pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                echo 'Приложение успешно запущено.'
                            else
                                echo 'Ошибка запуска приложения, см. лог $DEPLOY_PATH/$LOG_FILE'
                                exit 1
                            fi
                        "
                    """
                }
            }
        }
    }

    post {
        failure {
            echo "Деплой завершился с ошибкой. Проверьте лог на сервере: $DEPLOY_PATH/$LOG_FILE"
        }
        success {
            echo "Деплой успешно выполнен!"
        }
    }
}
