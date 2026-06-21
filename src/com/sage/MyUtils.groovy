package com.sage

class MyUtils implements Serializable {

    def steps   // Jenkins pipeline steps binding

    MyUtils(steps) {
        this.steps = steps
    }

    // Example: simple log message
    def sayHello(String name = "Sage") {
        steps.echo "Hello, ${name}! This is MyUtils.groovy running from the shared library."
    }

    // Example: run a shell or PowerShell command depending on agent OS
    def runCommand(String cmd) {
        if (isWindows()) {
            steps.powershell(returnStdout: true, script: cmd).trim()
        } else {
            steps.sh(returnStdout: true, script: cmd).trim()
        }
    }

    // Example: detect Windows agent
    boolean isWindows() {
        return steps.isUnix() == false
    }

    // Example: wrap a stage with timing
    def timed(String label, Closure body) {
        def start = System.currentTimeMillis()
        steps.echo "Starting ${label}..."
        body()
        def end = System.currentTimeMillis()
        steps.echo "${label} completed in ${(end - start) / 1000}s"
    }
}
