package otus

import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.parser.decode

import java.io.PrintWriter
import java.nio.file.Paths
import scala.io.Source
import io.circe.syntax._

object JsonParser extends App {

  if (args.length == 0) {
    println("Plz enter file name")
  }


  case class Country(name: CountryName, region: String, area: BigDecimal, capital: Array[String])

  case class CountryName(common: String, official: String)

  case class ResultCountry(name: String, capital: String, area: BigDecimal)

  val source = Source.fromURL("https://raw.githubusercontent.com/mledoze/countries/master/countries.json");

  val lines = try source.mkString finally source.close()

  implicit val countryNameDecoder: Decoder[CountryName] = deriveDecoder
  implicit val countryDecoder: Decoder[Country] = deriveDecoder

  implicit val resultCountryEncoder: Encoder[ResultCountry] = deriveEncoder

  val countries: Array[ResultCountry] = decode[Array[Country]](lines) match {
    case Left(e) =>
      println(s"FAILED: $e")
      throw e
    case Right(country) =>
      country.filter(c => c.region.equals("Africa"))
        .sortBy(_.area)(Ordering[BigDecimal].reverse)
        .take(10)
        .map(c => ResultCountry(c.name.official, c.capital(0), c.area))

  }

  val filename = args(0)

  val file = Paths.get(filename);

  new PrintWriter(filename) {
    write(countries.asJson.toString())
    close()
  }

}
