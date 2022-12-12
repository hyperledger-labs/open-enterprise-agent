package io.iohk.atala.pollux.service

import io.iohk.atala.api.http.model.{CollectionStats, Order, Pagination, PaginationInput}
import io.iohk.atala.pollux.schema.model.{VerificationPolicy, VerificationPolicyInput, VerificationPolicyPage}
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

trait VerificationPolicyService {
  def createVerificationPolicy(
      in: VerificationPolicyInput
  ): Task[VerificationPolicy]

  def getVerificationPolicyById(id: String): Task[Option[VerificationPolicy]]

  def updateVerificationPolicyById(
      id: String,
      update: VerificationPolicyInput
  ): Task[Option[VerificationPolicy]]

  def deleteVerificationPolicyById(id: String): Task[Option[VerificationPolicy]]

  def lookupVerificationPolicies(
      filter: VerificationPolicy.Filter,
      pagination: Pagination,
      order: Option[Order]
  ): Task[(VerificationPolicyPage, CollectionStats)]
}
