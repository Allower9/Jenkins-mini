pipeline {
    agent any

    environment {
        DEPLOY_USER = 'user1'                    // юзер на сервере
        DEPLOY_HOST = '127.0.0.1'                // тот же сервер, где Jenkins
        DEPLOY_PATH = '/var/www/myapp'           // директория деплоя
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/yourusername/simple-app.git'
            }
        }

        stage('Build') {
            steps {
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
                sshagent (credentials: ['deploy-key-id']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no $DEPLOY_USER@$DEPLOY_HOST 'mkdir -p $DEPLOY_PATH'
                        scp target/*.jar $DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/app.jar
                        # убиваем предыдущий процесс (если был)
                        ssh $DEPLOY_USER@$DEPLOY_HOST "pkill -f 'java -jar $DEPLOY_PATH/app.jar' || true"
                        # запускаем новое приложение в фоне
                        ssh $DEPLOY_USER@$DEPLOY_HOST "nohup java -jar $DEPLOY_PATH/app.jar > $DEPLOY_PATH/app.log 2>&1 &"
                    """
                }
            }
        }
    }
}
