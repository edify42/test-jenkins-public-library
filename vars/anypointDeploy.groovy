def call(args = null) {
    args = args ?: [:]
    // Populate from input map
    def repoPath = args.get("repoPath", null)
    def USERNAME = args.get("username", null)
    String branchName = "${env.BRANCH_NAME}"
    echo "${branchName} is the branch name"
    echo "${USERNAME} is not null"
    def artifactory = "http://172.17.0.1:8020/artifactory"

    if (!repoPath) {
        throw new Exception("Please specify a repoPath and name: `mavenDeploy(repoPath: 'libs-snapshot-local')`")
    }

    sh "${tool 'M3'}/bin/mvn -DaltDeploymentRepository='JCU Artifactory::default::${artifactory}/${repoPath}' -Dmaven.test.skip.exec=true -Denv.user=${USERNAME} -Denv.pass=${PASSWORD} -Denv.deployenv=Test deploy"
}
