def call(Map config = [:]) {

    // Defaults
    def agentLabel = config.agent ?: 'Ubuntu'
    def jdkName    = config.jdk   ?: 'jdk-21'
    def mvnName    = config.mvn   ?: 'mvn'
    def repo       = config.repo ?: ''https://github.com/sageisinc/sagejenkins.git'
    def skipTests  = config.skipTests ?: false

    node(agentLabel) {

        stage('Checkout') {
            if (isUnix()) {
                echo "Running on Linux — using Linux Git"
                git branch: config.branch ?: 'main',
                    url: config.repo ?: repo,
                    credentialsId: 'sageisinc'
            } else {
                echo "Running on Windows — using checkout scm"
                checkout scm
            }
        }

        stage('Setup Environment') {
            env.JAVA_HOME  = tool name: jdkName, type: 'jdk'
            env.MAVEN_HOME = tool name: mvnName, type: 'maven'
            env.PATH       = "${env.JAVA_HOME}/bin:${env.MAVEN_HOME}/bin:${env.PATH}"

            echo "JAVA_HOME = ${env.JAVA_HOME}"
            echo "MAVEN_HOME = ${env.MAVEN_HOME}"
        }

        stage('Build') {
            def cmd = skipTests ?
                "mvn -B -DskipTests clean package" :
                "mvn -B clean package"

            runCmd(cmd)
        }

        stage('Unit Tests') {
            if (!skipTests) {
                runCmd("mvn -B test")
                junit '**/target/surefire-reports/*.xml'
            } else {
                echo "Skipping tests"
            }
        }

        stage('Archive Artifacts') {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }

        echo "Maven pipeline completed successfully."
    }
}

// Cross‑platform command runner
def runCmd(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        powershell cmd
    }
}
