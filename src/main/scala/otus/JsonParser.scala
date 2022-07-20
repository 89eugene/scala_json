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

  def decodeListTolerantly[A: Decoder]: Decoder[List[A]] =
    Decoder.decodeList(Decoder[A].either(Decoder[Json])).map(
      _.flatMap(_.left.toOption)
    )

  val tolerantCountryDecoder = decodeListTolerantly[Country]

  val countries: List[ResultCountry] = decode[List[Country]](lines)(tolerantCountryDecoder) match {
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
