package io.iohk.atala.agent.walletapi.model

import io.iohk.atala.castor.core.model.did.{CanonicalPrismDID, PrismDIDOperation, ScheduledDIDOperationStatus}

import java.time.Instant
import scala.collection.immutable.ArraySeq

final case class ManagedDIDDetail(did: CanonicalPrismDID, state: ManagedDIDState)

final case class ManagedDIDState(
    createOperation: PrismDIDOperation.Create,
    didIndex: Int,
    publicationState: PublicationState
) {
  def keyMode: KeyManagementMode = KeyManagementMode.HD
}

sealed trait PublicationState

object PublicationState {
  final case class Created() extends PublicationState
  final case class PublicationPending(publishOperationId: ArraySeq[Byte]) extends PublicationState
  final case class Published(publishOperationId: ArraySeq[Byte]) extends PublicationState
}

final case class DIDUpdateLineage(
    operationId: ArraySeq[Byte],
    operationHash: ArraySeq[Byte],
    previousOperationHash: ArraySeq[Byte],
    status: ScheduledDIDOperationStatus,
    createdAt: Instant,
    updatedAt: Instant
)

final case class ManagedDIDStatePatch(publicationState: PublicationState)
