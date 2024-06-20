package org.hyperledger.identus.mercury

import org.hyperledger.identus.shared.models._

import java.io.IOException

package object error {

  type MercuryException = MercuryError | IOException
  type MercuryThrowable = MercuryError | IOException | Throwable // REMOVE Throwable

  def mercuryErrorAsThrowable(error: MercuryThrowable): java.lang.Throwable = error match
    // case ex: MercuryError =>
    //   ex match
    //     case te: TransportError => new RuntimeException(te)
    case ex: MercuryError => ex.toUnmanagedFailureException
    case ex: IOException  => ex
    case ex: Throwable    => ex

  sealed trait MercuryError extends Failure {
    override val namespace: String = "MercuryError"
  }

  trait TransportError extends Exception with MercuryError

  case class SendMessageError(cause: Throwable, mData: Option[String] = None) extends MercuryError {
    override val statusCode = org.hyperledger.identus.shared.models.StatusCode.FixmeStatusCode
    override val userFacingMessage =
      s"Error when sending message: ${cause.getMessage};${mData.map(e => s" DATA:'$e'").getOrElse("")}. "
        + cause.getMessage
  }

  // case class SendMessageError(cause: Throwable, mData: Option[String] = None)
  //     extends RuntimeException(
  //       s"Error when sending message: ${cause.getMessage};${mData.map(e => s" DATA:'$e'").getOrElse("")}",
  //       cause
  //     )
  //     with TransportError

}
