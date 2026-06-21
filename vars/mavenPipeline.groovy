def call(Map config = [:]) {

    // Defaults
    def agentLabel = config.agent ?: 'any'
    def jdkName    = config.jdk   ?: 'jdk-21'
    def mvnName    = config.mvn   ?: 'maven-3.9'
    def skipTests  = config.skipTests ?: false

    node(agentLabel) {

        stage('Checkout') {
            checkout scm
        }

        stage('Setup Environment') {
            env.JAVA_HOME  = tool name: jdkName, type: 'jdk'
            env.MAVEN_HOME = tool name: mvnName, type: 'maven'
            env.PATH       = "${env.JAVA_HOME}/bin:${env.MAVEN_HOME}/bin:${env.PATH}"

            echo "Using JDK: ${env.JAVA_HOME}"
            echo "Using Maven: ${env.MAVEN_HOME}"
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

// Cross-platform helper
def runCmd(String cmd) {
    if (isUnix()) {
        sh cmd
    } else {
        powershell cmd
    }
}
