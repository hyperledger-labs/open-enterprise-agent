package org.hyperledger.identus.shared.db

import cats.effect.std.Dispatcher
import cats.effect.Async
import doobie.util.transactor.Transactor
import zio.*
import zio.test.*

import java.util.concurrent.atomic.AtomicBoolean

object ContextAwareTaskSpec extends ZIOSpecDefault {
  override def spec = suite("ContextAwareTask")(
    test("allocates, uses and releases a Cats dispatcher as a Task") {
      val async = summon[Async[ContextAwareTask]]
      val task: ContextAwareTask[Int] = Dispatcher.parallel[ContextAwareTask].use { dispatcher =>
        async.fromFuture(async.delay(dispatcher.unsafeToFuture(async.pure(42))))
      }
      task.map(value => assertTrue(value == 42))
    },
    test("keeps wallet and system transactors distinct in the environment") {
      assertTrue(
        Tag[Transactor[ContextAwareTask]].tag != Tag[Transactor[Task]].tag
      )
    },
    test("runs resource finalizers on failure") {
      val async = summon[Async[ContextAwareTask]]
      val failure = new RuntimeException("test failure")
      for {
        released <- ZIO.succeed(new AtomicBoolean(false))
        task = async.bracket(async.unit)(_ => async.raiseError[Int](failure))(_ => async.delay(released.set(true)))
        result <- task.either
      } yield assertTrue(result == Left(failure), released.get())
    }
  )
}
