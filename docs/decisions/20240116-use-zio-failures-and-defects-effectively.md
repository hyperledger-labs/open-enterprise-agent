# Use ZIO Failures and Defects Effectively

- Status: draft
- Deciders: Fabio Pinheiro, Shailesh Patil, Pat Losoponkul, Yurii Shynbuiev, David Poltorak, Benjamin Voiturier
- Date: 2024-01-16
- Tags: error-handling, zio

## Context and Problem Statement

ZIO is a powerful and purely functional library for building scalable and resilient applications in Scala. However,
effectively handling errors within the context of ZIO presents challenges that must be addressed.

Within our software development projects utilising ZIO, the management and handling of errors have emerged as areas
requiring more clarity and strategy. The existing practices have shown limitations in terms of their efficiency and
comprehensiveness.

The key issues are:

1. **Lack of Consistent Error Handling Strategies**: There's inconsistency in error handling across different modules
   and components of our ZIO-based applications, making it challenging to maintain a unified approach.
2. **Understanding and Communicating Errors**: There's a need for a clearer method to categorise errors and communicate
   these effectively within the team and across various layers, facilitating quicker identifications and resolutions.
3. **Optimising Error Recovery Mechanisms**: While ZIO provides powerful abstractions for error recovery, there's a
   necessity to optimise these mechanisms to ensure graceful degradation and resilience in our applications.

This ADR aims to explore and define strategies for utilising ZIO's capabilities more effectively in handling errors. It
will outline decision drivers, available options, their pros and cons, and ultimately, the recommended approach to
enhance our error management practices with the ZIO framework.

Effective management of errors directly impacts the reliability, maintainability, and customer experience of our
applications.

By addressing the challenges of consistent error handling, we aim to enhance the stability of our products, ensuring
reduced downtime, clearer communication with customers through structured error messages, and quicker issue
resolution.

This not only improves the overall customer experience but also accelerates feature delivery as developers can focus
more on implementing new functionalities rather than troubleshooting ad-hoc errors. The resulting streamlined
development process contributes to cost reduction and optimised resource allocation.

In essence, these efforts are customer-centric, aiming to deliver a reliable, efficient, and customer-friendly service
interface that positively impacts the overall customer experience and product adoption.

## Decision Drivers

1. **Consistency and Standardization**: A consistent and standardised approach to error handling across different
   modules and components of our ZIO applications is crucial. This consistency will ease code readability, maintenance,
   and team collaboration.
2. **Robustness and Resilience**: A key driver is to harden our applications against failures by leveraging ZIO's
   powerful error recovery mechanisms. Enhancing the robustness of our applications will improve their resilience in
   adverse conditions.
3. **Traceability and Debugging**: Reducing debugging time and efforts associated with error resolution is another
   driving factor. An efficient error-handling strategy should enable traceability and quicker identification,
   communication, and resolution of errors.
4. **User Experience and Reliability**: Improving the quality and clarity of error messages reported to our users is a
   key driver. We aim to refine error messages to enable users to better understand what’s going on and respond to
   issues,
   thereby enhancing the user experience and the overall reliability of our applications.
5. **Alignment with Best Practices**: Aligning our error-handling strategies with industry best practices and leveraging
   the full potential of ZIO's error management features is a driver. Adhering to established standards can lead to more
   effective and maintainable code.

## Considered Options

### Option 1: Continue With The Current Error Handling Strategy

Continuing with the existing "so-so" error handling strategy currently in place within our ZIO applications without
significant modifications or improvements.

Pros:

- Maintains continuity with current practices, potentially requiring minimal adjustments and avoiding immediate
  disruptions to ongoing projects.

Cons:

- Inconsistencies in the code base across the components and services may persist, leading to potential challenges in
  maintenance and scalability.
- Engineers may spend time reinventing error-handling solutions rather than leveraging established best practices or
  frameworks.
- No significant improvement in terms of traceability and problem debugging, potentially hindering the resolution of
  issues and defects.

### Option 2: Leverage ZIO Failures And Defects Effectively

Adopting best practices for error handling within ZIO applications and effectively utilising ZIO Failures and Defects,
defining and implementing stricter guidelines and protocols for error handling.

Pros:

- Ensures adherence to established best practices, promoting code consistency, reliability, and scalability across
  various components and services.
- Reduces the need for developers to reinvent error-handling solutions, allowing them to leverage proven strategies and
  frameworks.
- Improves traceability and problem debugging by employing standardised error-handling practices, facilitating quicker
  issue identification and resolution.

Cons:

- Will require adjustments to current code and practices, necessitating time and effort for implementation.Actual impact
  and workload still needs to be identified.

## Decision Outcome

It has been decided to pursue Option 2: **Leverage ZIO Failures and Defects Effectively**. This decision aligns with our
commitment to enhancing the reliability, scalability, and maintainability of our applications.

While we acknowledge this option requires more refactoring, its long-term benefits in terms of code quality, developer
efficiency, and robust error management outweigh the associated refactoring efforts.

## Option 2 - General Implementation Rules and Principles

### Case 1: When designing a component or service

#### Carefully Segregate Error Types

When designing a new component or service, the nature of anticipated errors should be carefully considered, and a clear
distinction between expected errors (i.e. ZIO Failures or domain-specific errors) and unexpected errors (i.e. ZIO
Defects) should be made.

This segregation should be done according to the principles outlined in
the [ZIO Types of Errors](https://zio.dev/reference/error-management/types) documentation section.
That is, carefully distinguishing between:

- **ZIO Failures**
    - The expected/recoverable errors (i.e. domain-specific errors).
    - Declared in the Error channel of the effect => ZIO[R, E, A].
    - Supposed to be handled by the caller to prevent call stack propagation.

- **ZIO Defects**
    - The unexpected/unrecoverable errors.
    - Not represented in the ZIO effect.
    - We do NOT expect the caller to handle them.
    - Propagated throughout the call stack until converted to a Failure or logged for traceability and debugging
      purposes by
      the uppermost layer.

#### Use ADT to Model ZIO Failures

Use **Algebraic Data Types** (ADTs) to model domain-specific errors as ZIO failures within the component/service
interface.

_Implementation tips:_

- **Use a sealed trait or abstract class** to represent the hierarchy of ZIO Failures, allowing for a well-defined set
  of error possibilities.
- **Define specific error cases within the companion object** of the sealed trait. This practice prevents potential
  conflicts when importing errors with common names (e.g. RecordNotFoundError), allowing users to prefix them with the
  name of the parent sealed trait for better code clarity.

_Example:_

```scala
sealed trait DomainError

object DomainError {
  final case class BusinessLogicError(message: String) extends DomainError

  final case class DataValidationError(message: String) extends DomainError
}
```

#### Use Scala 3 Union Types to Be More Specific About ZIO Failure Types

Using the Scala 3 Union Types feature to declare the expected failures of a ZIO effect should be preferred over using
the broader and more generic top-level sealed trait. This allows for a more explicit and detailed definition of
potential failure scenarios and enhances error handling accuracy on the caller side.

This principle is outlined in
the [following section](https://zio.dev/reference/error-management/best-practices/union-types) of the ZIO Error
Management Best Practices documentation.

_Example:_

```scala
trait MyService {
  def myMethod(): ZIO[Any, BusinessLogicError | DataValidationError, Unit]
}
```

#### Don’t Type Unexpected Errors (i.e. ZIO Defects)

When we first discover typed errors, it may be tempting to put every error into the ZIO failure type parameter/channel.
That is a mistake because we can't recover from all types of errors. When we encounter unexpected errors we can't do
anything with, we should let the application die (i.e. the ZIO fiber). This is known as the “Let it Crash” principle,
and it is a good approach for all unexpected errors.

This principle is outlined in
the [following section](https://zio.dev/reference/error-management/best-practices/unexpected-errors) of the ZIO Error
Management Best Practices documentation.

### Case 2: When calling an existing component or service

#### Only Catch Failures You Effectively Handle

As a user of an existing component or service, you should exclusively catch failures that you are prepared to
effectively handle. Any unhandled failures should be transformed into defects and propagated through the call stack. You
should not expect callers of your component to handle lower-level failures that you do not handle.

#### Use Failure Wrappers To Prevent Failures Leakage From Lower Layers

When invoking lower-level components, do not directly expose their failure types in your component interface. Use
wrappers to encapsulate and abstract failure types originating from lower-level components, thus enhancing
loose coupling and safeguarding against leakage of underlying implementation details to the caller.

Using failure wrappers and propagate them **should not be the default strategy**. Lower-level failures should primarily
be managed at your component implementation level ensuring that it handles and appropriately recovers them.

Unhandled failures within the component's boundaries should preferably be transformed into defects.

#### Do not reflexively log errors

Avoid catching a ZIO failure or defect solely for the purpose of logging it. Instead, consider allowing the error to
propagate through the call stack. It's preferable to assume that the uppermost layer, commonly known as **'the end of
the world' will handle the logging** of those errors. This practice promotes a centralised and consistent logging
approach for better traceability and debugging.

This principle is outlined in
the [following section](https://zio.dev/reference/error-management/best-practices/logging-errors) of the ZIO Error
Management Best Practices documentation.

#### Adopt The “Let it Crash” Principle For ZIO Defects

Adopt the “Let it Crash” principle for ZIO defects. Let them bubble up the call stack and crash the current ZIO fiber.
They will be handled/recovered at a higher level or logged appropriately “at the end of the world” by the uppermost
layer.

## Option 2 - Practical Implementation

### Repository Layer

#### Try using defects only (`UIO` or `URIO`)

The repository layer leverages Doobie,
which [natively relies on unchecked exceptions](https://tpolecat.github.io/doobie/docs/09-Error-Handling.html#about-exceptions).
Doobie will report any database error as a subclass of `Throwable`, and its specific type will be directly
linked to the underlying database implementation (i.e. PostgreSQL). This means there is no deterministic way to recover
from an SQL execution error in a database agnostic way.

A good approach is to use ZIO Defects to report repository errors, declaring all repository methods as `URIO`
or `UIO`([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/main/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/repository/ConnectionRepository.scala)).
Conversely, declaring them as `Task` assumes that the caller (i.e. service) is able to properly handle and
recover from the low-level and database specific exceptions exposed in the error channel, which is a fallacy.

```scala
trait ConnectionRepository {
  def findAll: URIO[WalletAccessContext, Seq[ConnectionRecord]]
}
```

Converting a ZIO `Task` to ZIO `UIO` can easily be done
using `ZIO#orDie`([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/sql-doobie/src/main/scala/io/iohk/atala/connect/sql/repository/JdbcConnectionRepository.scala#L114)).

```scala
class JdbcConnectionRepository(xa: Transactor[ContextAwareTask], xb: Transactor[Task]) extends ConnectionRepository {
  override def findAll: URIO[WalletAccessContext, Seq[ConnectionRecord]] = {
    val cxnIO =
      sql"""
           | SELECT
           |   id,
           |   created_at,
           |   ...
           | FROM public.connection_records
           | ORDER BY created_at
        """.stripMargin
        .query[ConnectionRecord]
        .to[Seq]

    cxnIO
      .transactWallet(xa)
      .orDie
  }
}
```

For those cases where one has to generate a defect, a common way to do this is by using the following ZIO
construct ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/sql-doobie/src/main/scala/io/iohk/atala/connect/sql/repository/JdbcConnectionRepository.scala#L212)):

```scala
class JdbcConnectionRepository(xa: Transactor[ContextAwareTask], xb: Transactor[Task]) extends ConnectionRepository {
  override def getById(recordId: UUID): URIO[WalletAccessContext, ConnectionRecord] =
    for {
      maybeRecord <- findById(recordId)
      record <- ZIO.fromOption(maybeRecord).orDieWith(_ => RuntimeException(s"Record not found: $recordId"))
    } yield record
}
```

#### Apply the `get` vs `find` pattern

Follow the `get` and `find` best practice in the repository interface for read operations:

- `getXxx()` returns the requested record or throws an unexpected exception/defect when not
  found ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/repository/ConnectionRepository.scala#L36)).
- `findXxx()` returns an `Option` with or without the request record. This allows the caller service to handle
  the `found`
  and `not-found` cases and report appropriately to the end
  user ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/repository/ConnectionRepository.scala#L32)).

```scala
trait ConnectionRepository {
  def findById(recordId: UUID): URIO[WalletAccessContext, Option[ConnectionRecord]]

  def getById(recordId: UUID): URIO[WalletAccessContext, ConnectionRecord]
}
```

#### Do not return the affected row count

The `create`, `update` or `delete` repository methods should not return an `Int` indicating the number of rows affected
by the operation but either return `Unit` when successful or throw an exception/defect when the row count is not what is
expected, like e.g. an update operation resulting in a `0` affected row
count ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/sql-doobie/src/main/scala/io/iohk/atala/connect/sql/repository/JdbcConnectionRepository.scala#L85)).

```scala
class JdbcConnectionRepository(xa: Transactor[ContextAwareTask], xb: Transactor[Task]) extends ConnectionRepository {
  override def create(record: ConnectionRecord): URIO[WalletAccessContext, Unit] = {
    val cxnIO =
      sql"""
           | INSERT INTO public.connection_records(
           |   id,
           |   created_at,
           |   ...
           | ) values (
           |   ${record.id},
           |   ${record.createdAt},
           |   ...
           | )
        """.stripMargin.update

    cxnIO.run
      .transactWallet(xa)
      .ensureOneAffectedRowOrDie
  }
}

extension[Int](ma: RIO[WalletAccessContext, Int]) {
  def ensureOneAffectedRowOrDie: URIO[WalletAccessContext, Unit] = ma.flatMap {
    case 1 => ZIO.unit
    case count => ZIO.fail(RuntimeException(s"Unexpected affected row count: $count"))
  }.orDie
}
```

#### Do not reflexively log errors

The upper layer will automatically do so in an appropriate and consistent way using Tapir interceptor customisation.

### Service Layer

#### Reporting `404 Not Found` to user

How can a service appropriately report a `404 Not Found` to a user that e.g. tries to update a record
that does not exist in the database? Following the above rules, the `update` method will throw a defect that will be
caught at the upper level and return a generic `500 Internal Server Error` to the user.

For those cases where a specific error like `404` should be returned, it is up to the service to first call `find()`
before `update()` and construct a `NotFound` failure, propagated through error channel, if it gives
a `None` ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/service/ConnectionServiceImpl.scala#L149)).

Relying on the service layer to implement it will guarantee consistent behaviour regardless of the underlying database
type (could be different RDMS flavour, No-SQL, etc.).

```scala
class ConnectionServiceImpl() extends ConnectionService {
  override def markConnectionRequestSent(recordId: UUID):
  ZIO[WalletAccessContext, RecordIdNotFound | InvalidStateForOperation, ConnectionRecord] =
    for {
      maybeRecord <- connectionRepository.findById(recordId)
      record <- ZIO.fromOption(maybeRecord).mapError(_ => RecordIdNotFound(recordId))
      updatedRecord <- updateConnectionProtocolState(
        recordId,
        ProtocolState.ConnectionRequestPending,
        ProtocolState.ConnectionRequestSent
      )
    } yield updatedRecord
}
```

#### Do not type unexpected errors

Defects from lower layers (typically repository) should not be wrapped in a failure, and error case class declarations
like [this](https://github.com/hyperledger-labs/open-enterprise-agent/blob/b579fd86ab96db711425f511154e74be75583896/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/model/error/ConnectionServiceError.scala#L8)
should be prohibited.

Also, considering that failures are expected errors from which user can potentially recover, error case class
like `UnexpectedError` should be
prohibited ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/b579fd86ab96db711425f511154e74be75583896/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/model/error/ConnectionServiceError.scala#L12)).

#### Extend the common `Failure` trait

Make sure all service errors extend the shared
trait [`io.iohk.atala.shared.models.Failure`](https://github.com/hyperledger-labs/open-enterprise-agent/blob/main/shared/src/main/scala/io/iohk/atala/shared/models/Failure.scala).
This allows handling "at the end of the world“ to be done in a consistent and in generic way.

Create an exhaustive and meaningful list of service errors and make sure the value of the `userFacingMessage` attribute
is chosen wisely! It will be presented "as is" to the user and should not contain any sensitive
data ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/model/error/ConnectionServiceError.scala#L14)).

```scala
trait Failure {
  val statusCode: StatusCode
  val userFacingMessage: String
}

sealed trait ConnectionServiceError(
                                     val statusCode: StatusCode,
                                     val userFacingMessage: String
                                   ) extends Failure

object ConnectionServiceError {
  final case class InvitationAlreadyReceived(invitationId: String)
    extends ConnectionServiceError(
      StatusCode.BadRequest,
      s"The provided invitation has already been used: invitationId=$invitationId"
    )
}
```

#### User Input Validation

User input validation (business invariants) should primarily be enforced at the service layer and implemented using the
[ZIO Prelude framework](https://zio.dev/zio-prelude/functional-data-types/validation).

While it may also be partially implemented at the REST entry point level via OpenAPI specification, it is crucial to
enforce validation at the service level as well. This ensures consistency and reliability across all interfaces that may
call our services, recognising that REST may not be the sole interface interacting with our services.

```scala
class ConnectionServiceImpl() extends ConnectionService {
  def validateInputs(
                      label: Option[String],
                      goalCode: Option[String],
                      goal: Option[String]
                    ): IO[UserInputValidationError, Unit] = {
    val validation = Validation
      .validate(
        ValidationUtils.validateLengthOptional("label", label, 0, 255),
        ValidationUtils.validateLengthOptional("goalCode", goalCode, 0, 255),
        ValidationUtils.validateLengthOptional("goal", goal, 0, 255)
      )
      .unit
    ZIO.fromEither(validation.toEither).mapError(UserInputValidationError.apply)
  }
}
```

Validation errors should be modelled using a dedicated error case class and, when possible, provide validation failure
details. One could use a construct like the following:

```scala
sealed trait ConnectionServiceError(
                                     val statusCode: StatusCode,
                                     val userFacingMessage: String
                                   ) extends Failure

object ConnectionServiceError {
  final case class UserInputValidationError(errors: NonEmptyChunk[String])
    extends ConnectionServiceError(
      StatusCode.BadRequest,
      s"The provided input failed validation: errors=${errors.mkString("[", "], [", "]")}"
    )
}
```

#### Use Scala 3 Union Types

Use Scala 3 union-types declaration in the effect’s error channel to clearly notify the caller of potential
failures ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/connect/lib/core/src/main/scala/io/iohk/atala/connect/core/service/ConnectionServiceImpl.scala#L178))

````scala
class ConnectionServiceImpl() extends ConnectionService {

  override def receiveConnectionRequest(request: ConnectionRequest, expirationTime: Option[Duration] = None):
  ZIO[WalletAccessContext, ThreadIdNotFound | InvalidStateForOperation | InvitationExpired, ConnectionRecord] = ???

}
````

#### Do not reflexively log errors

The upper layer will automatically do so in an appropriate and consistent way using Tapir interceptor customisation.

### Controller Layer

#### Reporting RFC-9457 Error Response

All declared Tapir endpoints must
use [`io.iohk.atala.api.http.ErrorResponse`](https://github.com/hyperledger-labs/open-enterprise-agent/blob/main/prism-agent/service/server/src/main/scala/io/iohk/atala/api/http/ErrorResponse.scala)
as their output error type ([example](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/prism-agent/service/server/src/main/scala/io/iohk/atala/connect/controller/ConnectionEndpoints.scala#L45))
This type ensures that the response returned to the user complies with
the [RFC-9457 Problem Details for HTTP APIs](https://www.rfc-editor.org/rfc/rfc9457.html).

```scala
object ConnectionEndpoints {
  
  val createConnection: Endpoint[
    (ApiKeyCredentials, JwtCredentials),
    (RequestContext, CreateConnectionRequest),
    ErrorResponse,
    Connection,
    Any
  ] = ???
  
}
```

#### Use "Failure => ErrorResponse" Implicit Conversion

If all the underlying services used by a controller comply with the above rules, then the only error type that could
propagate through the effect’s error channel is the parent [`io.iohk.atala.shared.models.Failure`](https://github.com/hyperledger-labs/open-enterprise-agent/blob/main/shared/src/main/scala/io/iohk/atala/shared/models/Failure.scala) type and its conversion
to the ErrorResponse type is done automatically via [Scala implicit conversion](https://github.com/hyperledger-labs/open-enterprise-agent/blob/eb898e068f768507d6979a5d9bab35ef7ad4a045/prism-agent/service/server/src/main/scala/io/iohk/atala/api/http/ErrorResponse.scala#L44).

#### Do not reflexively log errors

The upper layer will automatically do so in an appropriate and consistent way using Tapir interceptor customisation.