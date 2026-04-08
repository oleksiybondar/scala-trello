package io.github.oleksiybondar.api.infrastructure.db.board

import io.github.oleksiybondar.api.domain.board.BoardId
import io.github.oleksiybondar.api.domain.permission.RoleId
import io.github.oleksiybondar.api.domain.user.UserId
import io.github.oleksiybondar.api.testkit.fixtures.BoardMemberFixtures
import io.github.oleksiybondar.api.testkit.fixtures.SlickBoardMemberRepoFixtures.withCleanRepo
import munit.FunSuite

import java.time.Instant
import java.util.UUID

class SlickBoardMemberRepoSpec extends FunSuite {

  test("create persists a dashboard member that can be loaded by dashboard and user") {
    val member = BoardMemberFixtures.sampleMember

    withCleanRepo { repo =>
      for {
        _      <- repo.create(member)
        result <- repo.findByDashboardIdAndUserId(member.dashboardId, member.userId)
      } yield assertEquals(result, Some(member))
    }
  }

  test("listByDashboardId returns members for the requested dashboard ordered by createdAt") {
    val firstMember  = BoardMemberFixtures.sampleMember
    val secondMember =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        roleId = RoleId(2),
        createdAt = Instant.parse("2026-04-06T08:05:00Z")
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstMember)
        _      <- repo.create(secondMember)
        result <- repo.listByDashboardId(firstMember.dashboardId)
      } yield assertEquals(result, List(firstMember, secondMember))
    }
  }

  test("listByUserId returns memberships for the requested user") {
    val firstMember  = BoardMemberFixtures.sampleMember
    val secondMember =
      BoardMemberFixtures.member(
        dashboardId = BoardId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb")),
        createdAt = Instant.parse("2026-04-06T09:05:00Z")
      )
    val otherMember  =
      BoardMemberFixtures.member(
        userId = UserId(UUID.fromString("22222222-2222-2222-2222-222222222222"))
      )

    withCleanRepo { repo =>
      for {
        _      <- repo.create(firstMember)
        _      <- repo.create(secondMember)
        _      <- repo.create(otherMember)
        result <- repo.listByUserId(firstMember.userId)
      } yield assertEquals(result, List(firstMember, secondMember))
    }
  }

  test("updateRole returns true and updates the role for an existing member") {
    val member = BoardMemberFixtures.sampleMember

    withCleanRepo { repo =>
      for {
        _       <- repo.create(member)
        updated <- repo.updateRole(member.dashboardId, member.userId, RoleId(3))
        result  <- repo.findByDashboardIdAndUserId(member.dashboardId, member.userId)
      } yield {
        assertEquals(updated, true)
        assertEquals(result.map(_.roleId), Some(RoleId(3)))
      }
    }
  }

  test("updateRole returns false when the membership does not exist") {
    withCleanRepo { repo =>
      repo
        .updateRole(
          BoardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
          UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
          RoleId(2)
        )
        .map(updated => assertEquals(updated, false))
    }
  }

  test("delete returns true for an existing membership and removes it") {
    val member = BoardMemberFixtures.sampleMember

    withCleanRepo { repo =>
      for {
        _       <- repo.create(member)
        deleted <- repo.delete(member.dashboardId, member.userId)
        result  <- repo.findByDashboardIdAndUserId(member.dashboardId, member.userId)
      } yield {
        assertEquals(deleted, true)
        assertEquals(result, None)
      }
    }
  }

  test("delete returns false when the membership does not exist") {
    withCleanRepo { repo =>
      repo
        .delete(
          BoardId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
          UserId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        )
        .map(deleted => assertEquals(deleted, false))
    }
  }
}
