def workspace = new File(".").getAbsolutePath()
if (!new File("${workspace}/common.groovy").canRead()) { workspace = "${WORKSPACE}"}
evaluate(new File("${workspace}/common.groovy"))

job("clusterator-create") {
  disabled() // delete this file when ready to fully remove job
  description "Create a set number of clusters in the hephy leasable project. This job runs Monday-Friday at 7AM."

  logRotator {
    daysToKeep defaults.daysToKeep
  }

  triggers {
    cron('H 7 * * 1-5')
  }

  parameters {
    stringParam('NUMBER_OF_CLUSTERS', '2', 'Number of clusters to create at 1 time')
    stringParam('NUM_NODES', '5', 'Number of nodes in each cluster')
    stringParam('MACHINE_TYPE', 'n1-standard-4', 'Node type')
    stringParam('VERSION', '', 'The version of kubernetes to use.')
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    credentialsBinding {
      string("GCLOUD_CREDENTIALS", "15122437-45b8-48ac-bb84-652394c8a927")
    }
  }

  steps {
    shell """
      #!/usr/bin/env bash

      set -eo pipefail
      docker run \
      -e GCLOUD_CREDENTIALS="\${GCLOUD_CREDENTIALS}" \
      -e NUMBER_OF_CLUSTERS="\${NUMBER_OF_CLUSTERS}" \
      -e NUM_NODES="\${NUM_NODES}" \
      -e MACHINE_TYPE="\${MACHINE_TYPE}" \
      -e VERSION="\${VERSION}" \
      quay.io/kingdonb/clusterator:git-d863fa2 create
    """.stripIndent().trim()
  }
}

job("clusterator-delete") {
  disabled() // delete this file when ready to fully remove job
  description "Clean up clusters in the hephy leasable project. This job runs Monday-Friday at 7PM."

  logRotator {
    daysToKeep defaults.daysToKeep
  }

  triggers {
    cron('H 19 * * 1-5')
  }

  wrappers {
    timestamps()
    colorizeOutput 'xterm'
    credentialsBinding {
      string("GCLOUD_CREDENTIALS", "15122437-45b8-48ac-bb84-652394c8a927")
    }
  }

  steps {
    shell """
      #!/usr/bin/env bash

      set -eo pipefail
      docker run -e GCLOUD_CREDENTIALS="\${GCLOUD_CREDENTIALS}" quay.io/kingdonb/clusterator:git-d863fa2 delete
    """.stripIndent().trim()
  }
}
