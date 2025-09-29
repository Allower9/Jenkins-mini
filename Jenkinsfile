pipeline {
    agent any

    environment {
        DEPLOY_USER = 'user1'
        DEPLOY_HOST = '84.201.164.197'
        DEPLOY_PATH = '/var/www/myapp'
        JAR_NAME = 'app.jar'
        LOG_FILE = 'app.log'
        BACKUP_JAR = 'app_backup.jar'
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
                        set -e
                        echo "Создаём директорию на сервере..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "mkdir -p $DEPLOY_PATH"

                        echo "Создаём резервную копию предыдущего JAR..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            if [ -f $DEPLOY_PATH/$JAR_NAME ]; then
                                mv $DEPLOY_PATH/$JAR_NAME $DEPLOY_PATH/$BACKUP_JAR
                                echo 'Резервная копия создана.'
                            fi
                        "

                        echo "Копируем новый JAR..."
                        scp -o StrictHostKeyChecking=no target/*.jar $DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/$JAR_NAME

                        echo "Останавливаем старый процесс..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            if pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                pkill -f 'java -jar $DEPLOY_PATH/$JAR_NAME'
                                echo 'Старое приложение остановлено.'
                            fi
                        "

                        echo "Запускаем новое приложение..."
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "
                            nohup java -jar $DEPLOY_PATH/$JAR_NAME > $DEPLOY_PATH/$LOG_FILE 2>&1 &
                            sleep 5
                            if ! pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                echo 'Ошибка запуска нового приложения. Выполняем откат...'
                                if [ -f $DEPLOY_PATH/$BACKUP_JAR ]; then
                                    mv $DEPLOY_PATH/$BACKUP_JAR $DEPLOY_PATH/$JAR_NAME
                                    nohup java -jar $DEPLOY_PATH/$JAR_NAME > $DEPLOY_PATH/$LOG_FILE 2>&1 &
                                    echo 'Откат завершён.'
                                fi
                                exit 1
                            fi
                            echo 'Приложение успешно запущено.'
                        "
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Деплой завершён успешно!"
        }
        failure {
            echo "Деплой завершился с ошибкой. Проверьте лог: $DEPLOY_PATH/$LOG_FILE"
        }
    }
}
