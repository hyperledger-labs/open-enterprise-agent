package io.iohk.atala.sharedtest.containers

import org.testcontainers.utility.DockerImageName

object KeycloakTestContainer {
  def keycloakContainer(
      imageName: String = "quay.io/keycloak/keycloak:24.0.1",
  ): KeycloakContainerCustom = {
    val isOnGithubRunner = sys.env.contains("GITHUB_NETWORK")
    val container =
      new KeycloakContainerCustom(
        dockerImageNameOverride = DockerImageName.parse(imageName),
        isOnGithubRunner = isOnGithubRunner
      )

    sys.env.get("GITHUB_NETWORK").map { network =>
      container.container.withNetworkMode(network)
    }

    container
  }
}
