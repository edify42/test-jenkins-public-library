def call(args = null) {
    args = args ?: [:]
    // Populate from input map
    def USERNAME = args.get("username", null)
    def PASSWORD = args.get("password", null)
    def ENV = args.get("environment", null)
    // serverNumber is the literal number suffix at the end like 02, or 03
    def serverNumber = args.get("serverNumber", null)
    String branchName = "${env.BRANCH_NAME}"
    //echo "${branchName} is the branch name"
    //echo "${USERNAME} is not null"
    // CHANGE TO YOUR RELEVANT ARTIFACTORY INSTANCE
    def artifactory = "http://172.17.0.1:8020/artifactory"

    if (!USERNAME || !PASSWORD || !ENV || ! serverNumber) {
        throw new Exception("Missing input key and value `anypointDeploy(username: 'someuser', password: 'somepass', environment: 'somenv'...)`")
    }

    switch (branchName) {
      case 'develop':
         if (ENV != 'Test') {
           throw new Exception("Incorrect environment (${ENV}) selected on branch ${branchName}")
         }
         String repoPath = 'anypoint-snapshot-local'
         def server = 'anypoint-test-' + serverNumber
         sh "${tool 'M3'}/bin/mvn -DaltDeploymentRepository='JCU Artifactory::default::${artifactory}/${repoPath}' -Dmaven.test.skip.exec=true -Denv.user=${USERNAME} -Denv.pass=${PASSWORD} -Denv.deployenv=${ENV} -Denv.target=${server} clean deploy -f Deployment/pom.xml"
         break
      case 'master':
         if (ENV != 'UAT' || 'Production') {
           throw new Exception("Incorrect environment (${ENV}) selected on branch ${branchName}")
         }
         String repoPath = 'anypoint-release-local'
         if (ENV == 'UAT'){
           def server = 'anypoint-uat-' + serverNumber
           sh "${tool 'M3'}/bin/mvn -DaltDeploymentRepository='JCU Artifactory::default::${artifactory}/${repoPath}' -Dmaven.test.skip.exec=true -Denv.user=${USERNAME} -Denv.pass=${PASSWORD} -Denv.deployenv=${ENV} -Denv.target=${server} clean deploy -f Deployment/pom.xml"
         }
         if (ENV == 'Production'){
           def server = 'anypoint-prod-' + serverNumber
           timeout(time:1, unit:'DAYS') {
               input message:'Final approval for Production Deployment?' submitter: ddeloit1,admin
           }
           sh "${tool 'M3'}/bin/mvn -DaltDeploymentRepository='JCU Artifactory::default::${artifactory}/${repoPath}' -Dmaven.test.skip.exec=true -Denv.user=${USERNAME} -Denv.pass=${PASSWORD} -Denv.deployenv=${ENV} -Denv.target=${server} clean deploy -f Deployment/pom.xml"
         }
         break

      default:
         throw new Exception("No suitable branch for Anypoint code deployment")
    }
}
