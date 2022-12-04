//> using scala "2.13.10"
//> using lib "org.http4s::http4s-ember-server:1.0.0-M37"
//> using lib "org.http4s::http4s-ember-client:1.0.0-M37"
//> using lib "org.http4s::http4s-dsl:1.0.0-M37"

import cats.effect._
import cats.syntax.all._
import com.comcast.ip4s._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._

import scala.concurrent.duration.DurationInt

object Main extends IOApp.Simple {

  val routes = (r: Ref[IO, String]) =>
    HttpRoutes.of[IO] {
      case GET -> Root / "remember" => r.get.flatMap(Ok(_))
      case req @ POST -> Root / "remember" =>
        req.bodyText[IO].compile.string.flatMap(r.set) >>
          Ok("ok")
    }

  val sv = Resource
    .eval(Ref[IO].of("hello"))
    .flatMap(r => EmberServerBuilder.default[IO].withPort(port"8089").withHttpApp(routes(r).orNotFound).build)

  val cl  = EmberClientBuilder.default[IO].build
  val uri = Uri.unsafeFromString("http://localhost:8089/remember")

  def run: IO[Unit] = (sv, cl).tupled.map(_._2).use { cl =>
    cl.expect[String](uri).flatMap(IO.println) >>
      cl.run(Request[IO](POST, uri, entity = EntityEncoder[IO, String].toEntity("goodbye"))).use_ >>
      cl.expect[String](uri).flatMap(IO.println)
  }

}
