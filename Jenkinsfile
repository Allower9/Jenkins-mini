pipeline {
    agent any

    environment {
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout([
                    $class: 'GitSCM', 
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[
                        url: 'https://github.com/Allower9/Jenkins-mini.git',
                        credentialsId: 'deploy2key'
                    ]]
                ])
            }
        }

        stage('Build') {
            steps {
                echo 'Проверка версий Java и Maven...'
                sh 'java -version'
                sh 'mvn -version'

                echo 'Сборка проекта...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                echo 'Прогон тестов...'
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('Deploy') {
            steps {
                sshagent(credentials: ['deploy_user1']) {
                    sh '''
                    set +e

                    echo "1. Создаём директорию на сервере..."
                    ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "mkdir -p /var/www/myapp"

                    echo "2. Создаём резервную копию предыдущего JAR (если есть)..."
                    ssh -o StrictHostKeyChecking=no user1@84.201.164.197 '
                        if [ -f /var/www/myapp/app.jar ]; then
                            mv /var/www/myapp/app.jar /var/www/myapp/app_backup.jar
                            echo "Резервная копия создана."
                        fi
                    '

                    echo "3. Копируем новый JAR..."
                    scp -o StrictHostKeyChecking=no target/simple-app-1.0-SNAPSHOT.jar user1@84.201.164.197:/var/www/myapp/app.jar

                    echo "4. Останавливаем старое приложение..."
                    ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "pkill -f 'java.*app.jar' || true"

                    echo "5. Запускаем новое приложение..."
                    ssh -o StrictHostKeyChecking=no user1@84.201.164.197 "nohup java -jar /var/www/myapp/app.jar > /var/www/myapp/app.log 2>&1 &"

                    set -e
                    '''
                }
            }
        }
    }

    post {
        success {
            echo '✅ Сборка и деплой прошли успешно!'
        }
        failure {
            echo '❌ Произошла ошибка при сборке или деплое. Проверьте логи.'
        }
    }
}
