package io.iohk.atala.castor.core.model

package object error {

  sealed trait DIDOperationError
  object DIDOperationError {
    final case class DLTProxyError(cause: Throwable) extends DIDOperationError
    final case class UnexpectedDLTResult(msg: String) extends DIDOperationError
    final case class ValidationError(cause: OperationValidationError) extends DIDOperationError
  }

  sealed trait DIDResolutionError {
    def message: String
  }

  object DIDResolutionError {
    final case class DLTProxyError(cause: Throwable) extends DIDResolutionError {
      override def message: String = cause.getMessage
    }
    final case class UnexpectedDLTResult(msg: String) extends DIDResolutionError {
      override def message: String = msg
    }
    final case class ValidationError(cause: OperationValidationError) extends DIDResolutionError {
      override def message: String = cause.toString
    }
  }

  sealed trait OperationValidationError

  object OperationValidationError {
    final case class TooManyDidPublicKeyAccess(limit: Int, access: Option[Int]) extends OperationValidationError
    final case class TooManyDidServiceAccess(limit: Int, access: Option[Int]) extends OperationValidationError
    final case class InvalidArgument(msg: String) extends OperationValidationError
  }

}
