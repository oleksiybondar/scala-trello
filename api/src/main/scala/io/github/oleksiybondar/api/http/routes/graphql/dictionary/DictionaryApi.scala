package io.github.oleksiybondar.api.http.routes.graphql.dictionary

import cats.effect.unsafe.implicits.global
import io.github.oleksiybondar.api.domain.ticket.TicketSeverity
import io.github.oleksiybondar.api.domain.timeTracking.TimeTrackingActivity
import io.github.oleksiybondar.api.http.routes.graphql.GraphQLContext
import sangria.schema.{Field, ListType, ObjectType, OptionType, StringType, fields}

object DictionaryApi {

  val TicketSeverityType: ObjectType[GraphQLContext, TicketSeverity] =
    ObjectType(
      name = "TicketSeverity",
      fields[GraphQLContext, TicketSeverity](
        Field("id", StringType, resolve = _.value.id.value.toString),
        Field("name", StringType, resolve = _.value.name.value),
        Field("description", OptionType(StringType), resolve = _.value.description)
      )
    )

  val TimeTrackingActivityType: ObjectType[GraphQLContext, TimeTrackingActivity] =
    ObjectType(
      name = "TimeTrackingActivity",
      fields[GraphQLContext, TimeTrackingActivity](
        Field("id", StringType, resolve = _.value.id.value.toString),
        Field("code", StringType, resolve = _.value.code.value),
        Field("name", StringType, resolve = _.value.name.value),
        Field("description", OptionType(StringType), resolve = _.value.description)
      )
    )

  val queryFields: List[Field[GraphQLContext, Unit]] =
    List(
      Field(
        name = "ticketSeverities",
        fieldType = ListType(TicketSeverityType),
        resolve = ctx => ctx.ctx.ticketSeverityRepo.list.unsafeToFuture()
      ),
      Field(
        name = "timeTrackingActivities",
        fieldType = ListType(TimeTrackingActivityType),
        resolve = ctx => ctx.ctx.timeTrackingActivityRepo.list.unsafeToFuture()
      )
    )
}
