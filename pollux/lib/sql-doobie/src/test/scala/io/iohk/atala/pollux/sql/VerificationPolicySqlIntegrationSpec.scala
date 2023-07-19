package io.iohk.atala.pollux.sql

import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import io.iohk.atala.pollux.core.model.{
  CredentialSchemaAndTrustedIssuersConstraint,
  VerificationPolicy,
  VerificationPolicyConstraint
}
import io.iohk.atala.pollux.core.repository.VerificationPolicyRepository
import io.iohk.atala.pollux.sql.model.db.VerificationPolicySql
import io.iohk.atala.pollux.sql.repository.JdbcVerificationPolicyRepository
import io.iohk.atala.test.container.MigrationAspects.*
import io.iohk.atala.test.container.PostgresLayer.*
import zio.*
import zio.interop.catz.*
import zio.interop.catz.implicits.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

object VerificationPolicySqlIntegrationSpec extends ZIOSpecDefault {

  private val pgLayer = postgresLayer()
  private val transactorLayer =
    pgLayer >>> hikariConfigLayer >>> transactor
  private val repositoryLayer =
    transactorLayer >>> JdbcVerificationPolicyRepository.layer
  private val testEnvironmentLayer =
    zio.test.testEnvironment ++ pgLayer ++ transactorLayer ++ repositoryLayer

  def spec = (suite("verification policy DAL spec")(
    verificationPolicyCRUDSuite,
    verificationPolicyLookupSuite
  ) @@ nondeterministic @@ sequential @@ timed @@ migrate(
    schema = "public",
    paths = "classpath:sql/pollux"
  )).provideSomeLayerShared(testEnvironmentLayer)

  val verificationPolicyCRUDSuite =
    suite("verification policy CRUD operations")(
      test("insert, findById, update and delete operations") {
        for {
          tx <- ZIO.service[Transactor[Task]]
          repo <- ZIO.service[VerificationPolicyRepository[Task]]

          expectedCreated <- VerificationPolicyGen.verificationPolicyZIO
            .runCollectN(1)
            .flatMap(_.head)
          actualCreated <- repo.create(expectedCreated)
          getByIdCreated <- repo.get(expectedCreated.id)

          allRecordsAreSimilar = assert(expectedCreated)(
            equalTo(actualCreated)
          ) &&
            assert(getByIdCreated)(isSome(equalTo(actualCreated)))

          isCreated = assert(actualCreated)(equalTo(expectedCreated)) &&
            assert(getByIdCreated)(isSome(equalTo(expectedCreated)))

          expectedUpdated = expectedCreated.copy(
            name = "new name",
            description = "new description"
          )
          actualUpdated <- repo
            .update(
              actualCreated.id,
              actualCreated.nonce,
              expectedUpdated
            )
            .map(_.get)
          getByIdUpdated <- repo.get(expectedUpdated.id).map(_.get)

          isUpdated = assert(actualUpdated)(equalTo(expectedUpdated.copy(updatedAt = actualUpdated.updatedAt))) &&
            assert(getByIdUpdated)(equalTo(expectedUpdated.copy(updatedAt = actualUpdated.updatedAt)))

          //

          expectedUpdated2 = actualUpdated.copy(
            name = "new name 2 ",
            description = "new description 2"
          )
          actualUpdated2 <- repo
            .update(
              actualUpdated.id,
              actualUpdated.nonce,
              expectedUpdated2
            )
            .map(_.get)
          getByIdUpdated2 <- repo.get(expectedUpdated2.id).map(_.get)

          isUpdated2 = assert(actualUpdated2)(equalTo(expectedUpdated2.copy(updatedAt = actualUpdated.updatedAt))) &&
            assert(getByIdUpdated2)(equalTo(expectedUpdated2.copy(updatedAt = actualUpdated.updatedAt)))

          //

          actualDeleted <- repo.delete(
            expectedUpdated.id
          )

          isDeletedReturnedBack = assert(actualDeleted)(
            isSome(equalTo(actualUpdated2))
          )
          getByIdDeleted <- repo.get(actualUpdated.id)

          isDeleted = assert(getByIdDeleted)(isNone)

          _ <- VerificationPolicySql.deleteAll().transact(tx)
        } yield isCreated && allRecordsAreSimilar && isUpdated && isDeletedReturnedBack && isDeleted
      },
      deleteAllVerificationPoliciesTest,
      insertNVerificationPoliciesTest(100),
      deleteAllVerificationPoliciesTest
    ) @@ nondeterministic @@ sequential @@ timed

  def insertNVerificationPoliciesTest(n: Int) =
    test(s"insert $n verification policies entries") {
      for {
        repo <- ZIO.service[VerificationPolicyRepository[Task]]

        generatedVerificationPolicies: List[VerificationPolicy] <-
          VerificationPolicyGen.verificationPolicyZIO
            .runCollectN(n)
            .flatMap(ZIO.collectAll)

        _ <- ZIO.collectAll(
          generatedVerificationPolicies.map(vp => repo.create(vp))
        )

        totalCount <- repo.totalCount()

        allNEntitiesAreStored = assert(totalCount)(equalTo(n))
      } yield allNEntitiesAreStored
    }
  def deleteAllVerificationPoliciesTest =
    test("delete all verification policies entries") {
      for {
        tx <- ZIO.service[Transactor[Task]]
        repo <- ZIO.service[VerificationPolicyRepository[Task]]

        _ <- VerificationPolicySql.deleteAll().transact(tx)
        totalCount <- repo.totalCount()

        allEntitiesAreDeleted = assert(totalCount)(equalTo(0))
      } yield allEntitiesAreDeleted
    }

  val N = 30
  val verificationPolicyLookupSuite = suite("lookup operations")(
    insertNVerificationPoliciesTest(N),
    test("get all in one page") {
      for {
        repo <- ZIO.service[VerificationPolicyRepository[Task]]
        all <- repo.lookup(None, None, None)
        allNRecordsAreReturned = assert(all.length)(equalTo(N))
      } yield allNRecordsAreReturned
    },
    test("get all by two pages") {
      for {
        repo <- ZIO.service[VerificationPolicyRepository[Task]]
        first <- repo.lookup(None, offsetOpt = Some(0), limitOpt = Some(N / 2))
        second <- repo.lookup(
          None,
          offsetOpt = Some(N / 2),
          limitOpt = Some(N / 2)
        )
        firstPageContainsAHalfOfTheRecords = assert(first.length)(
          equalTo(N / 2)
        )
        secondPageContainsAHalfOfTheRecords = assert(second.length)(
          equalTo(N - N / 2)
        )
        allNRecordsAreReturned = assert(first.length + second.length)(
          equalTo(N)
        )
      } yield firstPageContainsAHalfOfTheRecords &&
        secondPageContainsAHalfOfTheRecords &&
        allNRecordsAreReturned
    },
    test("paginate through the collection of verifiable policies") {
      for {
        repo <- ZIO.service[VerificationPolicyRepository[Task]]

        paginator = new Paginator(skipLimit =>
          repo.lookup(
            nameOpt = None,
            offsetOpt = Some(skipLimit.skip),
            limitOpt = Some(skipLimit.limit)
          )
        )

        allItems1 <- paginator.fetchAll(SkipLimit(0, 1))
        allItems10 <- paginator.fetchAll(SkipLimit(0, 10))
        allItems20 <- paginator.fetchAll(SkipLimit(0, 20))
        totalCount <- repo.totalCount()

        allItemsArePaginated1 = assert(totalCount)(equalTo(allItems1.length))
        allItemsArePaginated10 = assert(totalCount)(equalTo(allItems10.length))
        allItemsArePaginated20 = assert(totalCount)(equalTo(allItems20.length))
      } yield allItemsArePaginated1 && allItemsArePaginated10 && allItemsArePaginated20
    }
  )

  object VerificationPolicyGen {
    val id = Gen.uuid
    val name =
      Gen.alphaNumericStringBounded(5, 10).map("Generated Policy Name " + _)
    val description = Gen.alphaNumericStringBounded(5, 30)

    val schemaId =
      Gen.alphaNumericStringBounded(30, 30).map("http://atala.io/schemas/" + _)
    val trustedIssuer =
      Gen.alphaNumericStringBounded(64, 64).map("did:prism:" + _)
    val trustedIssuers = Gen.setOfBounded(1, 10)(trustedIssuer).map(_.toVector)

    val verificationPolicyConstraint: Gen[Any, VerificationPolicyConstraint] =
      for {
        schemaId <- schemaId
        trustedIssuers <- trustedIssuers
      } yield CredentialSchemaAndTrustedIssuersConstraint(
        schemaId,
        trustedIssuers
      )

    val verificationPolicyZIO: Gen[Any, UIO[VerificationPolicy]] = for {
      name <- name
      description <- description
      constraints <- Gen
        .setOfBounded(min = 1, max = 10)(verificationPolicyConstraint)
        .map(_.toVector)
    } yield VerificationPolicy.make(
      name = name,
      description = description,
      constraints = constraints
    )
  }

  case class SkipLimit(skip: Int, limit: Int) {
    def next: SkipLimit = SkipLimit(skip + limit, limit)
  }
  class Paginator[T](page: SkipLimit => Task[List[T]]) {
    def fetchAll(
        from: SkipLimit,
        acc: List[T] = List.empty[T]
    ): Task[List[T]] = {
      val nextPage = page(from)
      nextPage.flatMap(items => items.headOption.fold(ZIO.succeed(acc))(nonEmpty => fetchAll(from.next, acc ++ items)))
    }

    def fetchPage(from: SkipLimit): Task[List[T]] = page(from)
  }
}
