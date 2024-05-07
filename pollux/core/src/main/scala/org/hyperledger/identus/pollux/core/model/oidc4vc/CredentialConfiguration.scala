package org.hyperledger.identus.pollux.core.model.oidc4vc

import org.hyperledger.identus.pollux.core.model.CredentialFormat

import java.net.URI
import java.time.Instant

final case class CredentialConfiguration(
    configurationId: String,
    format: CredentialFormat,
    schemaId: URI,
    createdAt: Instant
) {
  def scope: String = configurationId
}
