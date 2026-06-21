def utils = new com.sage.MyUtils()

pipeline {
    agent any

    stages {
        stage('Run') {
            steps {
                script {
                    utils.sayHello()
                }
            }
        }
    }
}
