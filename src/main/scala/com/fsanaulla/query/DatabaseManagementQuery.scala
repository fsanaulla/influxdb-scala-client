package com.fsanaulla.query

import akka.http.scaladsl.model.Uri

/**
  * Created by fayaz on 27.06.17.
  */
trait DatabaseManagementQuery extends QueryBuilder {

  protected def createDatabaseQuery(dbName: String): Uri = {
    queryBuilder("/query", s"CREATE DATABASE $dbName")
  }

  protected def dropDatabaseQuery(dbName: String): Uri = {
    queryBuilder("/query", s"DROP DATABASE $dbName")
  }

  protected def dropMeasurementQuery(dbName: String, measurementName: String): Uri = {
    queryBuilder("/query", Map("db" -> dbName, "q" -> s"DROP SERIES FROM $measurementName"))
  }
}
