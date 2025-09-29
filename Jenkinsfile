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
                        # Создаём директорию на сервере
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "mkdir -p $DEPLOY_PATH"

                        # Копируем jar-файл на сервер
                        scp -o StrictHostKeyChecking=no target/*.jar $DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/$JAR_NAME

                        # Завершаем старый процесс приложения, если он есть
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "pkill -f 'java -jar $DEPLOY_PATH/$JAR_NAME' || true"

                        # Запускаем приложение в фоне
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST "nohup java -jar $DEPLOY_PATH/$JAR_NAME > $DEPLOY_PATH/app.log 2>&1 &"
                    """
                }
            }
        }
    }
}
