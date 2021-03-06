package com.phasmid.hedge_fund.model

import spray.http.Uri._
import spray.http.Uri
import com.phasmid.hedge_fund.http.UriGet

/**
 * @author robinhillyard
 */

case class YQLQuery(format: String, diagnostics: Boolean) extends Query {
  val uriGet = new UriGet()

  def createQuery(symbols: List[String]): Uri = {
    val symbolList = symbols map { s => '"' + s + '"' } mkString ("(", ",", ")")
    val query = s"select * from ${YQLQuery.financeQuotesTable} where symbol in $symbolList"
    val queryParams = Map("q" -> query, "format" -> format, "diagnostics" -> diagnostics.toString, "env" -> YQLQuery.env, "callback" -> "")
    uriGet.get(YQLQuery.server, YQLQuery.path, queryParams)
  }

  def getProtocol = format + ":YQL"
}

object YQLQuery {
  val financeQuotesTable = "yahoo.finance.quotes"
  val env = "http://datatables.org/alltables.env"
  val server = "query.yahooapis.com"
  val path = "/v1/public/yql"
}

class YQLModel extends Model {
  def isOption = false
  def getKey(query: String) = query match {
    case "name" => Some("YQL")
    case "symbol" => Some("symbol")
    case "price" => Some("Ask")
    case _ => None
  }
}
