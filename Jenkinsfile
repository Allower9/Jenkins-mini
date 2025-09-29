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
                    sh """
                        set -e  # Выход при любой ошибке
                        
                        echo "Создаём директорию на сервере..."
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "mkdir -p $DEPLOY_PATH"

                        echo "Создаём резервную копию предыдущего JAR..."
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "
                            if [ -f $DEPLOY_PATH/$JAR_NAME ]; then
                                mv $DEPLOY_PATH/$JAR_NAME $DEPLOY_PATH/app_backup_\\$(date +%Y%m%d_%H%M%S).jar
                                echo 'Резервная копия создана.'
                            fi
                        "

                        echo "Копируем новый JAR..."
                        scp -o StrictHostKeyChecking=no -o ConnectTimeout=30 target/simple-app-1.0-SNAPSHOT.jar $DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/$JAR_NAME

                        echo "Останавливаем старый процесс..."
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "
                            if pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                pkill -f 'java -jar $DEPLOY_PATH/$JAR_NAME'
                                sleep 5  # Даём время для graceful shutdown
                                echo 'Старое приложение остановлено.'
                            else
                                echo 'Процесс не найден, продолжаем...'
                            fi
                        "

                        echo "Запускаем новое приложение..."
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "
                            cd $DEPLOY_PATH
                            nohup java -jar $JAR_NAME > app.log 2>&1 &
                            echo \$! > app.pid
                            echo 'Приложение запущено с PID: ' \$(cat app.pid)
                        "

                        echo "Проверяем запуск..."
                        sleep 10
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "
                            if pgrep -f 'java -jar $DEPLOY_PATH/$JAR_NAME' > /dev/null; then
                                echo '✅ Приложение успешно запущено!'
                                echo '=== Последние строки лога ==='
                                tail -10 $DEPLOY_PATH/app.log
                            else
                                echo '❌ Приложение не запустилось'
                                echo '=== Полный лог ошибок ==='
                                cat $DEPLOY_PATH/app.log
                                exit 1
                            fi
                        "
                    """
                }
            }
            post {
                success {
                    echo '✅ Деплой успешно завершен!'
                }
                failure {
                    echo '❌ Деплой завершился с ошибкой'
                    sh """
                        ssh -o StrictHostKeyChecking=no -o ConnectTimeout=30 $DEPLOY_USER@$DEPLOY_HOST "cat $DEPLOY_PATH/app.log" || true
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline завершен с статусом: ${currentBuild.result}"
        }
        failure {
            emailext (
                subject: "СБОЙ СБОРКИ: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                Проверьте сборку: ${env.BUILD_URL}

                Ошибка в стадии деплоя.
                """,
                to: "your-email@example.com"
            )
        }
    }
}
