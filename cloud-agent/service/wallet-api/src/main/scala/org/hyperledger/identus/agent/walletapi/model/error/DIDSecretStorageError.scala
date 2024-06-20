package org.hyperledger.identus.agent.walletapi.model.error

import org.hyperledger.identus.mercury.model.DidId
import org.hyperledger.identus.shared.models._

sealed trait DIDSecretStorageError(
    override val statusCode: StatusCode,
    override val userFacingMessage: String
) extends Failure {
  override val namespace: String = "DIDSecretStorageError"
}

object DIDSecretStorageError {
  case class KeyNotFoundError(didId: DidId, keyId: String)
      extends DIDSecretStorageError(
        StatusCode.FixmeStatusCode,
        s"The not found: keyId='$keyId', didId='$didId'"
      )
  case class WalletNotFoundError(didId: DidId)
      extends DIDSecretStorageError(
        StatusCode.FixmeStatusCode,
        s"The DID not Found in Wallet: didId='$didId'"
      )
}
